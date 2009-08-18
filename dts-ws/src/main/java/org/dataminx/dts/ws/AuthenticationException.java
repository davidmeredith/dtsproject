package org.dataminx.dts.ws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * An exception that gets thrown if the user has failed to authenticate on the WS call.
 *
 * @author Gerson Galang
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class AuthenticationException  extends RuntimeException {

    /**
     * Constructs an instance of {@link AuthenticationException}.
     */
    public AuthenticationException() {
        super();
    }

    /**
     * Constructs an instance of {@link AuthenticationException} given the specified message.
     *
     * @param msg the exception message
     */
    public AuthenticationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link AuthenticationException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public AuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link AuthenticationException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
