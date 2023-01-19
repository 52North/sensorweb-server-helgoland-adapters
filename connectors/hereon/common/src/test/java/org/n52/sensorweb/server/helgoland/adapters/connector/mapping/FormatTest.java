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
package org.n52.sensorweb.server.helgoland.adapters.connector.mapping;

import org.n52.sensorweb.server.helgoland.adapters.connector.response.Attributes;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormatTest {

    private static final Attributes attributes = createAttributes();

    @Test
    public void test_feature_name_separator() {
        String fields = "platform,feature_of_interest,transect";
        String formattedString = new Format().withName("name").withFormat(new FormatFormat().withSeparator("_"))
                .withFields(fields).getFormattedString(attributes);
        Assertions.assertEquals("PLATFORM_FEATURE_TRANSECT", formattedString);
    }

    @Test
    public void test_feature_description_separator() {
        String formattedString =
                new Format().withName("description")
                        .withFormat(new FormatFormat().withSeparator(";").withPrefix("Feature for ")
                                .withOptionals(getFeatureOptionals())).getFormattedString(attributes);
        Assertions.assertEquals("Feature for Platform: PLATFORM;Feature: FEATURE;Transect: TRANSECT", formattedString);
    }

    @Test
    public void test_feature_description_separator_no_transect() {
        String formattedString = new Format().withName("description")
                .withFormat(new FormatFormat().withSeparator(";").withPrefix("Feature for ")
                        .withOptionals(getFeatureOptionals())).getFormattedString(createAttributes(false));
        Assertions.assertEquals("Feature for Platform: PLATFORM;Feature: FEATURE", formattedString);
    }

    @Test
    public void test_datastream_name_separator() {
        String fields = "platform,para_standard_name,sensor,platform,feature_of_interest,transect";
        String formattedString = new Format().withName("name").withFormat(new FormatFormat().withSeparator("_"))
                .withFields(fields).getFormattedString(attributes);
        Assertions.assertEquals("PLATFORM_PARAMETER_SENSOR_PLATFORM_FEATURE_TRANSECT", formattedString);
    }

    @Test
    public void test_datastream_description_separator() {
        String formattedString =
                new Format().withName("description")
                        .withFormat(new FormatFormat().withSeparator(";").withPrefix("Datastream for ")
                                .withOptionals(getDatastreamOptionals())).getFormattedString(attributes);
        Assertions.assertEquals(
                "Datastream for Platform: PLATFORM;Parameter: PARAMETER;Sensor: SENSOR;Feature: PLATFORM_FEATURE_TRANSECT",
                formattedString);
    }

    @Test
    public void test_datastream_description_separator_no_transect() {
        String formattedString = new Format().withName("description")
                .withFormat(new FormatFormat().withSeparator(";").withPrefix("Datastream for ")
                        .withOptionals(getDatastreamOptionals())).getFormattedString(createAttributes(false));
        Assertions.assertEquals(
                "Datastream for Platform: PLATFORM;Parameter: PARAMETER;Sensor: SENSOR;Feature: PLATFORM_FEATURE",
                formattedString);
    }

    private List<Optional> getFeatureOptionals() {
        List<Optional> optionals = new LinkedList<>();
        optionals.add(new Optional().withField("platform").withValue("Platform: "));
        optionals.add(new Optional().withField("feature_of_interest").withValue("Feature: "));
        optionals.add(new Optional().withField("transect").withValue("Transect: "));
        return optionals;
    }

    private List<Optional> getDatastreamOptionals() {
        List<Optional> optionals = new LinkedList<>();
        optionals.add(new Optional().withField("platform").withValue("Platform: "));
        optionals.add(new Optional().withField("para_standard_name").withValue("Parameter: "));
        optionals.add(new Optional().withField("sensor").withValue("Sensor: "));
        optionals.add(new Optional().withField("platform,feature_of_interest,transect").withValue("Feature: ")
                .withSeparator("_"));
        return optionals;
    }

    private static Attributes createAttributes() {
        return createAttributes(true);
    }

    private static Attributes createAttributes(boolean transect) {
        Attributes attributes = new Attributes();
        attributes.withAdditionalProperty("platform", "PLATFORM");
        attributes.withAdditionalProperty("para_standard_name", "PARAMETER");
        attributes.withAdditionalProperty("sensor", "SENSOR");
        attributes.withAdditionalProperty("feature_of_interest", "FEATURE");
        if (transect) {
            attributes.withAdditionalProperty("transect", "TRANSECT");
        }
        return attributes;
    }

}
