package org.recap.converter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.marc4j.marc.Record;
import org.recap.ScsbConstants;
import org.recap.ScsbCommonConstants;
import org.recap.model.jpa.*;
import org.recap.model.marc.BibMarcRecord;
import org.recap.model.marc.HoldingsMarcRecord;
import org.recap.model.marc.ItemMarcRecord;
import org.recap.repository.jpa.ImsLocationDetailsRepository;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;
import org.recap.repository.jpa.OwnerCodeDetailsRepository;
import org.recap.util.CommonUtil;
import org.recap.util.DBReportUtil;
import org.recap.util.MarcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchulakshmig on 17/10/16.
 */
@Service
public class MarcToBibEntityConverter implements XmlToBibEntityConverterInterface{

    private static final Logger logger = LoggerFactory.getLogger(MarcToBibEntityConverter.class);

    @Autowired
    private MarcUtil marcUtil;

    @Autowired
    private DBReportUtil dbReportUtil;


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private OwnerCodeDetailsRepository ownerCodeDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private ImsLocationDetailsRepository imsLocationDetailsRepository;

    @Override
    public Map convert(Object marcRecord, InstitutionEntity institutionEntity) {
        Map<String, Object> map = new HashMap<>();
        boolean processBib = false;

        Record record = (Record) marcRecord;
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        List<ItemEntity> itemEntities = new ArrayList<>();
        List<ReportEntity> reportEntities = new ArrayList<>();

        getDbReportUtil().setInstitutionEntitiesMap(commonUtil.getInstitutionEntityMap());
        getDbReportUtil().setCollectionGroupMap(commonUtil.getCollectionGroupMap());

        StringBuilder errorMessage = new StringBuilder();

        try {
            BibMarcRecord bibMarcRecord = marcUtil.buildBibMarcRecord(record);
            Record bibRecord = bibMarcRecord.getBibRecord();
            Integer owningInstitutionId;
            if(institutionEntity == null){
                owningInstitutionId = getOwningInstitutionId(bibMarcRecord);
                institutionEntity = institutionDetailsRepository.findById(owningInstitutionId).orElse(institutionEntity);
            }
            Date currentDate = new Date();
            Map<String, Object> bibMap = processAndValidateBibliographicEntity(bibRecord, institutionEntity,currentDate,errorMessage);
            BibliographicEntity bibliographicEntity = (BibliographicEntity) bibMap.get(ScsbConstants.BIBLIOGRAPHIC_ENTITY);
            ReportEntity bibReportEntity = (ReportEntity) bibMap.get("bibReportEntity");
            if (bibReportEntity != null) {
                reportEntities.add(bibReportEntity);
            } else {
                processBib = true;
            }

            List<HoldingsMarcRecord> holdingsMarcRecords = bibMarcRecord.getHoldingsMarcRecords();
            if (CollectionUtils.isNotEmpty(holdingsMarcRecords)) {
                for (HoldingsMarcRecord holdingsMarcRecord : holdingsMarcRecords) {
                    boolean processHoldings = false;
                    Record holdingsRecord = holdingsMarcRecord.getHoldingsRecord();
                    Map<String, Object> holdingsMap = processAndValidateHoldingsEntity(bibliographicEntity, holdingsRecord, currentDate,errorMessage);
                    HoldingsEntity holdingsEntity = (HoldingsEntity) holdingsMap.get("holdingsEntity");
                    ReportEntity holdingsReportEntity = (ReportEntity) holdingsMap.get("holdingsReportEntity");
                    if (holdingsReportEntity != null) {
                        reportEntities.add(holdingsReportEntity);
                    } else {
                        processHoldings = true;
                    }
                    String holdingsCallNumber = marcUtil.getDataFieldValue(holdingsRecord, "852", 'h');
                    if(holdingsCallNumber == null){
                        holdingsCallNumber = "";
                    }
                    Character holdingsCallNumberType = marcUtil.getInd1(holdingsRecord, "852", 'h');

                    List<ItemMarcRecord> itemMarcRecordList = holdingsMarcRecord.getItemMarcRecordList();
                    if (CollectionUtils.isNotEmpty(itemMarcRecordList)) {
                        if (processHoldings) {
                            holdingsEntities.add(holdingsEntity);
                        }
                        for (ItemMarcRecord itemMarcRecord : itemMarcRecordList) {
                            Record itemRecord = itemMarcRecord.getItemRecord();
                            Map<String, Object> itemMap = processAndValidateItemEntity(bibliographicEntity, holdingsEntity, institutionEntity, holdingsCallNumber, holdingsCallNumberType, itemRecord,currentDate,errorMessage);
                            commonUtil.addItemAndReportEntities(itemEntities, reportEntities, processHoldings, holdingsEntity, itemMap);
                        }
                    } else {
                        errorMessage.append(" No Items Found in Incoming Marc Record for Owning Institution Holdings Id - ").append(holdingsEntity.getOwningInstitutionHoldingsId()).append(", Owning Institution Bib Id - ").append(bibliographicEntity.getOwningInstitutionBibId());
                    }

                }
                bibliographicEntity.setHoldingsEntities(holdingsEntities);
                bibliographicEntity.setItemEntities(itemEntities);
            } else {
                errorMessage.append(" No Holdings Found in Incoming Marc Record for Owning Institution Bib Id - ").append(bibliographicEntity.getOwningInstitutionBibId());
                bibliographicEntity = null;
            }

            if (processBib) {
                map.put(ScsbConstants.BIBLIOGRAPHIC_ENTITY, bibliographicEntity);
            }
        } catch (Exception e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
            errorMessage.append(e.getMessage());
        }
        map.put("errorMessage",errorMessage);
        return map;
    }

