package org.recap.service;

import lombok.extern.slf4j.Slf4j;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by rajeshbabuk on 17/Sep/2021
 */
@Slf4j
@Service
public class AsyncService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Async
    public void compareMatchPoints(String incomingMarcXml, String existingMarcXml, String matchingIdentifier, String institutionCode, Map<String, ItemEntity> fetchedBarcodeItemEntityMap, Map<String, ItemEntity> incomingBarcodeItemEntityMap, boolean isCGDProtected) {
        try {
            boolean isCGDChanged = false;
            if (!isCGDProtected) {
                isCGDChanged = commonUtil.isCgdChanged(fetchedBarcodeItemEntityMap, incomingBarcodeItemEntityMap);
            }

            boolean isMatchPointsEqual = commonUtil.compareMatchPointsByMarcXml(incomingMarcXml, existingMarcXml, institutionCode);
            if (isCGDChanged || !isMatchPointsEqual) {
                List<Integer> bibIds = bibliographicDetailsRepository.findIdByMatchingIdentity(matchingIdentifier);
                bibliographicDetailsRepository.resetMatchingColumnsAndUpdateMaQualifier(bibIds);
                bibliographicDetailsRepository.flush();
                commonUtil.indexData(new HashSet<>(bibIds));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
