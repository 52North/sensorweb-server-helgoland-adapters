package org.n52.proxy.connector.utils;

import org.n52.janmayen.exception.LocationHintException;

/**
 * @author Jan Schulte
 */
public class ProxyException extends LocationHintException {
    private static final long serialVersionUID = -8654668015391742318L;

    public ProxyException(String message, Object... args) {
        super(message, args);
    }

}
