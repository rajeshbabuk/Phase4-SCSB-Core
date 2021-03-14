package org.recap.repository.jpa;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ItemEntity;

public interface BibliographicDetailsResopistoryCustom {
    <S extends BibliographicEntity> S updateBibForSubmitCollection(S entity, ItemEntity itemEntity);
}
