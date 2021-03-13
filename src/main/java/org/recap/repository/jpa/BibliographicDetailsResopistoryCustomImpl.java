package org.recap.repository.jpa;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Transactional
public class BibliographicDetailsResopistoryCustomImpl implements BibliographicDetailsResopistoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public <S extends BibliographicEntity> S updateBibForSubmitCollection(S entity, ItemEntity fetchedItemEntity) {
            setHoldingsAndItemId(entity, fetchedItemEntity);
            return entityManager.merge(entity);
    }

    private <S extends BibliographicEntity> void setHoldingsAndItemId(S entity, ItemEntity fetchedItemEntity) {
        Integer holdingsId = fetchedItemEntity.getHoldingsEntities().get(0).getId();
        Integer itemId = fetchedItemEntity.getId();

        setHoldingsId(entity, holdingsId);
        setItemId(entity, itemId);
    }

    private <S extends BibliographicEntity> void setItemId(S entity, Integer itemId) {
        entity.getItemEntities()
                .stream()
                .filter(itemEntity -> itemEntity.getId()==null)
                .forEach(itemEntity -> itemEntity.setId(itemId));
    }

    private <S extends BibliographicEntity> void setHoldingsId(S entity, Integer holdingsId) {
        entity.getHoldingsEntities()
                .stream()
                .filter(holdingsEntity -> holdingsEntity.getId() == null)
                .forEach(holdingsEntity -> holdingsEntity.setId(holdingsId));
    }
}
