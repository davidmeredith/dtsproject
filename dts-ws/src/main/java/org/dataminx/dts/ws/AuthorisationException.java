package org.dataminx.dts.ws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * An exception that gets thrown if the user doesn't have the authority to perform the WS call.
 *
 * @author Gerson Galang
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class AuthorisationException extends RuntimeException {

    /**
     * Constructs an instance of {@link AuthorisationException}.
     */
    public AuthorisationException() {
        super();
    }

    /**
     * Constructs an instance of {@link AuthorisationException} given the specified message.
     *
     * @param msg the exception message
     */
    public AuthorisationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link AuthorisationException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public AuthorisationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link AuthorisationException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public AuthorisationException(Throwable cause) {
        super(cause);
    }
}
