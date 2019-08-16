package org.n52.proxy.connector.utils;

import org.n52.janmayen.http.MediaTypes;

public class ServiceMetadata {

    private final String metadata;

    private final String format;

    public ServiceMetadata(String metadata, String format) {
       this.metadata = metadata;
       this.format = format;
    }

    public String getMetadata() {
        return metadata;
    }

    public boolean isSetMetadata() {
        return getMetadata() != null && !getMetadata().isEmpty();
    }

    public String getFormat() {
        return format;
    }

    public boolean isSetFormat() {
        return getFormat() != null && !getFormat().isEmpty();
    }

    public static ServiceMetadata createXmlServiceMetadata(String metadata) {
        return new ServiceMetadata(metadata,MediaTypes.APPLICATION_XML.toString());
    }

    public static ServiceMetadata createJsonServiceMetadata(String metadata) {
        return new ServiceMetadata(metadata, MediaTypes.APPLICATION_JSON.toString());
    }
}
