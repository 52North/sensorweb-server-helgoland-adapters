/*
 * Copyright (C) 2015-2022 52°North Spatial Information Research GmbH
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
package org.n52.sensorweb.server.helgoland.adapters.connector;

import org.n52.shetland.arcgis.service.feature.FeatureServiceConstants;

public interface HereonConstants extends FeatureServiceConstants {

    String HEREON = "Hereon";

    interface Fields {
        String OBJECT_ID = "objectid";
        // String METADATA_ID = "metadata_id";
    }

    interface MetadataFields extends Fields {
        String METADATA_ID = "metadata_id";
        String PARA_STANDARD_NAME = "para_standard_name";
        String PARA_STANDARD_URL = "para_standard_name_url";
        String RESP_SCIEN_LASTNAME = "resp_scien_lastname";
        String RESP_SCIEN_LFIRSTNAME = "resp_scien_firstname";
        String RESP_SCIEN_ORCID = "resp_scien_orcid";
        String RESP_SCIEN_EMAIL = "resp_scien_email";
        String RESP_SCIEN_INSTITUTION = "resp_scien_institution";
        String RESP_SCIEN_INSTITUTION_FOR_IDEN = "resp_scien_institution_ror_iden";
        String SENSOR_URL = "sensor_url";
        String LICENSE = "license";
        String LINK_TO_METADATA = "link_to_metadata";
        String FEATURE_OF_INTEREST = "feature_of_interest";
        String NAME_START_HARBOR = "name_start_harbor";
        String NAME_END_HARBOR = "name_end_harbor";
        String DOI_PID = "doi_pid";
        String URL_MAINTENANCE_INFO = "url_maintenance_info";
        String URL_LAB_INFO = "url_lab_info";
        String MEASURE_VAL_LAB = "measure_val_lab";
        String UNIT_LAB = "unit_lab";
        String TIMESTAMP_SAMPLE_LAB = "timestamp_sample_lab";
        String TIMESTAMP_SMEASURE_LAB = "timestamp_measure_lab";
        String TIME_END_HARBOR = "time_end_harbor";
        String TIME_START_HARBOR = "time_start_harbor";
    }

    interface DataFields extends Fields {
        String GLOBAL_ID = "globalid";
        String PLATFORM = "PLATFORM";
        String MEASURE_VAL = "MEASURE_VAL";
        String UNIT = "UNIT";
        String INT_PARA_NAME = "INT_PARA_NAME";
        String PARAMETER = "PARAMETER";
        String DATE_TIME = "DATE_TIME";
        String LAT = "LAT";
        String LON = "LON";
        String WATER_DEPTH = "WATER_DEPTH";
        String SENSOR = "SENSOR";
        String QUALITY_FLAG = "QUALITY_FLAG";
        String REASON_QUALITY_FLAG = "REASON_QUALITY_FLAG";
        String INFOFLAG = "INFOFLAG";
        String AUTO_QUALITY_TEST_PARAM = "AUTO_QUALITY_TEST_PARAM";
        String STATUS_FLAG = "STATUS_FLAG";
        String MAN_QUALITY_TEST_PARAM = "MAN_QUALITY_TEST_PARAM";
        String FLAG_MAN_QUALITY_CONTROL = "FLAG_MAN_QUALITY_CONTROL";
        String FLAG_DATA_PUBLIC = "FLAG_DATA_PUBLIC";
        String TRANSECT = "TRANSECT";
        String ERROR_VAL = "ERROR_VAL";
        String METHOD_ERR_VAL = "METHOD_ERR_VAL";
        String STD_DEVIATION = "STD_DEVIATION";
        String VARIANCE = "VARIANCE";
        String MIN = "MIN";
        String MAX = "MAX";
        String NUMBER_OF_VALUES = "NUMBER_OF_VALUES";
        String PRIMARYID = "PRIMARYID";
        String RAW_MEASURE_VAL = "RAW_MEASURE_VAL";
        String ET_DATE_CREATED = "ET_DATE_CREATED";
        String ET_DATE_MODIFIED = "ET_DATE_MODIFIED";
        String ET_USER_CREATED = "ET_USER_CREATED";
        String ET_USER_MODIFIED = "ET_USER_MODIFIED";
        String ET_IDENTIFIER = "ET_IDENTIFIER";
        String ET_UUID = "ET_UUID";
    }

}
