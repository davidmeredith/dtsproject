/**
 *
 */
package org.dataminx.dts.wn;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dataminx.dts.wn.service.WorkerNodeJobPollable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * An {@link JobOperator} implementation that decorates {@link SimpleJobOperator} with
 * DTS WorkNode specific control logic of accepting new batch job.
 *
 * @author hnguyen
 */
public class WorkerNodeManager implements JobOperator, WorkerNodeJobPollable, InitializingBean, DisposableBean {

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(WorkerNodeManager.class);

    /** an JobOperator implementation such as {link SimpleJobOperator} */
    private JobOperator mOperator;

    /** max number of batch job that can be run by this WorkNodeManager */
    private int maxBatchJobNumer;

    private static final String LOCK_FILE_NAME=".lock";

    private static final String WORKER_NODE_DIR_NAME="dts-workernode";


    public void setmOperator(JobOperator mOperator) {
        this.mOperator = mOperator;
    }

    public void setMaxBatchJobNumer(int maxBatchJobNumer) {
        this.maxBatchJobNumer = maxBatchJobNumer;
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#getExecutions(long)
     */
    public List<Long> getExecutions(long instanceId) throws NoSuchJobInstanceException {
        return mOperator.getExecutions(instanceId);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#getJobInstances(java.lang.String, int, int)
     */
    public List<Long> getJobInstances(String jobName, int start, int count)
        throws NoSuchJobException {
        return mOperator.getJobInstances(jobName, start, count);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#getJobNames()
     */
    public Set<String> getJobNames() {
        return mOperator.getJobNames();
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#getParameters(long)
     */
    public String getParameters(long executionId) throws NoSuchJobExecutionException {
        return mOperator.getParameters(executionId);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#getRunningExecutions(java.lang.String)
     */
    public Set<Long> getRunningExecutions(String jobName) throws NoSuchJobException {
        return mOperator.getRunningExecutions(jobName);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#getStepExecutionSummaries(long)
     */
    public Map<Long, String> getStepExecutionSummaries(long executionId)
        throws NoSuchJobExecutionException {
        return mOperator.getStepExecutionSummaries(executionId);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#getSummary(long)
     */
    public String getSummary(long executionId) throws NoSuchJobExecutionException {
        return mOperator.getSummary(executionId);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#restart(long)
     */
    public Long restart(long executionId) throws JobInstanceAlreadyCompleteException,
        NoSuchJobExecutionException, NoSuchJobException, JobRestartException {
        return mOperator.restart(executionId);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#start(java.lang.String, java.lang.String)
     */
    public Long start(String jobName, String parameters) throws NoSuchJobException,
        JobInstanceAlreadyExistsException {
        return mOperator.start(jobName, parameters);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#startNextInstance(java.lang.String)
     */
    public Long startNextInstance(String jobName) throws NoSuchJobException,
        JobParametersNotFoundException, JobRestartException, JobExecutionAlreadyRunningException,
        JobInstanceAlreadyCompleteException {
        return mOperator.startNextInstance(jobName);
    }

    /* (non-Javadoc)
     * @see org.springframework.batch.core.launch.JobOperator#stop(long)
     */
    public boolean stop(long executionId) throws NoSuchJobExecutionException,
        JobExecutionNotRunningException {
        return mOperator.stop(executionId);
    }

    /**
     * Bases on number of current running job and max number of batch job allowed to
     * determine whether this manager can poll more message from the JMS Queue.
     */
    public synchronized boolean canPoll() {
        final int runningJobs = runningJobs();
        if (runningJobs < maxBatchJobNumer) {
            return true;
        }
        return false;
    }

    private synchronized int runningJobs() {
        int runningJobs=0;
        for (String jobName:mOperator.getJobNames()) {
            try {
                if (mOperator.getRunningExecutions(jobName).size()>=1) {
                    runningJobs++;
                }
            }
            catch (NoSuchJobException ex) {
                LOG.debug("Ignore job " + jobName,ex);
            }
        }
        return runningJobs;
    }

    /**
     *
     */
    public void afterPropertiesSet() throws Exception {
//        File userHomeLockFile = new File(SystemUtils.USER_HOME + File.pathSeparator + WORKER_NODE_DIR_NAME +
//                                            File.pathSeparator+ LOCK_FILE_NAME);
//        for (String jobName:mOperator.getJobNames()) {
//            for (Long execId:mOperator.getExecutions(jobName)) {
//                mOperator.stop(execId); // because the job status still be STARTED
//                mOperator.restart(execId);
//            }
//        }
        LOG.debug("afterPropertiesSet()");
    }

    /**
     * Manages graceful shutdown of the workernode by attempting to remove the lock file. The absence
     * of lock file indicates a graceful,managed shutdown.
     */
    public void destroy() throws Exception {
        // TODO: a graceful shutdown should stop all the running jobs cleanly as well.
        // remove lock file if graceful shutdown
//        File userHomeLockFile = new File(SystemUtils.USER_HOME + File.pathSeparator + WORKER_NODE_DIR_NAME +
//            File.pathSeparator+ LOCK_FILE_NAME);
//        if (!userHomeLockFile.delete()) {
//            userHomeLockFile.deleteOnExit();
//        }
        LOG.debug("destroy()");
    }
}