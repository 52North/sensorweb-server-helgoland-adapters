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
package org.n52.sensorweb.server.helgoland.adapters.config;

public class Credentials {

    private String username;
    private String password;
    private String tokenUrl;

    public Credentials() {
        super();
    }

    public Credentials(String username, String password) {
        this(username, password, null);
    }

    public Credentials(String username, String password, String tokenUrl) {
        super();
        this.username = username;
        this.password = password;
        this.tokenUrl = tokenUrl;
    }

    public String getUsername() {
        return username;
    }

    public Credentials setUsername(String username) {
        this.username = username;
        return this;
    }

    public boolean isSetUsername() {
        return getUsername() != null && !getUsername().isEmpty();
    }

    public String getPassword() {
        return password;
    }

    public Credentials setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isSetPassword() {
        return getPassword() != null && !getPassword().isEmpty();
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public Credentials setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
        return this;
    }

    public boolean isSetTokenUrl() {
        return getTokenUrl() != null && !getTokenUrl().isEmpty();
    }

    public boolean isSetCredentials() {
        return isSetUsername() && isSetPassword();
    }

}
