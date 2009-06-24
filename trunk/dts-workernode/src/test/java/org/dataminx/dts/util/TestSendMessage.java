/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.util;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.dataminx.dts.jms.JobSubmitQueueSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

/**
 * Test that a DTS job is launched when a JMS message is posted on the DTS Job Submission queue.
 *
 * @author Alex Arana
 */
@ContextConfiguration(locations = { "/test-context.xml", "/activemq/jms-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSendMessage {
    @Autowired
    private JobSubmitQueueSender mJmsQueueSender;

    @Test
    public void submitDtsJob() throws Exception {
        final File file = new ClassPathResource("minx-dts.xml").getFile();
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        final DocumentBuilder builder = docFactory.newDocumentBuilder();
        final Document dtsJob = builder.parse(file);

        //logger.info(client.submitJob(dtsJob));
        mJmsQueueSender.doSend(dtsJob);
    }
}