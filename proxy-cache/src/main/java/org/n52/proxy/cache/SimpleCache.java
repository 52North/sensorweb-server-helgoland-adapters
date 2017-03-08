package org.n52.proxy.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache {
    private Map<String, byte[]> requestToResponse = new ConcurrentHashMap<>();

    public void put(String path, Map<String, String[]> queryParams, byte[] responseBody) {
        requestToResponse.put(requestParamsToString(path, queryParams), responseBody);
    }

    public boolean isResponseCached(String path, Map<String, String[]> queryParams) {
        return requestToResponse.containsKey(requestParamsToString(path, queryParams));
    }

    private String requestParamsToString(String path, Map<String, String[]> queryParams) {
        StringBuilder builder = new StringBuilder();
        builder.append(path);
        SortedMap<String, String[]> sortedParams = new TreeMap<>(queryParams);
        for (Map.Entry<String, String[]> entry : sortedParams.entrySet()) {
            builder.append(",")
                    .append(entry.getKey())
                    .append("=")
                    .append(Arrays.toString(entry.getValue()));
        }
        return builder.toString();
    }
}
