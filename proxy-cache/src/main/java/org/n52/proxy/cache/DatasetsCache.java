package org.n52.proxy.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DatasetsCache {
    private Map<String, byte[]> requestToResponse = new HashMap<>();

    public void put(String path, Map<String, String[]> queryParams, byte[] responseBody) {
        requestToResponse.put(requestParamsToString(path, queryParams), responseBody);
    }

    public boolean isResponseCached(String path, Map<String, String[]> queryParams) {
        return requestToResponse.containsKey(requestParamsToString(path, queryParams));
    }

    //todo sort
    //todo sort different params in one param
    private String requestParamsToString(String path, Map<String, String[]> queryParams) {
        StringBuilder builder = new StringBuilder();
        builder.append(path);
        for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
            builder.append(",")
                    .append(entry.getKey())
                    .append("=")
                    .append(Arrays.toString(entry.getValue()));
        }
        return builder.toString();
    }
}
