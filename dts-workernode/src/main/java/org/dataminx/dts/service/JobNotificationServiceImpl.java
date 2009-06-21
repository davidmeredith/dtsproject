/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataminx.dts.service;

import org.dataminx.dts.jms.JobEventQueueSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the DTS Worker Node's {@link JobNotificationService}.
 *
 * @author Alex Arana
 */
@Service("jobNotificationService")
@Scope("singleton")
public class JobNotificationServiceImpl implements JobNotificationService {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(JobNotificationServiceImpl.class);

    /**
     * A reference to the Job Event Queue sender object.
     */
    @Autowired
    private JobEventQueueSender mJobEventQueueSender;

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyJobError(String jobId, String message, Throwable error) {
        // TODO Implement this method
        sendJmsMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyJobProgress(String jobId, String message) {
        // TODO Implement this method
        sendJmsMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyJobStatus(String jobId, String message) {
        // TODO Implement this method
        sendJmsMessage(message);
    }

    /**
     * Sends a JMS message to the JMS DTS Job Event queue.
     *
     * @param message Message to send
     */
    protected void sendJmsMessage(final Object message) {
        LOG.debug("Posting a message to the DTS Job Event Queue: " + message);
        mJobEventQueueSender.doSend(message);
    }
}