    private Map<String, Object> processAndValidateBibliographicEntity(Record bibRecord, InstitutionEntity institutionEntity,Date currentDate,StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        String owningInstitutionBibId = marcUtil.getControlFieldValue(bibRecord, "001");
        if (StringUtils.isNotBlank(owningInstitutionBibId)) {
            bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        } else {
            errorMessage.append(" Owning Institution Bib Id cannot be null");
        }
        if (institutionEntity != null) {
            bibliographicEntity.setOwningInstitutionId(institutionEntity.getId());
            bibliographicEntity.setInstitutionEntity(institutionEntity);
        } else {
            errorMessage.append(" Owning Institution Id cannot be null");
        }
        bibliographicEntity.setCreatedDate(currentDate);
        bibliographicEntity.setCreatedBy(ScsbConstants.SUBMIT_COLLECTION);
        bibliographicEntity.setLastUpdatedDate(currentDate);
        bibliographicEntity.setLastUpdatedBy(ScsbConstants.SUBMIT_COLLECTION);
        return marcUtil.extractXmlAndSetEntityToMap(bibRecord, errorMessage, map, bibliographicEntity, getDbReportUtil());
    }

    private Map<String, Object> processAndValidateHoldingsEntity(BibliographicEntity bibliographicEntity, Record holdingsRecord, Date currentDate
    ,StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();
        String holdingsContent = new MarcUtil().writeMarcXml(holdingsRecord);
        HoldingsEntity holdingsEntity = commonUtil.buildHoldingsEntity(bibliographicEntity, currentDate, errorMessage, holdingsContent, ScsbConstants.SUBMIT_COLLECTION);
        String owningInstitutionHoldingsId = marcUtil.getDataFieldValue(holdingsRecord, "852", '0');
        if (errorMessage.toString().length() > 1) {
            //errorMessage.append(", Owning Institution Holdings Id - ").append(holdingsEntity.getOwningInstitutionHoldingsId()).append(", Owning Institution Bib Id - ").append(bibliographicEntity.getOwningInstitutionBibId());
            ReportEntity reportEntity = new ReportEntity();
            reportEntity.setFileName(ScsbCommonConstants.SUBMIT_COLLECTION_REPORT);
            reportEntity.setCreatedDate(new Date());
            reportEntity.setInstitutionName(bibliographicEntity.getInstitutionEntity().getInstitutionCode());
            reportEntity.setType(ScsbCommonConstants.SUBMIT_COLLECTION_FAILURE_REPORT);
            List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibHoldingsFailureReportEntity(bibliographicEntity, holdingsEntity, reportEntity.getInstitutionName(), holdingsRecord);
            ReportDataEntity errorReportDataEntity = new ReportDataEntity();
            errorReportDataEntity.setHeaderName(ScsbCommonConstants.ERROR_DESCRIPTION);
            errorReportDataEntity.setHeaderValue(errorMessage.toString());
            reportDataEntities.add(errorReportDataEntity);
            reportEntity.addAll(reportDataEntities);
            map.put("holdingsReportEntity", reportEntity);
        }
        return commonUtil.addHoldingsEntityToMap(map, holdingsEntity, owningInstitutionHoldingsId);
    }

