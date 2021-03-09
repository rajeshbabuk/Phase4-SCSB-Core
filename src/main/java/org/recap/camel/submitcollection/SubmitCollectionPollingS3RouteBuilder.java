package org.recap.camel.submitcollection;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.RecapCommonConstants;
import org.recap.RecapConstants;
import org.recap.camel.submitcollection.processor.SubmitCollectionProcessor;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by premkb on 19/3/17.
 */
@Component
public class SubmitCollectionPollingS3RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionPollingS3RouteBuilder.class);

    @Autowired
    ProducerTemplate producer;

    @Autowired
    CamelContext camelContext;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    PropertyUtil propertyUtil;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Value("${s3.submit.collection.dir}")
    private String submitCollectionS3BasePath;

    @Autowired
    AmazonS3 awsS3Client;

    @Value("${scsbBucketName}")
    private String scsbBucketName;

    @Value("${submit.collection.local.dir}")
    private String submitCollectionLocalWorkingDir;

    /**
     * Predicate to identify is the input file is gz
     */
    Predicate gzipFile = exchange -> {
        if (exchange.getIn().getHeader("CamelFileNameOnly") != null) {
            String fileName = exchange.getIn().getHeader("CamelFileNameOnly").toString();
            return StringUtils.equalsIgnoreCase("gz", FilenameUtils.getExtension(fileName));
        } else {
            return false;
        }
    };

    public SubmitCollectionPollingS3RouteBuilder(CamelContext camelContext, ApplicationContext applicationContext) {
        try {
            //This route is used to send message to queue which is used in controller to identify the completion of submit collection process
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.SUBMIT_COLLECTION_COMPLETION_QUEUE_FROM)
                            .routeId(RecapConstants.SUBMIT_COLLECTION_COMPLETED_ROUTE)
                            .log("Completed Submit Collection Process")
                            .process(exchange -> exchange.getIn().setBody("Submit collection process completed sucessfully in sequential order"))
                            .to(RecapConstants.SUBMIT_COLLECTION_COMPLETION_QUEUE_TO)
                            .end();
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapCommonConstants.DIRECT_ROUTE_FOR_EXCEPTION)
                            .log("Calling direct route for exception")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class, "${in.header.institution", "${in.header.isCGDProtected", "${in.header.cgdType"), RecapConstants.SUBMIT_COLLECTION_CAUGHT_EXCEPTION_METHOD);

                }
            });
        } catch (Exception e) {
            logger.info("Exception occurred while instantiating submit collection route definitions : {}", e.getMessage());
        }
    }

    public void createRoutesForSubmitCollection() {
        List<String> protectedAndNotProtected = Arrays.asList(RecapConstants.PROTECTED, RecapConstants.NOT_PROTECTED);
        String nextInstitution = null;
        List<String> allInstitutionCodeExceptHTC = institutionDetailsRepository.findAllInstitutionCodeExceptHTC();
        for (int i = 0; i < allInstitutionCodeExceptHTC.size(); i++) {
            String currentInstitution = allInstitutionCodeExceptHTC.get(i);
            nextInstitution = (i < allInstitutionCodeExceptHTC.size() - 1) ? allInstitutionCodeExceptHTC.get(i + 1) : null;
            for (String cdgType : protectedAndNotProtected) {
                String nextRouteId = getNextRouteId(currentInstitution, nextInstitution, cdgType);
                if (RecapConstants.PROTECTED.equalsIgnoreCase(cdgType))
                    addRoutesToCamelContext(currentInstitution, cdgType, currentInstitution + RecapConstants.CGD_PROTECTED_ROUTE_ID, nextRouteId, true);
                else {
                    addRoutesToCamelContext(currentInstitution, cdgType, currentInstitution + RecapConstants.CGD_NOT_PROTECTED_ROUTE_ID, nextRouteId, false);
                }
            }
        }
    }

    private String getNextRouteId(String currentInstitution, String nextInstitution, String cdgType) {
        if (RecapConstants.PROTECTED.equalsIgnoreCase(cdgType)) {
            return currentInstitution + RecapConstants.CGD_NOT_PROTECTED_ROUTE_ID;
        } else if (StringUtils.isNotBlank(nextInstitution)) {
            return nextInstitution + RecapConstants.CGD_PROTECTED_ROUTE_ID;
        } else {
            return RecapConstants.SUBMIT_COLLECTION_COMPLETED_ROUTE;
        }
    }

    public void addRoutesToCamelContext(String currentInstitution, String cgdType, String currentInstitutionRouteId, String nextInstitutionRouteId, Boolean isCGDProtected) {
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(scsbBucketName);
            listObjectsRequest.setPrefix(submitCollectionS3BasePath + currentInstitution + "/cgd_" + cgdType + "/" + "scsb");
            ObjectListing objectListing = awsS3Client.listObjects(listObjectsRequest);

            for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
                getObjectContentToDrive(os.getKey(), currentInstitution, cgdType);
                logger.info("File with the key -->" + os.getKey() + " " + os.getSize() + " " + os.getLastModified());
            }

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    onCompletion()
                            .choice()
                            .when(exchangeProperty(RecapCommonConstants.CAMEL_BATCH_COMPLETE))
                            .process(exchange -> clearDirectory(currentInstitution, cgdType))
                            .log("OnCompletion executing for :" + currentInstitution + cgdType)
                            .to("controlbus:route?routeId=" + currentInstitutionRouteId + "&action=stop&async=true")
                            .delay(10)
                            .process(exchange -> startNextRouteInNewThread(exchange, nextInstitutionRouteId));
                    onException(Exception.class)
                            .log("Exception caught during submit collection process - " + currentInstitution + cgdType)
                            .handled(true)
                            .setHeader(RecapCommonConstants.INSTITUTION, constant(currentInstitution))
                            .setHeader(RecapCommonConstants.IS_CGD_PROTECTED, constant(isCGDProtected))
                            .setHeader(RecapConstants.CGG_TYPE, constant(cgdType))
                            .to(RecapCommonConstants.DIRECT_ROUTE_FOR_EXCEPTION);
                    from("file://"+ submitCollectionLocalWorkingDir + currentInstitution + "/cgd_" + cgdType + "?sendEmptyMessageWhenIdle=true&delete=true")
                            .routeId(currentInstitutionRouteId)
                            .noAutoStartup()
                            .choice()
                            .when(gzipFile)
                            .unmarshal()
                            .gzipDeflater()
                            .log(currentInstitution + "Submit Collection S3 Route Unzip Complete")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class, currentInstitution, isCGDProtected, cgdType), RecapConstants.PROCESS_INPUT)
                            .when(body().isNull())//This condition is satisfied when there are no files in the S3 directory(parameter-sendEmptyMessageWhenIdle=true)
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class, currentInstitution, isCGDProtected, cgdType), RecapConstants.SEND_EMAIL_FOR_EMPTY_DIRECTORY)
                            .log(currentInstitution + "-" + cgdType + " Directory is empty")
                            .otherwise()
                            .log("submit collection for " + currentInstitution + "-" + cgdType + " started")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class, currentInstitution, isCGDProtected, cgdType), RecapConstants.PROCESS_INPUT)
                            .log(currentInstitution + " Submit Collection " + cgdType + " S3 Route Record Processing completed")
                            .end();
                }
            });

        } catch (Exception e) {
            logger.error(RecapCommonConstants.LOG_ERROR, e.getMessage());
        }
    }

    private void getObjectContentToDrive(String fileName, String currentInstitution, String cgdType) {
        List<String> str = new ArrayList<>();
        String finalFileName = null;
        try {
            S3Object s3Object = awsS3Client.getObject(scsbBucketName, fileName);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            finalFileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            if (inputStream != null) {
                IOUtils.copy(inputStream, new FileOutputStream(new File(submitCollectionLocalWorkingDir + currentInstitution + "/cgd_" + cgdType + "/" + finalFileName)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startNextRouteInNewThread(Exchange exchange, String nextInstitutionRouteId) {
        Thread startThread;
        startThread = new Thread(() -> {
            try {
                if (nextInstitutionRouteId.contains("Complete")) {
                    producer.sendBody(RecapConstants.SUBMIT_COLLECTION_COMPLETION_QUEUE_FROM, exchange);
                } else {
                    camelContext.getRouteController().startRoute(nextInstitutionRouteId);
                }
            } catch (Exception e) {
                logger.info("Exception occured while starting next route : {}", e.getMessage());
                exchange.setException(e);
            }
        });
        startThread.start();
    }

    public void removeRoutesForSubmitCollection() throws Exception {
        logger.info(" Total routes before removing : {}", camelContext.getRoutesSize());
        List<String> protectedAndNotProtected = Arrays.asList(RecapConstants.PROTECTED, RecapConstants.NOT_PROTECTED);
        List<String> allInstitutionCodeExceptHTC = institutionDetailsRepository.findAllInstitutionCodeExceptHTC();
        for (String institution : allInstitutionCodeExceptHTC) {
            for (String cdgType : protectedAndNotProtected) {
                if (RecapConstants.PROTECTED.equalsIgnoreCase(cdgType)) {
                    camelContext.getRouteController().stopRoute(institution + RecapConstants.CGD_PROTECTED_ROUTE_ID);
                    camelContext.removeRoute(institution + RecapConstants.CGD_PROTECTED_ROUTE_ID);
                } else {
                    camelContext.getRouteController().stopRoute(institution + RecapConstants.CGD_NOT_PROTECTED_ROUTE_ID);
                    camelContext.removeRoute(institution + RecapConstants.CGD_NOT_PROTECTED_ROUTE_ID);
                }
            }
        }
        logger.info(" Total routes after removing : {}", camelContext.getRoutesSize());
    }

    public void clearDirectory(String institutionCode, String cgdType) {
        File destDirFile = new File(submitCollectionLocalWorkingDir + institutionCode + "/cgd_"+ cgdType);
        try {
            FileUtils.cleanDirectory(destDirFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
