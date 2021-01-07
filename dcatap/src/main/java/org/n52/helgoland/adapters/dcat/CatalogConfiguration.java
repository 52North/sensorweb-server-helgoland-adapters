package org.n52.helgoland.adapters.dcat;

import org.apache.jena.rdf.model.Model;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;

@ComponentScan
@Configuration(proxyBeanMethods = false)
public class CatalogConfiguration implements WebMvcConfigurer {

    @Bean
    public GeoJsonWriter geoJsonWriter() {
        GeoJsonWriter writer = new GeoJsonWriter();
        writer.setEncodeCRS(true);
        return writer;
    }

    @Bean
    public WKTWriter wktWriter() {
        return new WKTWriter();
    }

    @Bean
    public ModelPersistence modelPersistence(PersistenceProperties persistenceProperties) {
        if (persistenceProperties.isEnabled()) {
            return new FileModelPersistence(persistenceProperties.getPath());
        } else {
            return new NoopModelPersistence();
        }

    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ModelHttpMessageConverter());
    }

    private static class NoopModelPersistence implements ModelPersistence {
        @Override
        public Optional<Model> read() {
            return Optional.empty();
        }

        @Override
        public void write(Model model) {
            // noop
        }
    }
}
