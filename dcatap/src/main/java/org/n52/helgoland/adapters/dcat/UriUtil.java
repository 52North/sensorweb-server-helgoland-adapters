package org.n52.helgoland.adapters.dcat;

import java.net.URI;
import java.net.URISyntaxException;

public final class UriUtil {
    private UriUtil() {}

    public static String createMailURI(String mail) {
        return mail.startsWith("mailto:") ? mail : "mailto:" + mail;
    }

    public static String createTelURI(String number) {
        return number.startsWith("tel:") ? number : "tel:" + number;
    }

    public static void requireAbsoluteURI(String name, String url) {
        if (!isAbsoluteURI(url)) {
            throw new IllegalArgumentException(name + " has to be a absolute URI");
        }
    }

    public static boolean isAbsoluteURI(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            return new URI(url).isAbsolute();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
