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
