package org.recap.camel.submitcollection.processor;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.camel.EmailPayLoad;
import org.recap.model.reports.ReportDataRequest;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.service.common.SetupDataService;
import org.recap.service.submitcollection.SubmitCollectionBatchService;
import org.recap.service.submitcollection.SubmitCollectionReportGenerator;
import org.recap.service.submitcollection.SubmitCollectionService;
import org.recap.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by premkb on 19/3/17.
 */
@Service
@Scope("prototype")
public class SubmitCollectionProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionProcessor.class);

    @Autowired
    private SubmitCollectionService submitCollectionService;

    @Autowired
    private SubmitCollectionBatchService submitCollectionBatchService;

    @Autowired
    private SubmitCollectionReportGenerator submitCollectionReportGenerator;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    PropertyUtil propertyUtil;

    @Value("${" + PropertyKeyConstants.EMAIL_SUBMIT_COLLECTION_SUBJECT + "}")
    private String submitCollectionEmailSubject;

    @Value("${" + PropertyKeyConstants.EMAIL_SUBMIT_COLLECTION_SUBJECT_FOR_EMPTY_DIRECTORY + "}")
    private String subjectForEmptyDirectory;

    @Value("${" + PropertyKeyConstants.S3_SUBMIT_COLLECTION_REPORT_DIR + "}")
    private String submitCollectionReportS3Dir;

    private String institutionCode;
    private boolean isCGDProtection;
    private String cgdType;

    @Autowired
    private SetupDataService setupDataService;

    @Autowired
    AmazonS3 awsS3Client;

    @Value("${" + PropertyKeyConstants.S3_SUBMIT_COLLECTION_DIR + "}")
    private String submitCollectionS3BasePath;

    @Value("${" + PropertyKeyConstants.SCSB_BUCKET_NAME + "}")
    private String bucketName;

    @Value("${" + PropertyKeyConstants.SUBMIT_COLLECTION_USE_SOLR_PARTIAL_INDEX_TOTAL_DOCS_SIZE + ":1000}")
    private int solrMaxDocSizeToUsePartialIndex;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    public SubmitCollectionProcessor() {
    }

    public SubmitCollectionProcessor(String inputInstitutionCode, boolean isCGDProtection, String cgdType) {
        this.institutionCode = inputInstitutionCode;
        this.isCGDProtection = isCGDProtection;
        this.cgdType=cgdType;
    }

    /**
     * Process input.
     *
     * @param exchange the exchange
     * @throws Exception the exception
     */
    public void processInput(Exchange exchange) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Set<Integer> processedBibIds = new HashSet<>();
        Set<String> updatedBoundWithDummyRecordOwnInstBibIdSet = new HashSet<>();
        List<Map<String, String>> idMapToRemoveIndexList = new ArrayList<>();
        List<Map<String, String>> bibIdMapToRemoveIndexList = new ArrayList<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        String xmlFileName = null;
        ExecutorService executorService = null;
        try {
            logger.info("Submit Collection : Route started and started processing the records from s3 for submitcollection");
            String inputXml = exchange.getIn().getBody(String.class);
            xmlFileName = exchange.getIn().getHeader(ScsbConstants.CAMEL_FILE_NAME_ONLY).toString();
            xmlFileName = submitCollectionS3BasePath+ institutionCode+ ScsbCommonConstants.PATH_SEPARATOR + "cgd_" + cgdType + ScsbCommonConstants.PATH_SEPARATOR + xmlFileName;
            logger.info("Processing xmlFileName----->{}", xmlFileName);
            Integer institutionId = setupDataService.getInstitutionCodeIdMap().get(institutionCode);
            executorService = Executors.newFixedThreadPool(10);
            List<Future> futures = new ArrayList<>();
            submitCollectionBatchService.process(institutionCode, inputXml, processedBibIds, idMapToRemoveIndexList, bibIdMapToRemoveIndexList, xmlFileName, reportRecordNumList, false, isCGDProtection, updatedBoundWithDummyRecordOwnInstBibIdSet, exchange, executorService, futures);
            logger.info("Submit Collection : Solr indexing started for {} records", processedBibIds.size());
            performIndexing(processedBibIds);
            performIndexingByOwningInstitutionBibIds(updatedBoundWithDummyRecordOwnInstBibIdSet, institutionId);
            performIndexingToRemoveBibs(idMapToRemoveIndexList, bibIdMapToRemoveIndexList);
            collectFuturesAndProcess(futures);
            executorService.shutdown();
            ReportDataRequest reportRequest = getReportDataRequest(xmlFileName);
            String generatedReportFileName = submitCollectionReportGenerator.generateReport(reportRequest);
            producer.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoad(xmlFileName, generatedReportFileName), ScsbConstants.EMAIL_BODY_FOR, ScsbConstants.SUBMIT_COLLECTION);
            if (awsS3Client.doesObjectExist(bucketName, xmlFileName) && (inputXml != null && !inputXml.equals(""))) {
                String basepath = xmlFileName.substring(0, xmlFileName.lastIndexOf('/'));
                String fileName = xmlFileName.substring(xmlFileName.lastIndexOf('/'));
                awsS3Client.copyObject(bucketName, xmlFileName, bucketName, basepath + "/.done-" + institutionCode + "-cgd_" + cgdType + fileName);
                awsS3Client.deleteObject(bucketName, xmlFileName);
            }
            stopWatch.stop();
            logger.info("Submit Collection : Total time taken for processing through s3---> {} sec", stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            logger.info("Caught for institution inside catch block {} ",institutionCode);
            logger.error(ScsbCommonConstants.LOG_ERROR, e);
            exchange.setException(e);
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    private void performIndexingToRemoveBibs(List<Map<String, String>> idMapToRemoveIndexList, List<Map<String, String>> bibIdMapToRemoveIndexList) {
        logger.info("Submit Collection : Solr indexing completed and remove the incomplete record from solr index for {} records", idMapToRemoveIndexList.size());
        if (!idMapToRemoveIndexList.isEmpty() || !bibIdMapToRemoveIndexList.isEmpty()) {//remove the incomplete record from solr index
            StopWatch stopWatchRemovingDummy = new StopWatch();
            stopWatchRemovingDummy.start();
            logger.info("Calling indexing to remove dummy records");
            new Thread(() -> {
                try {
                    submitCollectionBatchService.removeBibFromSolrIndex(bibIdMapToRemoveIndexList);
                    submitCollectionBatchService.removeSolrIndex(idMapToRemoveIndexList);
                    logger.info("Removed dummy records from solr");
                } catch (Exception e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR, e);
                }
            }).start();
            stopWatchRemovingDummy.stop();
            logger.info("Time take to call and execute solr call to remove dummy-->{} sec", stopWatchRemovingDummy.getTotalTimeSeconds());
        }
    }

    private void performIndexingByOwningInstitutionBibIds(Set<String> updatedBoundWithDummyRecordOwnInstBibIdSet, Integer institutionId) {
        if (!updatedBoundWithDummyRecordOwnInstBibIdSet.isEmpty()) {
            logger.info("Updated boundwith dummy record own inst bib id size-->{}", updatedBoundWithDummyRecordOwnInstBibIdSet.size());
            submitCollectionService.indexDataUsingOwningInstBibId(new ArrayList<>(updatedBoundWithDummyRecordOwnInstBibIdSet), institutionId);
        }
    }

    private void performIndexing(Set<Integer> processedBibIds) {
        if (!processedBibIds.isEmpty()) {
            StopWatch stopWatchSolrIndexing = new StopWatch();
            stopWatchSolrIndexing.start();
            String indexingStatus = null;
            if (processedBibIds.size() > solrMaxDocSizeToUsePartialIndex) { // If the number of bib Ids is greater than configured value, default is 1000, index data with multi-threading using partial index api
                indexingStatus = submitCollectionBatchService.partialIndexData(processedBibIds);
            } else { // If the number of bib Ids is less than configured value, default is 1000, index data without multi-threading
                indexingStatus = submitCollectionBatchService.indexData(processedBibIds);
            }
            logger.info("Submit Collection : Solr indexing Status - {}", indexingStatus);
            stopWatchSolrIndexing.stop();
            logger.info("Submit Collection : Total Time taken to do solr indexing : {} sec", stopWatchSolrIndexing.getTotalTimeSeconds());
        }
    }

    public void caughtException(Exchange exchange) {
        logger.info("inside caught exception..........");
        Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        logger.info("Headers - institution - {}, isCgdProtected - {}, cgdType - {} ", exchange.getIn().getHeader(ScsbCommonConstants.INSTITUTION),
                exchange.getIn().getHeader(ScsbCommonConstants.IS_CGD_PROTECTED), exchange.getIn().getHeader(ScsbConstants.CGG_TYPE));
        if (exception != null) {
            String fileName = (String) exchange.getIn().getHeader(Exchange.FILE_NAME);
            String filePath = (String) exchange.getIn().getHeader(Exchange.FILE_PARENT);
            String institutionCode1 = (String) exchange.getIn().getHeader(ScsbCommonConstants.INSTITUTION);
            logger.info("Institution inside caught  - {}", institutionCode1);
            logger.info("Exception occured is - {}", exception.getMessage());
            producer.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoadForExcepion(institutionCode1, fileName, filePath, exception, exception.getMessage()), ScsbConstants.EMAIL_BODY_FOR, ScsbConstants.SUBMIT_COLLECTION_EXCEPTION);
        }
    }

    private void collectFuturesAndProcess(List<Future> futures) {
        logger.info("Before Collecting Futures - Number of Futures for Match Point Checks: {}", futures.size());
        Map<Integer, Set<Integer>> responseMap = new HashMap<>();
        Set<Integer> bibIds = new HashSet<>();
        Set<Integer> bibIdsToResetAndSetQualifierTo1 = new HashSet<>();
        Set<Integer> bibIdsToSetQualifierTo2 = new HashSet<>();
        Set<Integer> bibIdsToResetAndSetQualifierTo3 = new HashSet<>();
        for (Future future : futures) {
            try {
                responseMap = (Map<Integer, Set<Integer>>) future.get();
                if (!responseMap.isEmpty()) {
                    bibIdsToResetAndSetQualifierTo1 = responseMap.get(ScsbConstants.MA_QUALIFIER_1);
                    bibIdsToSetQualifierTo2 = responseMap.get(ScsbConstants.MA_QUALIFIER_2);
                    bibIdsToResetAndSetQualifierTo3 = responseMap.get(ScsbConstants.MA_QUALIFIER_3);
                    if (bibIdsToResetAndSetQualifierTo1 != null) {
                        bibIds.addAll(bibIdsToResetAndSetQualifierTo1);
                    } else if (bibIdsToSetQualifierTo2 != null) {
                        bibIds.addAll(bibIdsToSetQualifierTo2);
                    } else if (bibIdsToResetAndSetQualifierTo3 != null) {
                        bibIds.addAll(bibIdsToResetAndSetQualifierTo3);
                    }
                }
            } catch (Exception e) {
                logger.error(ScsbCommonConstants.LOG_ERROR, e);
            }
        }
        logger.info("After Collecting Futures - Number of Bib Ids Collected for MA Qualifier Update: {}", bibIds.size());
        if (bibIdsToResetAndSetQualifierTo1 != null && !bibIdsToResetAndSetQualifierTo1.isEmpty()) {
            int countOfUpdatedTo1 = bibliographicDetailsRepository.resetMatchingColumnsAndUpdateMaQualifier(bibIdsToResetAndSetQualifierTo1, ScsbConstants.MA_QUALIFIER_1);
            logger.info("Number of Bib Ids Updated with MA Qualifier - {} : {}", ScsbConstants.MA_QUALIFIER_1, countOfUpdatedTo1);
        }
        if (bibIdsToSetQualifierTo2 != null && !bibIdsToSetQualifierTo2.isEmpty()) {
            int countOfUpdatedTo2 = bibliographicDetailsRepository.updateMaQualifier(bibIdsToSetQualifierTo2, ScsbConstants.MA_QUALIFIER_2);
            logger.info("Number of Bib Ids Updated with MA Qualifier - {} : {}", ScsbConstants.MA_QUALIFIER_2, countOfUpdatedTo2);
        }
        if (bibIdsToResetAndSetQualifierTo3 != null && !bibIdsToResetAndSetQualifierTo3.isEmpty()) {
            int countOfUpdatedTo3 = bibliographicDetailsRepository.resetMatchingColumnsAndUpdateMaQualifier(bibIdsToResetAndSetQualifierTo3, ScsbConstants.MA_QUALIFIER_3);
            logger.info("Number of Bib Ids Updated with MA Qualifier - {} : {}", ScsbConstants.MA_QUALIFIER_3, countOfUpdatedTo3);
        }
        if (!bibIds.isEmpty()) {
            logger.info("Submit Collection : Solr indexing started for MA Qualifier Update. Total Bib Records : {}", bibIds.size());
            performIndexing(bibIds);
        }
    }

    private EmailPayLoad getEmailPayLoadForExcepion(String institutionCode, String name, String filePath, Exception exception, String exceptionMessage) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(ScsbConstants.SUBJECT_FOR_SUBMIT_COL_EXCEPTION);
        emailPayLoad.setXmlFileName(name);
        logger.info("Institution inside email payload for exception- {}", institutionCode);
        emailPayLoad.setTo(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_SUBMIT_COLLECTION_TO));
        emailPayLoad.setCc(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_SUBMIT_COLLECTION_CC));
        emailPayLoad.setLocation(submitCollectionReportS3Dir);
        emailPayLoad.setLocation(filePath);
        emailPayLoad.setInstitution(institutionCode.toUpperCase());
        emailPayLoad.setException(exception);
        emailPayLoad.setExceptionMessage(exceptionMessage);
        return emailPayLoad;

    }

    private ReportDataRequest getReportDataRequest(String xmlFileName) {
        ReportDataRequest reportRequest = new ReportDataRequest();
        logger.info("filename--->{}-{}", ScsbCommonConstants.SUBMIT_COLLECTION_REPORT, xmlFileName);
        reportRequest.setFileName(ScsbCommonConstants.SUBMIT_COLLECTION_REPORT + "-" + xmlFileName);
        reportRequest.setInstitutionCode(institutionCode.toUpperCase());
        reportRequest.setReportType(ScsbCommonConstants.SUBMIT_COLLECTION_SUMMARY);
        reportRequest.setTransmissionType(ScsbCommonConstants.FTP);
        return reportRequest;
    }

    private EmailPayLoad getEmailPayLoad(String xmlFileName, String reportFileName) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(submitCollectionEmailSubject);
        emailPayLoad.setReportFileName(reportFileName);
        emailPayLoad.setXmlFileName(xmlFileName);
        logger.info("Institution inside email payload - {}", institutionCode);
        emailPayLoad.setTo(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_SUBMIT_COLLECTION_TO));
        emailPayLoad.setCc(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_SUBMIT_COLLECTION_CC));
        emailPayLoad.setLocation(submitCollectionReportS3Dir);
        emailPayLoad.setInstitution(institutionCode.toUpperCase());
        return emailPayLoad;
    }

    /**
     * This method is used to send email when there are no files in the respective directory
     */
    public void sendEmailForEmptyDirectory() {
        String s3Path = submitCollectionS3BasePath+ institutionCode + "/cgd_" + cgdType;
        producer.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoadForNoFiles(institutionCode,s3Path), ScsbConstants.EMAIL_BODY_FOR, ScsbConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
    }

    private EmailPayLoad getEmailPayLoadForNoFiles(String institutionCode, String ftpLocationPath) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(subjectForEmptyDirectory+" - "+institutionCode+" - "+cgdType);
        emailPayLoad.setLocation(ftpLocationPath);
        emailPayLoad.setTo(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_SUBMIT_COLLECTION_NOFILES_TO));
        return  emailPayLoad;
    }
}
