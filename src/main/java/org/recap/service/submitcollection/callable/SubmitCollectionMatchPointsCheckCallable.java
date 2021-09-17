package org.recap.service.submitcollection.callable;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.recap.ScsbCommonConstants;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by rajeshbabuk on 17/Sep/2021
 */
@Slf4j
@Component
@Scope("prototype")
@Data
public class SubmitCollectionMatchPointsCheckCallable implements Callable<String> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    Integer fetchedBibId;
    String incomingMarcXml;
    String existingMarcXml;
    String matchingIdentifier;
    String institutionCode;
    Map<String, ItemEntity> fetchedBarcodeItemEntityMap;
    Map<String, ItemEntity> incomingBarcodeItemEntityMap;
    boolean isCGDProtected;

    @Override
    public String call() throws Exception {
        try {
            boolean isCGDChanged = false;
            if (!isCGDProtected) {
                isCGDChanged = commonUtil.isCgdChanged(fetchedBarcodeItemEntityMap, incomingBarcodeItemEntityMap);
            }
            boolean isMatchPointsEqual = commonUtil.compareMatchPointsByMarcXml(incomingMarcXml, existingMarcXml, institutionCode);
            if (isCGDChanged || !isMatchPointsEqual) {
                List<Integer> bibIds = new ArrayList<>();
                if (matchingIdentifier != null) {
                    bibIds = bibliographicDetailsRepository.findIdByMatchingIdentity(matchingIdentifier);
                } else {
                    bibIds.add(fetchedBibId);
                }
                log.info("Matching Id - {}, Resetting {} Bib Ids: {}", matchingIdentifier, bibIds.size(), bibIds);
                bibliographicDetailsRepository.resetMatchingColumnsAndUpdateMaQualifier(bibIds);
                bibliographicDetailsRepository.flush();
                commonUtil.indexData(new HashSet<>(bibIds));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ScsbCommonConstants.SUCCESS;
    }
}
