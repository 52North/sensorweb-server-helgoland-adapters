package org.n52.proxy.harvest;

import org.n52.proxy.connector.utils.ServiceConstellation;

@FunctionalInterface
public interface HarvestingListener {
    void onResult(ServiceConstellation result);
}
