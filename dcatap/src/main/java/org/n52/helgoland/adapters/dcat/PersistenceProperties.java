package org.n52.helgoland.adapters.dcat;

import com.google.common.base.StandardSystemProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Configuration
@ConfigurationProperties("service.dcat.persistence")
public class PersistenceProperties {
    private boolean enabled = true;

    private Path path = Paths.get(Optional.ofNullable(StandardSystemProperty.JAVA_IO_TMPDIR.value())
                                          .orElse("/tmp"), "catalog.rdf");

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
