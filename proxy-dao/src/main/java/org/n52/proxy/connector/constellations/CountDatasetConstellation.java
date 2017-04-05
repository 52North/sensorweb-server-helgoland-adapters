/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.proxy.connector.constellations;

import java.util.Date;
import static org.n52.proxy.connector.utils.EntityBuilder.updateDatasetEntity;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;

/**
 * @author Jan Schulte
 */
public class CountDatasetConstellation extends DatasetConstellation {

    public CountDatasetConstellation(String procedure, String offering, String category, String phenomenon,
            String feature) {
        super(procedure, offering, category, phenomenon, feature);
    }

    @Override
    public DatasetEntity createDatasetEntity(ProcedureEntity procedure, CategoryEntity category, FeatureEntity feature,
            OfferingEntity offering, PhenomenonEntity phenomenon, ProxyServiceEntity service) {
        CountDatasetEntity countDataset = new CountDatasetEntity();
        countDataset.setDomainId(this.getDomainId());
        updateDatasetEntity(countDataset, procedure, category, feature, offering, phenomenon, service);
        countDataset.setFirstValueAt(new Date());
        countDataset.setLastValueAt(new Date());
        return countDataset;
    }

}
