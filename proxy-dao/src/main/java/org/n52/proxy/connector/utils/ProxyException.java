package org.n52.proxy.connector.utils;

import org.n52.janmayen.exception.LocationHintException;

/**
 * @author Jan Schulte
 */
public class ProxyException extends LocationHintException {

    public ProxyException(String message, Object... args) {
        super(message, args);
    }

}
