package org.n52.helgoland.adapters.dcat;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD4;
import org.apache.jena.vocabulary.XSD;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.janmayen.Optionals;
import org.n52.janmayen.i18n.MultilingualString;
import org.n52.sensorweb.server.db.repositories.core.ServiceRepository;
import org.n52.sensorweb.server.helgoland.adapters.connector.utils.ServiceConstellation;
import org.n52.sensorweb.server.helgoland.adapters.harvest.HarvestingListener;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.ows.OwsAddress;
import org.n52.shetland.ogc.ows.OwsAllowedValues;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsCode;
import org.n52.shetland.ogc.ows.OwsDomain;
import org.n52.shetland.ogc.ows.OwsOnlineResource;
import org.n52.shetland.ogc.ows.OwsOperationsMetadata;
import org.n52.shetland.ogc.ows.OwsPossibleValues;
import org.n52.shetland.ogc.ows.OwsResponsibleParty;
import org.n52.shetland.ogc.ows.OwsServiceIdentification;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
import org.n52.shetland.ogc.ows.OwsValue;
import org.n52.shetland.ogc.ows.OwsValueRestriction;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.rdf.RDFDataTypes;
import org.n52.shetland.rdf.vocabulary.LOCN;
import org.n52.shetland.rdf.vocabulary.TIME;
import org.n52.shetland.util.ReferencedEnvelope;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.decode.exception.NoDecoderForKeyException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

@Service
public class CatalogTransformer implements HarvestingListener, CatalogProvider {
    private static final String API_DESCRIPTION = "http://52north.github.io/sensorweb-server-helgoland/version_3.x/api.html";
    private static final Logger LOG = LoggerFactory.getLogger(CatalogTransformer.class);
    private static final String PREFIX_XSD = "xsd";
    private static final String PREFIX_RDF = "rdf";
    private static final String PREFIX_DCAT = "dcat";
    private static final String PREFIX_DCT = "dct";
    private static final String PREFIX_FOAF = "foaf";
    private static final String PREFIX_VCARD = "vcard";
    private static final String PREFIX_TIME = "time";
    private static final String LANGUAGE_PARAMETER = "language";
    private static final String PREFIX_LOCN = "locn";
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final GeoJsonWriter geoJsonWriter;
    private final DecoderRepository decoderRepository;
    private final CatalogProperties catalogProperties;
    private final ModelPersistence modelPersistence;
    private final ServiceRepository serviceRepository;
    private final String externalUrl;
    private Model model;
    private Resource catalog;

    @Autowired
    public CatalogTransformer(GeoJsonWriter geoJsonWriter,
                              DecoderRepository decoderRepository,
                              CatalogProperties catalogProperties,
                              ModelPersistence modelPersistence,
                              ServiceRepository serviceRepository,
                              @Value("${external.url}") String externalUrl) {
        this.geoJsonWriter = Objects.requireNonNull(geoJsonWriter);
        this.decoderRepository = Objects.requireNonNull(decoderRepository);
        this.catalogProperties = Objects.requireNonNull(catalogProperties);
        this.modelPersistence = Objects.requireNonNull(modelPersistence);
        this.externalUrl = Objects.requireNonNull(externalUrl);
        this.serviceRepository = Objects.requireNonNull(serviceRepository);
    }

