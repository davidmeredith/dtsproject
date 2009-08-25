/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.jms;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.ClassUtils;

/**
 * Deserialises an incoming Message payload into an object graph using a schema unmarshaller.
 *
 * @author Alex Arana
 */
public class DtsMessagePayloadTransformer extends XmlPayloadUnmarshallingTransformer {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsMessagePayloadTransformer.class);

    /**
     * Constructs a new instance of <code>DtsMessageTransformer</code> using the specified unmarshaller
     * to deserialize a given XML Stream to an Object graph.
     *
     * @param unmarshaller Unmarshaller used to deserialise the given {@link javax.xml.transform.Source}
     *        into an object graph.
     */
    public DtsMessagePayloadTransformer(final Unmarshaller unmarshaller) {
        super(unmarshaller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transformer
    public Object transformPayload(final Object payload) {
        if (ClassUtils.isAssignableValue(XmlObject.class, payload)) {
            return payload;
        }
        return super.transformPayload(payload);
    }
}
