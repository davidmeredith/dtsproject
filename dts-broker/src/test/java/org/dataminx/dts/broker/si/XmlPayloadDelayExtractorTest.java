/**
 *
 */
package org.dataminx.dts.broker.si;

import org.apache.xmlbeans.XmlObject;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.TestedObject;

/**
 * Tests for {@link XmlPayloadDelayExtractor} implementation.
 *
 * @author hnguyen
 */
@Test(groups="testng-unit-tests")
public class XmlPayloadDelayExtractorTest extends UnitilsTestNG {

    private static final String QUERY_STR="declare namespace dmi='http://schemas.ogf.org/dmi/2008/05/dmi';$this//dmi:StartNotBefore";
    private static final String EXPECTED = "2010-02-28T12:00:00";
    @TestedObject
    private XmlPayloadDelayExtractor mExtractor;

    @Test
    public void testExtractDelay() throws Exception {
        mExtractor = new XmlPayloadDelayExtractor(QUERY_STR);
        final Resource xml = new ClassPathResource("/job.xml");
        XmlObject targetXml = SubmitJobRequestDocument.Factory.parse(xml.getInputStream()).getSubmitJobRequest();
        String delay = mExtractor.extractDelay(targetXml);
        Assert.assertEquals(delay, EXPECTED);
    }
}