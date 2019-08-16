package org.n52.proxy.db;

import org.n52.series.db.AnnotationBasedDataRepositoryFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ProxyTestDataRepositoryFactory extends AnnotationBasedDataRepositoryFactory {

    public ProxyTestDataRepositoryFactory(ApplicationContext appContext) {
        super(appContext);
    }

}
