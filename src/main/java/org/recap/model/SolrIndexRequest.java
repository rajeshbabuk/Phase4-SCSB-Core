package org.recap.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by SheikS on 6/18/2016.
 */
@Data
public class SolrIndexRequest {
    private String docType;
    private Integer numberOfThreads;
    private Integer numberOfDocs;
    private Integer commitInterval;
    private String owningInstitutionCode;
    private boolean doClean;
    private String dateFrom;
    private String dateTo;

    private String matchingCriteria;
    private String reportType;
    private String transmissionType;
    private Date createdDate;
    private String processType;
    private Date toDate;

    private String fromBibId;
    private String toBibId;
    private String partialIndexType;
    private String bibIds;

    private String replaceRequestByType;
    private String requestStatus;
    private String requestIds;
    private String startRequestId;
    private String endRequestId;
    private String fromDate;
    private String matchBy;
}
