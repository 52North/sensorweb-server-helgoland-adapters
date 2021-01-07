package org.n52.helgoland.adapters.dcat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("service.dcat.catalog")
public class CatalogProperties {
    private String language;
    private String license;
    private String homepage = "https://52north.org";
    private String publisher = "https://52north.org";
    private String description;
    private String title;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        UriUtil.requireAbsoluteURI("catalog homepage", homepage);
        this.homepage = homepage;
    }

    public String getPublisher() {

        return publisher;
    }

    public void setPublisher(String publisher) {
        UriUtil.requireAbsoluteURI("catalog publisher", publisher);
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
