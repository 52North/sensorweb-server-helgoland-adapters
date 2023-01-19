/*
 * Copyright (C) 2015-2023 52°North Spatial Information Research GmbH
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
package org.n52.sensorweb.server.helgoland.adapters.request;

import java.util.Map;

import org.n52.sensorweb.server.helgoland.adapters.web.request.AbstractGetRequest;
import org.n52.shetland.ogc.ows.OWSConstants;
import org.n52.shetland.ogc.sos.Sos2Constants;

public class GetCapabilitiesGetRequest extends AbstractGetRequest {

    private Boolean returnHumanReadableName;

    @Override
    public Map<String, String> getQueryParameters() {
        Map<String, String> parameter = createMap();
        parameter.put(OWSConstants.GetCapabilitiesParams.service.name(), Sos2Constants.SOS);
        parameter.put(OWSConstants.GetCapabilitiesParams.request.name(),
                OWSConstants.Operations.GetCapabilities.name());
        if (returnHumanReadableName != null) {
            parameter.put("returnHumanReadableIdentifier", returnHumanReadableName.toString());
        }
        return parameter;
    }

    @Override
    public String getPath() {
        return "";
    }

    public GetCapabilitiesGetRequest withReturnHumanReadableName(Boolean returnHumanReadableName) {
        this.returnHumanReadableName = returnHumanReadableName;
        return this;
    }

}
