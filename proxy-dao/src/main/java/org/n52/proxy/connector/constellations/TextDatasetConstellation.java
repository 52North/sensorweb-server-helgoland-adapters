package org.n52.proxy.connector.constellations;

import java.util.Date;

import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.TextDatasetEntity;

/**
 * @author Jan Schulte
 */
public class TextDatasetConstellation extends DatasetConstellation<TextDatasetEntity> {

    public TextDatasetConstellation(String procedure, String offering, String category, String phenomenon,
            String feature) {
        super(procedure, offering, category, phenomenon, feature);
    }

    @Override
    protected TextDatasetEntity createDatasetEntity(ProxyServiceEntity service) {
        TextDatasetEntity textDataset = new TextDatasetEntity();
        textDataset.setFirstValueAt(new Date());
        textDataset.setLastValueAt(new Date());
        return textDataset;
    }

}
