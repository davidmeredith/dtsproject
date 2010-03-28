package org.dataminx.dts.batch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.xmlbeans.XmlException;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.dataminx.dts.common.DtsConstants;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A unit test for the VfsMixedFilesJobPartitioningStrategy class.
 * 
 * @author Gerson Galang
 */
@Test(groups = { "unit-test" })
public class VfsMixedFilesJobPartitioningStrategyTest {

    private VfsMixedFilesJobPartitioningStrategy mPartitioningStrategy;
    private DtsVfsUtil mDtsVfsUtil;

    private static final long FILE_SIZE_10MB = 10485760;

    @BeforeClass
    public void init() {
        mDtsVfsUtil = mock(DtsVfsUtil.class);
        System.setProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY, "/tmp");
    }

    @Test(groups = { "local-file-transfer-test" })
    public void testPartitionWith1File() throws IOException, XmlException, JobScopingException {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);
        mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);

        final FileSystemManager fileSystemManager = DtsVfsUtil.createNewFsManager(false, false, false, false, true,
                true, false, "/tmp");

        when(mDtsVfsUtil.createNewFsManager()).thenReturn((DefaultFileSystemManager) fileSystemManager);
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(dtsJob.getJobDefinition(), UUID
                .randomUUID().toString());

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), 1);
        assertEquals(jobDetails.getTotalFiles(), 1);
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(DtsConstants.FILE_ROOT_PROTOCOL)
                .intValue(), 1);
    }

    @Test(groups = { "local-file-transfer-test" }, expectedExceptions = DtsException.class)
    public void testNegativeMaxTotalFileNumPerStepLimit() throws IOException, XmlException, JobScopingException {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);
        mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(-1);
        mPartitioningStrategy.partitionTheJob(dtsJob.getJobDefinition(), UUID.randomUUID().toString());
    }

    @Test(groups = { "local-file-transfer-test" }, expectedExceptions = DtsException.class)
    public void testNegativeMaxTotalByteSizePerStepLimit() throws IOException, XmlException, JobScopingException {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);
        mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(-1);
        mPartitioningStrategy.partitionTheJob(dtsJob.getJobDefinition(), UUID.randomUUID().toString());
    }

    @Test(groups = { "local-file-transfer-test" }, expectedExceptions = IllegalArgumentException.class)
    public void testNullJobDefinitionParameter() throws JobScopingException {
        mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.partitionTheJob(null, UUID.randomUUID().toString());
    }

    @Test(groups = { "local-file-transfer-test" }, expectedExceptions = IllegalArgumentException.class)
    public void testNullJobResourceKeyParameter() throws IOException, XmlException, JobScopingException {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);
        mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.partitionTheJob(dtsJob.getJobDefinition(), null);
    }

}