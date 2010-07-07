/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.security.crypto.DummyEncrypter;
import org.dataminx.dts.security.crypto.Encrypter;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The AbstractJobPartitioningStrategy provides an implementation to the JobPartitioningStrategy
 * and lets its subclasses provide the implementation to the createDtsJobStepAllocator method
 * of the JobPartitioningStrategy. The application context for this module wires up the method
 * implementation to the createDtsJobStepAllocator based on the DtsJobStepAllocator the application
 * will to use.
 *
 * @author Gerson Galang
 * @author David Meredith (modifications) 
 */
public abstract class AbstractJobPartitioningStrategy implements
    JobPartitioningStrategy, InitializingBean {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(AbstractJobPartitioningStrategy.class);

    /** A flag if the user has requested the processing of the job to be stopped or cancelled. */
    private final boolean mCancelled = false;

    /** The total size of all the files that will be transferred by this job. */
    private long mTotalSize;

    /** The total number of files to be transferred by this job. */
    private int mTotalFiles;

    /** The total number of files to be transferred by a Data Transfer element (ie Source-Target pair). */
    private int mPerDataTransferTotalFiles;

    /** The maximum byte size limit of all the files that can be transferred by the step. */
    private long mMaxTotalByteSizePerStepLimit;

    /** The maximum number of files that can be transferred by the step. */
    private int mMaxTotalFileNumPerStepLimit;

    /** A reference to the Encrypter. */
    private Encrypter mEncrypter;

    /** The list of files excluded from the transfer. Some of these files might unreadable files. */
    private ArrayList<String> mExcluded = new ArrayList<String>();

    /** A reference to the DtsVfsUtil. */
    private DtsVfsUtil mDtsVfsUtil;

    /** A reference to the DtsJobStepAllocator. */
    private DtsJobStepAllocator mDtsJobStepAllocator;

    /** TODO: Define a new unique directory as a sub dir of the DTS_JOB_STEP_DIRECTORY_KEY
    for writing step files for this particular job */
    // private File mJobStepDir;

    /**
     * Add the given source to the list of files to transfer.
     *
     * @param source the source which normally is represents a single file
     * @param destination the target
     * @param dataTransferIndex the DataTransfer index on how this source/destination's is listed in the
     *        JobDefinitionDocument
     * @throws FileSystemException on failure to connect to either the source or destination
     */
    private void addFilesToTransfer(final FileObject source,
        final FileObject destination, final int dataTransferIndex)
        throws FileSystemException, FileNotFoundException {
        LOGGER.debug("addFilesToTransfer(\"" + source.getURL() + "\", \""
            + destination.getURL() + "\", " + dataTransferIndex + ")");
        // update member vars
        mTotalSize += source.getContent().getSize();
        ++mTotalFiles;
        ++mPerDataTransferTotalFiles;
        // adds the new DtsDataTransferUnit to the current DtsJobStep (in jobStepAllocator)
        // the dataTransferUnit represents a single file only transfer
        mDtsJobStepAllocator.addDataTransferUnit(
                new DtsDataTransferUnit(
                    source.getURL().toString(),
                    destination.getURL().toString(),
                    dataTransferIndex,
                    source.getContent().getSize()),
                mMaxTotalByteSizePerStepLimit,
                mMaxTotalFileNumPerStepLimit);
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (mEncrypter == null) {
            mEncrypter = new DummyEncrypter();
        }
        if (mMaxTotalByteSizePerStepLimit == 0) {
            mMaxTotalByteSizePerStepLimit = Long.MAX_VALUE;
        }
        else if (mMaxTotalByteSizePerStepLimit < 0) {
            throw new JobScopingException(
                "MaxTotalByteSizePerLimit should be a positive number.");
        }
        if (mMaxTotalFileNumPerStepLimit == 0) {
            mMaxTotalFileNumPerStepLimit = Integer.MAX_VALUE;
        }
        else if (mMaxTotalFileNumPerStepLimit < 0) {
            throw new JobScopingException(
                "MaxTotalFileNumPerStepLimit should be a positive number.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public DtsJobDetails partitionTheJob(final JobDefinitionType jobDefinition,
        final String jobResourceKey, final String jobTag)
        throws JobScopingException {
        Assert.hasText(jobResourceKey,
            "JobResourceKey should not be null or empty.");
        Assert.hasText(jobTag,
            "JobTag should not be null or empty.");
        Assert.notNull(jobDefinition, "JobDefinitionType should not be null.");
        if (mMaxTotalByteSizePerStepLimit < 0) {
            throw new DtsException(
                "MaxTotalByteSizePerLimit should be a positive number.");
        }
        if (mMaxTotalFileNumPerStepLimit < 0) {
            throw new DtsException(
                "MaxTotalFileNumPerStepLimit should be a positive number.");
        }

        FileSystemManager fileSystemManager = null;
        final DtsJobDetails jobDetails = new DtsJobDetails();

        // Create a new unique dir for this particular job
        // as a sub-directory of the DTS_JOB_STEP_DIRECTORY_KEY.
        // This method throws IllegalStateExceptions if the job dir cannot
        // be created. 
        jobDetails.setRootJobDir( this.createRootJobDir(jobTag) );
        
        try {
            try {
                fileSystemManager = mDtsVfsUtil.createNewFsManager();
            }
            catch (final FileSystemException e) {
                throw new JobScopingException(
                    "FileSystemException was thrown while creating new FileSystemManager in the job scoping task.",
                    e);
            }

            jobDetails.setJobDefinition(jobDefinition);
            jobDetails.setJobId(jobResourceKey);
            jobDetails.setJobTag(jobTag);

            mDtsJobStepAllocator = createDtsJobStepAllocator();
            mDtsJobStepAllocator.setJobStepSaveDir( jobDetails.getRootJobDir() );
            mExcluded = new ArrayList<String>();
            mTotalSize = 0;
            mTotalFiles = 0;

            // populate the dataTransfers List from the given jobDefinition (i.e. a
            // list of source to sink constructs).
            final List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

            final JobDescriptionType jobDescription = jobDefinition
                .getJobDescription();
            if (jobDescription instanceof MinxJobDescriptionType) {
                final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
                CollectionUtils.addAll(dataTransfers, minxJobDescription
                    .getDataTransferArray());
            }
            if (CollectionUtils.isEmpty(dataTransfers)) {
                LOGGER
                    .warn("DTS job request is incomplete as it does not contain any data transfer elements.");
                throw new DtsJobExecutionException(
                    "DTS job request contains no data transfer elements.");
            }
            int dataTransferIndex = 0;
            for (final DataTransferType dataTransfer : dataTransfers) {

                // reset the total number of files to be transferred within this DataTransfer element
                mPerDataTransferTotalFiles = 0;

                // the sourceParent can be a directory or a file that needs to be
                // transferred to the target destination
                FileObject sourceParent = null;
                try {
                    sourceParent = fileSystemManager.resolveFile(
                        dataTransfer.getSource().getURI(),
                        mDtsVfsUtil.getFileSystemOptions(dataTransfer.getSource(),mEncrypter));

                    if (!sourceParent.getContent().getFile().exists()
                        || !sourceParent.getContent().getFile().isReadable()) {
                        throw new JobScopingException("The source "
                            + sourceParent
                            + " provided does not exist or is not readable.");
                    }
                }
                catch (final FileSystemException e) {
                    throw new JobScopingException(
                        "FileSystemException was thrown while accessing the remote file "
                            + dataTransfer.getSource().getURI() + ".", e);
                }

                FileObject targetParent = null;
                try {
                    targetParent = fileSystemManager.resolveFile(
                            dataTransfer.getTarget().getURI(),
                            mDtsVfsUtil.getFileSystemOptions(dataTransfer.getTarget(),mEncrypter));
                }
                catch (final FileSystemException e) {
                    throw new JobScopingException(
                        "FileSystemException was thrown while accessing the remote file "
                            + dataTransfer.getTarget().getURI() + ".", e);
                }

                try {

                    // 1 JobStepAllocator  -has->  *DtsJobSteps
                    // 1 DtsJobStep        -has->  *DtsDataTransferUnits
                    //
                    // Call createNewDataTransfer to initialise the current
                    // allocator's step with the given byte-size and file-num
                    // constraints (i.e. mDtsJobStepAllocator.mTmpDtsJobStep).
                    // Thus, there is always at least ONE (often more) steps per dataTransfer element.
                    LOGGER.debug("Creating new dataTransfer: "+sourceParent.getFileSystem().getRoot().getURL().toString() + "  "+targetParent.getFileSystem().getRoot().getURL().toString());
                    mDtsJobStepAllocator.createNewDataTransfer(
                        sourceParent.getFileSystem().getRoot().getURL().toString(),
                        targetParent.getFileSystem().getRoot().getURL().toString(),
                        mMaxTotalByteSizePerStepLimit,
                        mMaxTotalFileNumPerStepLimit);

                    // Find out the number of files to be transferred for the given
                    // source and target pair. also create the directory structure
                    // on the target destination. Do this by:
                    //
                    // - Recurse/drill-down into each source to generate file-only transfers (DTUs)
                    // - While recursing, create the directory structure on destination.
                    // - For each file only transfer,
                    //   Call this.addFilesToTransfer() which:
                    //    Increments the following member vars:
                    //       mTotalSize += sourceFile.getContent().getSize();
                    //       mTotalFiles++;
                    //       mPerDataTransferTotalFiles++;
                    //    Calls jobStepAllocator.addDataTransferUnit which does the following:
                    //    - if the current step's constraints have been or would be exceeded
                    //      with the addition of the DTU to its list, then add the existing step
                    //      to the allocator's step list and re-create the current step, then:
                    //    - add the DTU to the allocator's current step 

                    final CreationFlagEnumeration.Enum creationFlag = ((MinxJobDescriptionType) jobDescription)
                        .getTransferRequirements().getCreationFlag();
                    prepare(sourceParent, targetParent, dataTransferIndex, creationFlag);

                    // Update the max number of total files to be transferred for the
                    // given source and/or target based on the number of files to be
                    // transferred between the source and target so we can use the
                    // values for finding out the number of FileSystemManagers we can cache.

                    // get the sourceParentRootStr and targetParentRootStr which
                    // represent the keys
                    final String sourceParentRootStr = sourceParent
                        .getFileSystem().getRoot().getURL().toString();
                    final String targetParentRootStr = targetParent
                        .getFileSystem().getRoot().getURL().toString();

                    // Update the JobDetails Map that holds the maximum number of files to be transferred
                    // from each Source using: Map<Source URI (String), number of files (Integer)>.
                    // We need this max number of files to be transferred for each source
                    // so we can intelligently decide how many parallel threads we need to use
                    // whenever we deal with the given source. knowing the number of parallel
                    // threads to use means knowing the number of FileSystemManager connections
                    // to cache. there's no point caching 4 FileSystemManagers for a given source
                    // if the number of files to be transferred from that source is only 1.
                    this.updateOrPutSourceTargetMaxTotalFilesToTransfer(
                            jobDetails.getSourceTargetMaxTotalFilesToTransfer(),
                            sourceParentRootStr, mPerDataTransferTotalFiles);
                    this.updateOrPutSourceTargetMaxTotalFilesToTransfer(
                            jobDetails.getSourceTargetMaxTotalFilesToTransfer(),
                            targetParentRootStr, mPerDataTransferTotalFiles);

                    dataTransferIndex++;
                }
                catch (final DtsJobCancelledException e) {
                    // TODO: handle DTS Job Cancel event
                    LOGGER.debug("Job has been cancelled.");
                }
                catch (final FileSystemException e) {
                    throw new JobScopingException(
                        "FileSystemException was thrown while accessing the remote files in the job scoping task.",
                        e);
                }
                catch (final JobScopingException e) {
                    // TODO Auto-generated catch block
                    throw e;
                }

                try {
                    // If the allocator's current step contains DTUs, then ensure
                    // that this is also added to the allocator's list of steps and
                    // close the allocation.
                    mDtsJobStepAllocator.closeNewDataTransfer();
                } catch (final FileNotFoundException e) {
                    throw new DtsException(e);
                }

            }
            LOGGER.info("Total number of files to be transferred: "
                + mTotalFiles);
            LOGGER.info("Total size of files to be transferred: " + mTotalSize
                + " bytes");
            LOGGER.debug("list of excluded files: ");
            for (final String excluded : mExcluded) {
                LOGGER.debug(" - " + excluded);
            }

            // update the jobDetails (a transfer object) from both member vars
            // in this class and using the allocator's fields
            jobDetails.setExcludedFiles(mExcluded);
            jobDetails.setTotalBytes(mTotalSize);
            jobDetails.setTotalFiles(mTotalFiles);

            // TODO: There will be no need to save all the jobSteps in the jobDetails
            // as these can now be read from disk when required, e.g. in the
            // FileCopyTask (since the jobDetails now saves the path to the 
            // job's unique directory where all the jobSteps are persisted )
            jobDetails.setJobSteps(mDtsJobStepAllocator.getAllocatedJobSteps());

            for (final DtsJobStep jobStep : mDtsJobStepAllocator.getAllocatedJobSteps()) {
                LOGGER.debug(jobStep);
                //jobStep.clearDataTransferUnits();
            }

        }
        finally {
            // Always, immediately close the file system manager so FileCopyTask will be able to use all of the
            // available connections. Define this in finally so that we do not
            // leave hanging connections if a scoping error occurs.
            if (fileSystemManager != null) {
                ((DefaultFileSystemManager) fileSystemManager).close();
            }
        }

        return jobDetails;
    }


    /**
     * Create a new unique directory as a sub dir of the DTS_JOB_STEP_DIRECTORY_KEY
     * for this particular job for writing step files.
     */
    private String createRootJobDir(String jobTag) {
        // lets create a new unique job directory to store all the job
        // step files
        String jobStepDir = System.getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY);
        if(jobStepDir == null){
            throw new IllegalStateException("Job step root directory does not exist: "
                    + jobStepDir);
        }
        final File jobStepRootDir = new File(jobStepDir );
        if (jobStepRootDir == null || !jobStepRootDir.exists() && !jobStepRootDir.isDirectory()) {
            throw new IllegalStateException("Job step root directory does not exist: " + jobStepDir);
        }
        // TODO: maybe we should create the directory after the jobTag !
        //String jobTab = jobExecution.getExecutionContext().getString(DtsBatchJobConstants.DTS_JOB_TAG);
        //final File rootJobDir = new File(jobStepRootDir, UUID.randomUUID().toString());
        final File rootJobDir = new File(jobStepRootDir, jobTag);

        if (!rootJobDir.mkdir()) {
            throw new IllegalStateException("Could not create unique job step directory");
        }
        //final File jobStepDir = new File(rootJobDir, "dtsJobSteps");
        //if (!jobStepDir.mkdir()) {
        //    throw new IllegalStateException("Could not create job step sub directory");
        //}
        return rootJobDir.getAbsolutePath();
    }



    /**
     * Prepares the remote destination for the files that will be transferred to it by pre-generating the folders where
     * files from the source will be copied. This method will also scope the source FileObject.
     *
     * @param sourceParent the source FileObject
     * @param destinationParent the destination FileObject
     * @param dataTransferIndex the DataTransfer index on how this source/destination's is listed in the
     *        JobDefinitionDocument
     * @param creationFlag a flag to say if the file on the destination should be overwritten or not
     * @throws DtsJobCancelledException if the job has been cancelled
     * @throws JobScopingException if any error arised while the job was being scoped
     */
    private void prepare(final FileObject sourceParent,
        final FileObject destinationParent, final int dataTransferIndex,
        final CreationFlagEnumeration.Enum creationFlag)
        throws DtsJobCancelledException, JobScopingException {
        if (mCancelled) {
            throw new DtsJobCancelledException();
        }

        try {
            // Handle the following cases...
            // source: /tmp/passwdDtsJobStepAllocator
            // copy to:
            // destination that does not exists: /tmp/passwd
            // destination directory that does not exists: /tmp/hello/
            //
            // destination file that exists: /tmp/passwd
            //
            // destination directory that exists: /tmp

            if (!sourceParent.getContent().getFile().isReadable()
                && !mCancelled) {
                mExcluded.add(sourceParent.getName().getFriendlyURI());
            }
            else if (sourceParent.getType().equals(FileType.FILE)
                && !mCancelled) {

                // check and see if the size of the file has exceeded the max size of files to be transferred
                // by a step
                // Don't we need to compare the mMaxTotalByteSizePerStepLimit against
                // a running (i.e. accrued) byte total for the step rather than just against
                // each separate file ?
                if (sourceParent.getContent().getSize() > mMaxTotalByteSizePerStepLimit) {
                    throw new JobScopingException("file: "
                        + sourceParent.getName()
                        + " too big. Try transferring files smaller than "
                        + mMaxTotalByteSizePerStepLimit + " bytes.");
                }

                if (!destinationParent.exists()) {
                    // file to IMAGINARY
                    // Note that we are not supporting a single file transfer to
                    // a non-existent directory
                    // Any destinationParent which had a "/" at the end of it's
                    // URI will not be handled

                    // TODO: should we handle the above case?

                    addFilesToTransfer(sourceParent, destinationParent,
                        dataTransferIndex);

                }
                else if (destinationParent.exists()
                    && destinationParent.getType().equals(FileType.FILE)) {
                    // File to File

                    if (creationFlag.equals(CreationFlagEnumeration.OVERWRITE)) {
                        addFilesToTransfer(sourceParent, destinationParent,
                            dataTransferIndex);
                    }
                    else {
                        mExcluded.add(sourceParent.getName().getFriendlyURI());
                    }
                }
                else {
                    // ... File to Dir

                    // create the new object
                    /*final String newFilePath = destinationParent.getURL()
                        + FileName.SEPARATOR
                        + sourceParent.getName().getBaseName();*/

                    // would be easier to resolve using existing FileObject and 
                    // specifying a relative path, e.g:
                    final FileObject destinationChild = destinationParent.resolveFile(sourceParent.getName().getBaseName());
                    /*final FileObject destinationChild = destinationParent
                        .getFileSystem().getFileSystemManager().resolveFile(
                            newFilePath,
                            destinationParent.getFileSystem()
                                .getFileSystemOptions());*/


                    // destinationChild.createFile();

                    if (destinationChild.exists()) {
                        if (creationFlag
                            .equals(CreationFlagEnumeration.OVERWRITE)) {
                            addFilesToTransfer(sourceParent, destinationChild,
                                dataTransferIndex);
                        }
                        else {
                            mExcluded.add(sourceParent.getName()
                                .getFriendlyURI());
                        }

                    }
                    else {
                        addFilesToTransfer(sourceParent, destinationChild,
                            dataTransferIndex);
                    }
                }
            }
            else if (sourceParent.getType().equals(FileType.FOLDER)
                && !mCancelled) {

                // .. Dir to File
                if(destinationParent.exists() && destinationParent.getType().equals(FileType.FILE))
                     throw new JobScopingException("mkdir: destination exists but is not a directory");
                    // or mExcluded.add(sourceParent.getName().getFriendlyURI());

                // .. Dir to Dir/IMAGINARY
                // create the new object
                // again may be easier to resolve using existing FileObject and 
                // specifying a relative path, e.g:
                final FileObject destinationChild = destinationParent.resolveFile(sourceParent.getName().getBaseName());
                /*final String newFolderPath = destinationParent.getURL()
                    + FileName.SEPARATOR + sourceParent.getName().getBaseName();
                final FileObject destinationChild = destinationParent
                    .getFileSystem().getFileSystemManager().resolveFile(
                        newFolderPath,
                        destinationParent.getFileSystem()
                            .getFileSystemOptions());*/

                if (!destinationChild.exists()) {
                    destinationChild.createFolder();
                }

                // get the children
                final FileObject[] sourceChildren = sourceParent.getChildren();

                // iterate through the children
                for (final FileObject sourceChild : sourceChildren) {
                    // recurse into the directory, or copy the file
                    prepare(sourceChild, destinationChild, dataTransferIndex,
                        creationFlag);
                }
            }
        }
        catch (final DtsJobCancelledException e) {
            throw e;
        }
        catch (final JobScopingException e) {
            throw e;
        }
        catch (final FileSystemException e) {
            throw new DtsException(e);
        }
        catch(final FileNotFoundException e){
            throw new DtsException(e); 
        }
    }

    /**
     * Sets the Encrypter.
     *
     * @param encrypter the Encrypter
     */
    public void setEncrypter(final Encrypter encrypter) {
        mEncrypter = encrypter;
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setMaxTotalByteSizePerStepLimit(
        final long maxTotalByteSizePerStepLimit) {
        mMaxTotalByteSizePerStepLimit = maxTotalByteSizePerStepLimit;
    }

    public void setMaxTotalFileNumPerStepLimit(
        final int maxTotalFileNumPerStepLimit) {
        mMaxTotalFileNumPerStepLimit = maxTotalFileNumPerStepLimit;
    }



    /**
     * This method is used to get the optimum number of connections that should be cached by the MaxStreamCounterTask
     * step on the FileSystemManagerCache. There's no point caching 5 connections so we could do 5 concurrent
     * connections when there's actually only 2 files to be transferred for the given source/target FileObject.
     *
     * @param sourceTargetMaxTotalFilesToTransfer a map that holds the maximum number of files that will be transferred
     *        for the given source/target Root FileObject URI string
     * @param parentRootStr the source or target Root FileObject URI string
     * @param perDataTransferTotalFiles the total number of files to be transferred for the given DataTransferElement
     * @param sourceTargetMaxTotalFilesToTransfer
     * @param parentRootStr
     * @param perDataTransferTotalFiles
     */
    private void updateOrPutSourceTargetMaxTotalFilesToTransfer(
        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer,
        final String parentRootStr,
        final int perDataTransferTotalFiles) {
        // if map does not contain key
        //   or
        // if map.get(key) < perDataTransferTotalFiles
        //   then
        // put/update key (initial-put or overwrite)
        if( !sourceTargetMaxTotalFilesToTransfer.containsKey(parentRootStr) ||
                sourceTargetMaxTotalFilesToTransfer.get(parentRootStr) < perDataTransferTotalFiles) {
            sourceTargetMaxTotalFilesToTransfer.put(parentRootStr, perDataTransferTotalFiles);
        }
    }
}