    private Map<String, Object> processAndValidateItemEntity(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity, InstitutionEntity institutionEntity, String holdingsCallNumber, Character holdingsCallNumberType, Record itemRecord, Date currentDate,
                                                             StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();
        ItemEntity itemEntity = new ItemEntity();
        String itemBarcode = marcUtil.getDataFieldValue(itemRecord, "876", 'p');
        if (StringUtils.isNotBlank(itemBarcode)) {
            itemEntity.setBarcode(itemBarcode);
            map.put("itemBarcode",itemBarcode);
        } else {
            errorMessage.append(" Item Barcode cannot be null");
        }
        String ownerCode = marcUtil.getDataFieldValue(itemRecord, "876", 'z');
        if (StringUtils.isNotBlank(ownerCode)) {
            itemEntity.setCustomerCode(ownerCode);
        }
        String itemLibrary = marcUtil.getDataFieldValue(itemRecord, "876", 'k');
        if (StringUtils.isNotBlank(itemLibrary)) {
            itemEntity.setItemLibrary(itemLibrary);
        }

        itemEntity.setCallNumber(holdingsCallNumber);
        itemEntity.setCallNumberType(holdingsCallNumberType != null ? String.valueOf(holdingsCallNumberType) : "");
        String copyNumber = marcUtil.getDataFieldValue(itemRecord, "876", 't');
        if (StringUtils.isNotBlank(copyNumber) && NumberUtils.isCreatable(copyNumber)) {
            itemEntity.setCopyNumber(Integer.valueOf(copyNumber));
        }
        if (institutionEntity != null) {
            itemEntity.setOwningInstitutionId(institutionEntity.getId());
            itemEntity.setInstitutionEntity(institutionEntity);
        } else {
            errorMessage.append(" Owning Institution Id cannot be null");
        }
        String collectionGroupCode = marcUtil.getDataFieldValue(itemRecord, "876", 'x');
        if (StringUtils.isNotBlank(collectionGroupCode) && commonUtil.getCollectionGroupMap().containsKey(collectionGroupCode)) {
            itemEntity.setCollectionGroupId(commonUtil.getCollectionGroupMap().get(collectionGroupCode));
        }

        String useRestrictions = marcUtil.getDataFieldValue(itemRecord, "876", 'h');
        if (useRestrictions != null) {
            itemEntity.setUseRestrictions(useRestrictions);
        }

        itemEntity.setVolumePartYear(marcUtil.getDataFieldValue(itemRecord, "876", '3'));
        String owningInstitutionItemId = marcUtil.getDataFieldValue(itemRecord, "876", 'a');
        if (StringUtils.isNotBlank(owningInstitutionItemId)) {
            itemEntity.setOwningInstitutionItemId(owningInstitutionItemId);
        } else {
            errorMessage.append(" Item Owning Institution Id cannot be null");
        }

        itemEntity.setCreatedDate(currentDate);
        itemEntity.setCreatedBy(ScsbConstants.SUBMIT_COLLECTION);
        itemEntity.setLastUpdatedDate(currentDate);
        itemEntity.setLastUpdatedBy(ScsbConstants.SUBMIT_COLLECTION);

        if (errorMessage.toString().length() > 1) {
            //errorMessage.append(" for Owning Institution Item Id - ").append(itemEntity.getOwningInstitutionItemId()).append(", Owning Institution Holdings Id - ").append(holdingsEntity.getOwningInstitutionHoldingsId()).append(", Owning Institution Bib Id - ").append(bibliographicEntity.getOwningInstitutionBibId());
            ReportEntity reportEntity = new ReportEntity();
            reportEntity.setFileName(ScsbCommonConstants.SUBMIT_COLLECTION_REPORT);
            reportEntity.setCreatedDate(new Date());
            reportEntity.setInstitutionName(itemEntity.getInstitutionEntity().getInstitutionCode());
            reportEntity.setType(ScsbCommonConstants.SUBMIT_COLLECTION_FAILURE_REPORT);
            List<ReportDataEntity> reportDataEntities = dbReportUtil.generateBibHoldingsAndItemsFailureReportEntities(bibliographicEntity, holdingsEntity, itemEntity, reportEntity.getInstitutionName(), itemRecord);
            ReportDataEntity errorReportDataEntity = new ReportDataEntity();
            errorReportDataEntity.setHeaderName(ScsbCommonConstants.ERROR_DESCRIPTION);
            errorReportDataEntity.setHeaderValue(errorMessage.toString());
            reportDataEntities.add(errorReportDataEntity);
            reportEntity.addAll(reportDataEntities);
            map.put("itemReportEntity", reportEntity);
        }
        map.put("itemEntity", itemEntity);
        return map;
    }

    private Integer getOwningInstitutionId(BibMarcRecord bibMarcRecord) {
        Record itemRecord = bibMarcRecord.getHoldingsMarcRecords().get(0).getItemMarcRecordList().get(0).getItemRecord();
        String ownerCode = marcUtil.getDataFieldValue(itemRecord, "876", 'z');
        String imsLocation = marcUtil.getDataFieldValue(itemRecord, "876", 'l');
        ImsLocationEntity imsLocationEntity = imsLocationDetailsRepository.findByImsLocationCode(imsLocation);

        OwnerCodeEntity ownerCodeEntity;
        if(null != ownerCode) {
            ownerCodeEntity = ownerCodeDetailsRepository.findByOwnerCodeAndImsLocationId(ownerCode,imsLocationEntity.getId());
            return ownerCodeEntity.getInstitutionId();
        } else {
            String barcode = marcUtil.getDataFieldValue(bibMarcRecord.getHoldingsMarcRecords().get(0).getItemMarcRecordList().get(0).getItemRecord(), "876",'p');
            List<ItemEntity> itemEntityList = itemDetailsRepository.findByBarcode(barcode);
            return itemEntityList.get(0).getOwningInstitutionId();
        }
    }

    /**
     * Gets db report util.
     *
     * @return the db report util
     */
    public DBReportUtil getDbReportUtil() {
        return dbReportUtil;
    }

    /**
     * Sets db report util.
     *
     * @param dbReportUtil the db report util
     */
    public void setDbReportUtil(DBReportUtil dbReportUtil) {
        this.dbReportUtil = dbReportUtil;
    }
}
