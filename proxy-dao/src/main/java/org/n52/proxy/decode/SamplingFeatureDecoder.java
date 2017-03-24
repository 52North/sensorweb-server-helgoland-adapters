/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.proxy.decode;

import static com.google.common.collect.Lists.newArrayList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import static java.util.Collections.unmodifiableSet;
import java.util.List;
import java.util.Set;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.sampling.x20.SFSamplingFeatureDocument;
import static net.opengis.sampling.x20.SFSamplingFeatureDocument.Factory.newInstance;
import net.opengis.sampling.x20.SFSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import static org.apache.xmlbeans.XmlObject.Factory.parse;
import static org.n52.shetland.ogc.OGCConstants.UNKNOWN;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.CodeType;
import org.n52.shetland.ogc.gml.CodeWithAuthority;
import static org.n52.shetland.ogc.om.features.SfConstants.NS_SAMS;
import static org.n52.shetland.ogc.om.features.SfConstants.NS_SF;
import static org.n52.shetland.ogc.om.features.SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE;
import static org.n52.shetland.ogc.om.features.SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
import static org.n52.shetland.ogc.om.features.SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import static org.n52.shetland.ogc.sos.Sos2Constants.InsertObservationParams.observation;
import org.n52.svalbard.decode.AbstractGmlDecoderv321;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.exception.DecodingException;
import static org.n52.svalbard.util.CodingHelper.decoderKeysForElements;
import static org.n52.svalbard.util.XmlHelper.getNodeFromNodeList;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Jan Schulte
 */
public class SamplingFeatureDecoder extends AbstractGmlDecoderv321<XmlObject, AbstractFeature> {

    private static final Logger LOGGER = getLogger(SamplingFeatureDecoder.class);

    private static final Set<DecoderKey> DECODER_KEYS = decoderKeysForElements(NS_SF, SFSamplingFeatureDocument.class,
            SFSamplingFeatureType.class);

    @Override
    public AbstractFeature decode(XmlObject element) throws DecodingException {
        return parseSamplingFeature(((SFSamplingFeatureDocument) element).getSFSamplingFeature());
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return unmodifiableSet(DECODER_KEYS);
    }

    private AbstractFeature parseSamplingFeature(SFSamplingFeatureType sfSamplingFeature) throws DecodingException {
        final SamplingFeature sosFeat = new SamplingFeature(null, sfSamplingFeature.getId());
        parseAbstractFeatureType(sfSamplingFeature, sosFeat);
        sosFeat.setFeatureType(getFeatureType(sfSamplingFeature.getType()));
        sosFeat.setSampledFeatures(getSampledFeatures(sfSamplingFeature.getSampledFeatureArray()));
        sosFeat.setXml(getXmlDescription(sfSamplingFeature));
        sosFeat.setGeometry(getGeometry(sfSamplingFeature));
        checkTypeAndGeometry(sosFeat);
        sosFeat.setGmlId(sfSamplingFeature.getId());
        return sosFeat;
    }

    private String getFeatureType(final ReferenceType type) {
        if (type != null && type.getHref() != null && !type.getHref().isEmpty()) {
            return type.getHref();
        }
        return null;
    }

    private List<AbstractFeature> getSampledFeatures(FeaturePropertyType[] sampledFeatureArray)
            throws DecodingException {
        final List<AbstractFeature> sampledFeatures = newArrayList();
        for (FeaturePropertyType featurePropertyType : sampledFeatureArray) {
            sampledFeatures.addAll(getSampledFeatures(featurePropertyType));
        }
        return sampledFeatures;
    }

    private List<AbstractFeature> getSampledFeatures(final FeaturePropertyType sampledFeature)
            throws DecodingException {
        final List<AbstractFeature> sampledFeatures = new ArrayList<>(1);
        if (sampledFeature != null && !sampledFeature.isNil()) {
            // if xlink:href is set
            if (sampledFeature.getHref() != null && !sampledFeature.getHref().isEmpty()) {
                if (sampledFeature.getHref().startsWith("#")) {
                    sampledFeatures.add(new SamplingFeature(null, sampledFeature.getHref().replace("#", "")));
                } else {
                    final SamplingFeature sampFeat
                            = new SamplingFeature(new CodeWithAuthority(sampledFeature.getHref()));
                    if (sampledFeature.getTitle() != null && !sampledFeature.getTitle().isEmpty()) {
                        sampFeat.addName(new CodeType(sampledFeature.getTitle()));
                    }
                    sampledFeatures.add(sampFeat);
                }
            } else {
                XmlObject abstractFeature = null;
                if (sampledFeature.getAbstractFeature() != null) {
                    abstractFeature = sampledFeature.getAbstractFeature();
                } else if (sampledFeature.getDomNode().hasChildNodes()) {
                    try {
                        abstractFeature = parse(getNodeFromNodeList(sampledFeature.getDomNode().getChildNodes()));
                    } catch (XmlException xmle) {
                        throw new DecodingException("Error while parsing feature request!", xmle);
                    }
                }
                if (abstractFeature != null) {
                    final Object decodedObject = decodeXmlObject(abstractFeature);
                    if (decodedObject instanceof AbstractFeature) {
                        sampledFeatures.add((AbstractFeature) decodedObject);
                    }
                }
                throw new DecodingException(observation,
                        "The requested sampledFeature type is not supported by this service!");
            }
        }
        return sampledFeatures;
    }

    private String getXmlDescription(SFSamplingFeatureType sfSamplingFeature) {
        final SFSamplingFeatureDocument featureDoc
                = newInstance(getXmlOptions());
        featureDoc.setSFSamplingFeature(sfSamplingFeature);
        return featureDoc.xmlText(getXmlOptions());
    }

    private void checkTypeAndGeometry(final SamplingFeature sosFeat) throws DecodingException {
        final String featTypeForGeometry = getFeatTypeForGeometry(sosFeat.getGeometry());
        if (sosFeat.getFeatureType() == null) {
            sosFeat.setFeatureType(featTypeForGeometry);
        } else {
            if (!featTypeForGeometry.equals(sosFeat.getFeatureType())) {
                throw new DecodingException("The requested observation is invalid! The featureOfInterest type "
                        + "does not comply with the defined type (%s)!", sosFeat.getFeatureType());
            }
        }
    }

    private String getFeatTypeForGeometry(final Geometry geometry) {
        if (geometry instanceof Point) {
            return SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
        } else if (geometry instanceof LineString) {
            return SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE;
        } else if (geometry instanceof Polygon) {
            return SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE;
        }
        return UNKNOWN;
    }

    private Geometry getGeometry(SFSamplingFeatureType sfSamplingFeature) throws DecodingException {
        XmlObject[] shapes = sfSamplingFeature.selectChildren(NS_SAMS, "shape");
        if (shapes.length == 1) {
            try {
                ShapeDocument shapeDoc = (ShapeDocument) parse(shapes[0].getDomNode());
                Object decodedObject = decodeXmlElement(shapeDoc.getShape().getAbstractGeometry());
                if (decodedObject instanceof Geometry) {
                    return (Geometry) decodedObject;
                }
            } catch (XmlException ex) {
                throw new DecodingException(observation,
                        "The requested geometry type of featureOfInterest is not supported by this service!");
            }
        }
        throw new DecodingException(observation,
                "The requested geometry type of featureOfInterest is of wrong length!");
    }

}
