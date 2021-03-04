package org.recap.service;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.HoldingsDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;
import javax.persistence.EntityManager;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rajeshbabuk on 05/Mar/2021
 */
public class BibliographicRepositoryDAOUT extends BaseTestCaseUT {

    @InjectMocks
    BibliographicRepositoryDAO bibliographicRepositoryDAO;

    @Mock
    EntityManager entityManager;

    @Mock
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Mock
    private HoldingsDetailsRepository holdingsDetailsRepository;

    @Mock
    private ItemDetailsRepository itemDetailsRepository;

    @Test
    public void saveOrUpdate() {
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId("11");
        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setOwningInstitutionId(1);
        holdingsEntity.setOwningInstitutionHoldingsId("22");
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setOwningInstitutionItemId("33");
        holdingsEntity.setItemEntities(Collections.singletonList(itemEntity));
        bibliographicEntity.setHoldingsEntities(Collections.singletonList(holdingsEntity));
        bibliographicEntity.setItemEntities(Collections.singletonList(itemEntity));
        Mockito.when(bibliographicDetailsRepository.save(Mockito.any())).thenReturn(bibliographicEntity);
        Mockito.doNothing().when(entityManager).refresh(Mockito.any());
        BibliographicEntity savedBibliographicEntity = bibliographicRepositoryDAO.saveOrUpdate(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
    }
}
