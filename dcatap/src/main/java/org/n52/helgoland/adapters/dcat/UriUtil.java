/*
 * Copyright (C) 2015-2022 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.helgoland.adapters.dcat;

import java.net.URI;
import java.net.URISyntaxException;

public final class UriUtil {

    private static final String MAIL_TO = "mailto:";
    private static final String TEL = "tel:";

    private UriUtil() {
    }

    public static String createMailURI(String mail) {
        return mail.startsWith(MAIL_TO) ? mail : MAIL_TO + mail;
    }

    public static String createTelURI(String number) {
        return number.startsWith(TEL) ? number : TEL + number;
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
