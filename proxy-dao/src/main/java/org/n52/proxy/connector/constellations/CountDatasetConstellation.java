/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.proxy.connector.constellations;

import java.util.Date;

import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.CountDatasetEntity;

/**
 * @author Jan Schulte
 */
public class CountDatasetConstellation extends DatasetConstellation<CountDatasetEntity> {

    public CountDatasetConstellation(String procedure, String offering, String category, String phenomenon,
            String feature) {
        super(procedure, offering, category, phenomenon, feature);
    }

    @Override
    protected CountDatasetEntity createDatasetEntity(ProxyServiceEntity service) {
        CountDatasetEntity countDataset = new CountDatasetEntity();
        countDataset.setFirstValueAt(new Date());
        countDataset.setLastValueAt(new Date());
        return countDataset;
    }

}