    @PostConstruct
    public void init() {
        lock.writeLock().lock();
        try {
            this.model = modelPersistence.read().orElseGet(this::createModel);
            this.catalog = findCatalog().orElseGet(this::createCatalog);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void onResult(ServiceConstellation result) {
        if (result == null) {
            return;
        }

        ServiceEntity service = serviceRepository.findByNameAndUrlAndType(result.getService().getName(),
                                                                          result.getService().getUrl(),
                                                                          result.getService().getType());

        lock.writeLock().lock();
        try {

            SosCapabilities capabilities = getCapabilities(result);

            catalog.removeAll(DCTerms.modified);
            catalog.addProperty(DCTerms.modified,
                                now(),
                                XSDDatatype.XSDdateTime);

            createServiceBasedDatasets(service, capabilities);
            modelPersistence.write(model);
        } catch (DecodingException e) {
            LOG.warn("could not parse SOS capabilities document", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String now() {
        return DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now());
    }

    private Optional<String> removeDatasets(ServiceEntity result) {
        lock.writeLock().lock();
        try {
            List<Resource> dataset = getDatasetResources(result);

            // get the issued date
            Optional<String> issued = dataset.stream()
                                             .map(ds -> ds.getProperty(DCTerms.issued))
                                             .filter(Objects::nonNull)
                                             .findAny()
                                             .map(Statement::getString);

            // remove the link between catalog and dataset
            dataset.forEach(ds -> model.remove(catalog, DCAT.dataset, ds));

            // remove all statements that are no longer reachable from the catalog
            model.remove(model.difference(ResourceUtils.reachableClosure(catalog)));
            return issued;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<Resource> getDatasetResources(ServiceEntity result) {
        return model.listSubjectsWithProperty(RDF.type, DCAT.Dataset)
                    .filterKeep(r -> r.getURI().startsWith(result.getUrl()))
                    .toList();
    }

    private Model createModel() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix(PREFIX_XSD, XSD.NS);
        model.setNsPrefix(PREFIX_RDF, RDF.uri);
        model.setNsPrefix(PREFIX_DCAT, DCAT.NS);
        model.setNsPrefix(PREFIX_DCT, DCTerms.NS);
        model.setNsPrefix(PREFIX_FOAF, FOAF.NS);
        model.setNsPrefix(PREFIX_VCARD, VCARD4.NS);
        model.setNsPrefix(PREFIX_TIME, TIME.NS);
        model.setNsPrefix(PREFIX_LOCN, LOCN.NS);
        return model;
    }

    private void createServiceBasedDatasets(ServiceEntity service, SosCapabilities capabilities) {

        Optional<String> issued = removeDatasets(service);

        Resource dataset = model.createResource(service.getUrl(), DCAT.Dataset)
                                .addProperty(DCAT.landingPage, model.createResource(getGetCapabilitiesURL(service)))
                                // TODO review this
                                .addProperty(DCTerms.identifier, service.getIdentifier())
                                .addProperty(DCTerms.identifier, model.createTypedLiteral((long) service.getId()))
                                .addProperty(DCTerms.identifier, service.getUrl());
        catalog.addProperty(DCAT.dataset, dataset);
        addTitle(capabilities, dataset);
        addDescription(capabilities, dataset);
        addKeywords(capabilities, dataset);
        addKeywordsFromContents(capabilities, dataset);
        addSpatialExtent(capabilities, dataset);
        addTemporalExtent(capabilities, dataset);
        addServiceProvider(capabilities, dataset);
        addLanguages(capabilities, dataset);
        addAccessConstraints(capabilities, dataset);

        addIssuedAndModified(issued, dataset);

        dataset.addProperty(DCAT.distribution, createSosDistribution(service, capabilities, dataset));
        dataset.addProperty(DCAT.distribution, createHelgolandApiDistribution(service, dataset));

    }

    private void createOfferingBasedDatasets(ServiceEntity service, SosCapabilities capabilities) {

        Optional<String> issued = removeDatasets(service);

        capabilities.getContents().ifPresent(contents -> {
            contents.forEach(offering -> {
                String identifier = service.getUrl() + "#" + offering.getIdentifier();
                Resource dataset = model.createResource(identifier, DCAT.Dataset)
                                        .addProperty(DCAT.landingPage,
                                                     model.createResource(getGetCapabilitiesURL(service)))
                                        // TODO add the numeric offering id
                                        .addProperty(DCTerms.identifier, identifier);
                catalog.addProperty(DCAT.dataset, dataset);
                // TODO create unique title
                addTitle(capabilities, dataset);
                // TODO create unique description
                addDescription(capabilities, dataset);
                addKeywords(capabilities, dataset);
                addKeywordsFromOfferings(dataset, () -> Stream.of(offering));
                addSpatialExtent(offering, dataset);
                addTemporalExtent(offering, dataset);
                addServiceProvider(capabilities, dataset);
                addLanguages(capabilities, dataset);
                addAccessConstraints(capabilities, dataset);
                addIssuedAndModified(issued, dataset);
                dataset.addProperty(DCAT.distribution, createSosDistribution(service, capabilities, dataset));
                // TODO include the offering id in the URL
                dataset.addProperty(DCAT.distribution, createHelgolandApiDistribution(service, dataset));
            });
        });

    }

    private Resource createSosDistribution(ServiceEntity service, SosCapabilities capabilities, Resource dataset) {
        String getCapabilitiesURL = getGetCapabilitiesURL(service);
        Resource sosDataService = createSosService(service, capabilities, dataset);
        return model.createResource(DCAT.Distribution)
                    .addProperty(DCTerms.title, "SOS")
                    .addProperty(DCAT.accessURL, model.createResource(getCapabilitiesURL))
                    .addProperty(DCAT.mediaType, "application/xml")
                    .addProperty(DCAT.accessService, sosDataService);
    }

    private Resource createSosService(ServiceEntity service, SosCapabilities capabilities, Resource dataset) {
        Resource sosDataService = model.createResource(DCAT.DataService)
                                       .addProperty(DCAT.endpointURL,
                                                    model.createResource(service.getUrl()))
                                       .addProperty(DCAT.endpointDescription,
                                                    model.createResource(getGetCapabilitiesURL(service)))
                                       .addProperty(DCAT.servesDataset, dataset);
        addProfiles(capabilities, sosDataService);
        return sosDataService;
    }

    private void addIssuedAndModified(Optional<String> issued, Resource dataset) {
        String now = format(DateTime.now());
        dataset.addProperty(DCTerms.modified, now, XSDDatatype.XSDdateTime);
        dataset.addProperty(DCTerms.issued, issued.orElse(now), XSDDatatype.XSDdateTime);
    }

    private void addAccessConstraints(SosCapabilities capabilities, Resource dataset) {
        capabilities.getServiceIdentification()
                    .map(OwsServiceIdentification::getAccessConstraints)
                    .map(Set::stream)
                    .orElseGet(Stream::empty)
                    .map(this::createResourceOrLiteral)
                    .forEach(accessConstraints -> dataset.addProperty(DCTerms.accessRights, accessConstraints));
    }

    private Resource createHelgolandApiDistribution(ServiceEntity service, Resource dataset) {
        return model.createResource(DCAT.Distribution)
                    .addProperty(DCTerms.title, "Helgoland API")
                    .addProperty(DCAT.mediaType, "application/json")
                    .addProperty(DCAT.accessURL, model.createResource(getServiceURL(service)))
                    .addProperty(DCAT.accessService,
                                 model.createResource(DCAT.DataService)
                                      .addProperty(DCAT.endpointURL,
                                                   model.createResource(externalUrl))
                                      .addProperty(DCAT.endpointDescription,
                                                   model.createResource(API_DESCRIPTION))
                                      .addProperty(DCAT.servesDataset, dataset));
    }

    private String getServiceURL(ServiceEntity service) {
        return externalUrl + "services/" + service.getId();
    }

    private String getGetCapabilitiesURL(ServiceEntity result) {
        String url = result.getUrl();
        if (url.contains("?")) {
            url += "&";
        } else {
            url += "?";
        }
        url += "service=SOS&request=GetCapabilities";
        return url;
    }

    private void addLanguages(SosCapabilities capabilities, Resource dataset) {
        capabilities.getOperationsMetadata().map(OwsOperationsMetadata::getParameters).map(Set::stream)
                    .orElseGet(Stream::empty)
                    .filter(domain -> LANGUAGE_PARAMETER.equals(domain.getName()))
                    .map(OwsDomain::getPossibleValues)
                    .filter(OwsAllowedValues.class::isInstance)
                    .map(OwsPossibleValues::asAllowedValues)
                    .map(OwsAllowedValues::getRestrictions)
                    .flatMap(Set::stream)
                    .filter(OwsValueRestriction::isValue)
                    .map(OwsValueRestriction::asValue)
                    .map(OwsValue::getValue)
                    .forEach(language -> dataset.addProperty(DCTerms.language, language));
    }

    private void addServiceProvider(SosCapabilities capabilities, Resource dataset) {
        capabilities.getServiceProvider().ifPresent(serviceProvider -> addServiceProvider(dataset, serviceProvider));
    }

    private void addSpatialExtent(SosCapabilities capabilities, Resource dataset) {
        getSpatialExtent(capabilities).ifPresent(x -> dataset.addProperty(DCTerms.spatial, x));
    }

    private void addSpatialExtent(SosObservationOffering offering, Resource dataset) {
        getSpatialExtent(offering).ifPresent(x -> dataset.addProperty(DCTerms.spatial, x));
    }

    private Optional<Resource> getSpatialExtent(SosObservationOffering offering) {
        if (offering.isSetObservedArea() && offering.getObservedArea().isSetEnvelope()) {
            Geometry geometry = offering.getObservedArea().toGeometry();
            if (!geometry.isEmpty()) {
                Resource resource = model.createResource(DCTerms.Location)
                                         .addProperty(LOCN.geometry,
                                                      geoJsonWriter.write(geometry),
                                                      RDFDataTypes.GEO_JSON);
                return Optional.of(resource);
            }
        }
        return Optional.empty();
    }

    private Optional<Resource> getSpatialExtent(SosCapabilities capabilities) {
        return capabilities.getContents().map(Set::stream).orElseGet(Stream::empty)
                           .filter(SosObservationOffering::isSetObservedArea)
                           .map(SosObservationOffering::getObservedArea)
                           .map(ReferencedEnvelope::toGeometry)
                           .reduce(Geometry::union)
                           .map(geoJsonWriter::write)
                           .map(json -> model.createResource(DCTerms.Location)
                                             .addProperty(LOCN.geometry, json, RDFDataTypes.GEO_JSON));
    }

    private ReferencedEnvelope getEnvelope(Set<SosObservationOffering> contents) {
        ReferencedEnvelope envelope = null;
        for (SosObservationOffering offering : contents) {
            if (offering.isSetObservedArea()) {
                if (envelope == null) {
                    envelope = offering.getObservedArea();
                } else {
                    envelope.expandToInclude(offering.getObservedArea());
                }
            }
        }
        return envelope;
    }

    private void addTemporalExtent(SosCapabilities capabilities, Resource dataset) {
        getTemporalExtent(capabilities).ifPresent(time -> dataset.addProperty(DCTerms.temporal, time));
    }

    private void addTemporalExtent(SosObservationOffering offering, Resource dataset) {
        getTemporalExtent(offering).ifPresent(time -> dataset.addProperty(DCTerms.temporal, time));
    }

    private Optional<Resource> getTemporalExtent(SosObservationOffering offering) {
        return createTime(offering.getPhenomenonTime());
    }

    private Optional<Resource> getTemporalExtent(SosCapabilities capabilities) {
        LongSummaryStatistics ss = capabilities.getContents().map(Set::stream)
                                               .orElseGet(Stream::empty)
                                               .map(SosObservationOffering::getPhenomenonTime)
                                               .flatMap(this::toDateTimeStream)
                                               .mapToLong(DateTime::getMillis)
                                               .summaryStatistics();
        return Optional.of(ss).filter(x -> x.getCount() > 0).flatMap(this::createTime);
    }

    private Stream<DateTime> toDateTimeStream(Time time) {
        if (time instanceof TimeInstant && ((TimeInstant) time).isSetValue()) {
            return Stream.of(((TimeInstant) time).getValue());
        } else if (time instanceof TimePeriod) {
            TimePeriod period = (TimePeriod) time;
            if (period.isSetStart() && period.isSetEnd()) {
                return Stream.of(period.getStart(), period.getEnd());
            } else if (period.isSetStart()) {
                return Stream.of(period.getStart());
            } else if (period.isSetEnd()) {
                return Stream.of(period.getEnd());
            }
        }
        return Stream.empty();
    }

    private Optional<Resource> createTime(LongSummaryStatistics ss) {
        if (ss.getMin() == ss.getMax()) {
            return createTimeInstant(ss.getMin());
        }
        return createTimeInterval(createTimeInstant(ss.getMin()).orElse(null),
                                  createTimeInstant(ss.getMax()).orElse(null));
    }

    private Optional<Resource> createTimeInterval(Resource begin, Resource end) {
        if (begin == null && end == null) {
            return Optional.empty();
        }
        Resource resource = model.createResource(TIME.Interval);
        if (begin != null) {
            resource.addProperty(TIME.hasBeginning, begin);
        }
        if (end != null) {
            resource.addProperty(TIME.hasEnd, end);
        }
        return Optional.of(resource);
    }

    private Optional<Resource> createTimeInstant(long time) {
        return createTimeInstant(new DateTime(time));
    }

    private Optional<Resource> createTimeInstant(DateTime value) {
        if (value == null) {
            return Optional.empty();
        }
        Resource resource = model.createResource(TIME.Instant);
        resource.addProperty(TIME.inXSDDateTimeStamp, format(value));
        return Optional.of(resource);
    }

    private Optional<Resource> createTime(Time time) {
        if (time instanceof TimePeriod) {
            TimePeriod timePeriod = (TimePeriod) time;
            return createTimeInterval(createTimeInstant(timePeriod.getStart()).orElse(null),
                                      createTimeInstant(timePeriod.getEnd()).orElse(null));
        } else if (time instanceof TimeInstant) {
            TimeInstant instant = (TimeInstant) time;
            return createTimeInstant(instant.getValue());
        }
        return Optional.empty();
    }

    private String format(DateTime value) {
        return value.toDateTime(DateTimeZone.UTC)
                    .toString(ISODateTimeFormat.dateTime());
    }

    private void addTitle(SosCapabilities capabilities, Resource dataset) {
        addLocalizedStrings(dataset, capabilities.getServiceIdentification()
                                                 .flatMap(OwsServiceIdentification::getTitle)
                                                 .orElse(null), DCTerms.title);
    }

//    private void addTitle(SosCapabilities capabilities, SosObservationOffering offering, Resource dataset) {
//        addLocalizedStrings(dataset, capabilities.getServiceIdentification()
//                                                 .flatMap(OwsServiceIdentification::getTitle)
//                                                 .orElse(null), DCTerms.title);
//    }

    private void addDescription(SosCapabilities capabilities, Resource dataset) {
        addLocalizedStrings(dataset, capabilities.getServiceIdentification()
                                                 .flatMap(OwsServiceIdentification::getAbstract)
                                                 .orElse(null), DCTerms.description);
    }

    private void addLocalizedStrings(Resource dataset, MultilingualString localizedStrings, Property property) {
        if (localizedStrings != null) {
            localizedStrings.getLocalizations().values()
                            .forEach(description -> dataset.addProperty(property,
                                                                        description.getText(),
                                                                        description.getLangString()));
        }
    }

    private void addKeywordsFromContents(SosCapabilities capabilities, Resource dataset) {
        Supplier<Stream<SosObservationOffering>> offeringStream = () -> capabilities.getContents().map(Set::stream)
                                                                                    .orElseGet(Stream::empty);
        addKeywordsFromOfferings(dataset, offeringStream);

    }

    private void addKeywordsFromOfferings(Resource dataset, Supplier<Stream<SosObservationOffering>> offeringStream) {
//        Stream.of(offeringStream.get().map(SosObservationOffering::getIdentifier).map(this::createResourceOrLiteral),
//                  offeringStream.get().map(SosObservationOffering::getName).flatMap(List::stream)
//                                .map(name -> model.createLiteral(name.getValue(),
//                                                                 Optional.ofNullable(name.getCodeSpace())
//                                                                         .map(URI::toString)
//                                                                         .orElse(null))),
//                  offeringStream.get().map(SosObservationOffering::getObservableProperties).flatMap(Set::stream)
//                                .map(this::createResourceOrLiteral),
//                  offeringStream.get().map(SosObservationOffering::getProcedures).flatMap(Set::stream)
//                                .map(this::createResourceOrLiteral),
//                  offeringStream.get().map(SosObservationOffering::getFeatureOfInterest).flatMap(Set::stream)
//                                .map(this::createResourceOrLiteral))
//              .flatMap(Function.identity())
//              .forEach(feature -> dataset.addProperty(DCAT.keyword, feature));
    }

    private void addKeywords(SosCapabilities capabilities, Resource dataset) {
        capabilities.getServiceIdentification().map(OwsServiceIdentification::getKeywords)
                    .map(Set::stream).orElseGet(Stream::empty)
                    .forEach(keyword -> dataset.addProperty(DCAT.keyword, keyword.getKeyword().getValue(),
                                                            keyword.getKeyword().getLang().orElse("")));
    }

    private void addProfiles(SosCapabilities capabilities, Resource resource) {
        capabilities.getServiceIdentification()
                    .map(OwsServiceIdentification::getProfiles)
                    .map(Set::stream)
                    .orElseGet(Stream::empty)
                    .filter(URI::isAbsolute)
                    .map(URI::toString)
                    .forEach(profile -> resource.addProperty(DCTerms.conformsTo,
                                                             model.createResource(profile, DCTerms.Standard)));
    }

    private void addServiceProvider(Resource dataset, OwsServiceProvider serviceProvider) {
        Resource publisher = model.createResource(FOAF.Organization)
                                  .addProperty(FOAF.name, serviceProvider.getProviderName());
        serviceProvider.getProviderSite().flatMap(OwsOnlineResource::getHref).filter(URI::isAbsolute)
                       .map(URI::toString)
                       .map(site -> model.createResource(site, FOAF.Document))
                       .ifPresent(r -> publisher.addProperty(FOAF.homepage, r));
        dataset.addProperty(DCTerms.publisher, publisher);

        OwsResponsibleParty serviceContact = serviceProvider.getServiceContact();

        Resource organization = model.createResource(VCARD4.Organization)
                                     .addProperty(VCARD4.fn, serviceContact.getOrganisationName()
                                                                           .orElse(serviceProvider
                                                                                           .getProviderName()));
        serviceContact.getIndividualName().ifPresent(individualName -> {
            Resource contactPoint = model.createResource(VCARD4.Individual)
                                         .addProperty(VCARD4.fn, individualName);
            organization.addProperty(VCARD4.hasMember, contactPoint);
            serviceContact.getPositionName().ifPresent(x -> contactPoint.addProperty(VCARD4.role, x));
        });

        serviceContact.getRole().map(OwsCode::getValue)
                      .ifPresent(role -> organization.addProperty(VCARD4.role, role));
        dataset.addProperty(DCAT.contactPoint, organization);

        serviceContact.getContactInfo().ifPresent(contact -> {
            contact.getAddress().ifPresent(address -> {
                address.getElectronicMailAddress().stream()
                       .map(UriUtil::createMailURI)
                       .map(model::createResource)
                       .forEach(mail -> organization.addProperty(VCARD4.email, mail));
                createAddress(address).ifPresent(x -> organization.addProperty(VCARD4.hasAddress, x));
            });
            contact.getPhone().ifPresent(phone -> {
                phone.getFacsimile().stream()
                     .map(UriUtil::createTelURI)
                     .map(tel -> model.createResource(tel, VCARD4.Fax))
                     .forEach(number -> organization.addProperty(VCARD4.hasTelephone, number));
                phone.getVoice().stream().map(UriUtil::createTelURI)
                     .map(tel -> model.createResource(tel, VCARD4.Voice))
                     .forEach(number -> organization.addProperty(VCARD4.hasTelephone, number));
            });
        });
    }

    private Optional<Resource> createAddress(OwsAddress address) {
        if (!Optionals.any(address.getAdministrativeArea(),
                           address.getCity(),
                           address.getCountry(),
                           address.getPostalCode(),
                           address.getDeliveryPoint().stream().findAny())) {
            return Optional.empty();
        }
        Resource resource = model.createResource(VCARD4.Address);
        address.getAdministrativeArea().ifPresent(x -> resource.addProperty(VCARD4.region, x));
        address.getCity().ifPresent(x -> resource.addProperty(VCARD4.locality, x));
        address.getCountry().ifPresent(x -> resource.addProperty(VCARD4.country_name, x));
        address.getPostalCode().ifPresent(x -> resource.addProperty(VCARD4.postal_code, x));
        address.getDeliveryPoint().forEach(x -> resource.addProperty(VCARD4.street_address, x));
        return Optional.of(resource);
    }

    private RDFNode createResourceOrLiteral(String value) {
        if (UriUtil.isAbsoluteURI(value)) {
            return model.createResource(value);
        } else {
            return model.createLiteral(value);
        }
    }

    private SosCapabilities getCapabilities(ServiceConstellation result) throws DecodingException {
        XmlObject xmlResponse;
        try {
            xmlResponse = XmlObject.Factory.parse(result.getService().getServiceMetadata().getMetadata());
        } catch (XmlException e) {
            throw new DecodingException(e);
        }
        DecoderKey decoderKey = CodingHelper.getDecoderKey(xmlResponse);
        Decoder<Object, XmlObject> decoder = decoderRepository.getDecoder(decoderKey);
        if (decoder == null) {
            throw new NoDecoderForKeyException(decoderKey);
        }
        Object decode = decoder.decode(xmlResponse);
        if (decode instanceof OwsExceptionReport) {
            OwsExceptionReport exceptionReport = (OwsExceptionReport) decode;
            throw new DecodingException(exceptionReport);
        }

        GetCapabilitiesResponse response = (GetCapabilitiesResponse) decode;
        OwsCapabilities capabilities = response.getCapabilities();
        if (!(capabilities instanceof SosCapabilities)) {
            throw new DecodingException("not a SOS capabilities document");
        }
        return (SosCapabilities) capabilities;

    }

    private Optional<Resource> findCatalog() {
        return findResource(DCAT.Catalog);
    }

    private Optional<Resource> findResource(Resource type) {
        ResIterator iter = model.listSubjectsWithProperty(RDF.type, type);
        if (iter.hasNext()) {
            return Optional.of(iter.nextResource());
        }
        return Optional.empty();
    }

    private Resource createCatalog() {
        Resource catalog = model.createResource(DCAT.Catalog);
        if (!Strings.isNullOrEmpty(catalogProperties.getTitle())) {
            catalog.addProperty(DCTerms.title, catalogProperties.getTitle(),
                                Strings.nullToEmpty(catalogProperties.getLanguage()));
        }
        if (!Strings.isNullOrEmpty(catalogProperties.getDescription())) {
            catalog.addProperty(DCTerms.description, catalogProperties.getDescription(),
                                Strings.nullToEmpty(catalogProperties.getLanguage()));
        }
        if (UriUtil.isAbsoluteURI(catalogProperties.getPublisher())) {
            catalog.addProperty(DCTerms.publisher, model.createResource(catalogProperties.getPublisher()));
        }
        if (UriUtil.isAbsoluteURI(catalogProperties.getHomepage())) {
            catalog.addProperty(FOAF.homepage, model.createResource(catalogProperties.getHomepage()));
        }
        String now = now();
        catalog.addProperty(DCTerms.modified, now, XSDDatatype.XSDdateTime)
               .addProperty(DCTerms.issued, now, XSDDatatype.XSDdateTime);
        if (UriUtil.isAbsoluteURI(catalogProperties.getLicense())) {
            catalog.addProperty(DCTerms.license, model.createResource(catalogProperties.getLicense()));
        }
        if (!Strings.isNullOrEmpty(catalogProperties.getLanguage())) {
            catalog.addProperty(DCTerms.language, catalogProperties.getLanguage());
        }
        return catalog;
    }

    @Override
    public Model getModel() {
        lock.readLock().lock();
        try {
            return ModelFactory.createDefaultModel().add(model);
        } finally {
            lock.readLock().unlock();
        }
    }
}
