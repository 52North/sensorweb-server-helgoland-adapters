/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.proxy.da;

import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.profile.ProfileValue;
import org.n52.proxy.connector.AbstractConnector;
import org.n52.series.db.ValueAssemblerComponent;
import org.n52.series.db.assembler.value.QuantityProfileValueAssembler;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.old.dao.DbQuery;
import org.n52.series.db.repositories.core.DataRepository;
import org.n52.series.db.repositories.core.DatasetRepository;

/**
 * @author Jan Schulte
 */
@ValueAssemblerComponent(value = "profil-quantity", datasetEntityType = DatasetEntity.class)
public class ProxyQuantityProfileDataRepository extends QuantityProfileValueAssembler {

    private Map<String, AbstractConnector> connectorMap;

    public ProxyQuantityProfileDataRepository(DataRepository<ProfileDataEntity> profileDataRepository,
            DatasetRepository datasetRepository) {
        super(profileDataRepository, datasetRepository);
    }

    @Inject
    public void setConnectors(List<AbstractConnector> connectors) {
        this.connectorMap =
                connectors.stream().collect(toMap(AbstractConnector::getConnectorName, Function.identity()));
    }

    private AbstractConnector getConnector(DatasetEntity profileDatasetEntity) {
        String connectorName = profileDatasetEntity.getService().getConnector();
        return this.connectorMap.get(connectorName);
    }

    @Override
    public ProfileValue<BigDecimal> getFirstValue(DatasetEntity profileDatasetEntity, DbQuery query) {
        DataEntity<?> firstObs =
                getConnector(profileDatasetEntity).getFirstObservation(profileDatasetEntity).orElse(null);
        if (firstObs == null) {
            return null;
        }
        return assembleDataValue((ProfileDataEntity) firstObs, profileDatasetEntity, query);
    }

    @Override
    public ProfileValue<BigDecimal> getLastValue(DatasetEntity profileDatasetEntity, DbQuery query) {
        DataEntity<?> lastObs =
                getConnector(profileDatasetEntity).getLastObservation(profileDatasetEntity).orElse(null);
        if (lastObs == null) {
            return null;
        }
        return assembleDataValue((ProfileDataEntity) lastObs, profileDatasetEntity, query);
    }

    @Override
    protected Data<ProfileValue<BigDecimal>> assembleDataValues(DatasetEntity profileDatasetEntity, DbQuery query) {
        Data<ProfileValue<BigDecimal>> result = new Data<>();
        this.getConnector(profileDatasetEntity).getObservations(profileDatasetEntity, query).stream()
                .map(entry -> assembleDataValue((ProfileDataEntity) entry, profileDatasetEntity, query))
                .forEach(entry -> result.addNewValue(entry));
        return result;
    }

}
