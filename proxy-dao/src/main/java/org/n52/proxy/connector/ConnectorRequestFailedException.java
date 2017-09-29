package org.n52.proxy.connector;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ConnectorRequestFailedException extends RuntimeException {
    private static final long serialVersionUID = 627994337294485258L;

    public ConnectorRequestFailedException() {
    }

    public ConnectorRequestFailedException(String message) {
        super(message);
    }

    public ConnectorRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectorRequestFailedException(Throwable cause) {
        super(cause);
    }

    protected ConnectorRequestFailedException(String message, Throwable cause, boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
