package org.recap.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.RecapCommonConstants;
import org.recap.RecapConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessionReportsRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReportsRouteBuilder.class);

    /**
     * This method instantiates a new reports route builder to save accession reports in database.
     *
     * @param camelContext    the camel context
     * @param reportProcessor the report processor
     */
    @Autowired
    public AccessionReportsRouteBuilder(CamelContext camelContext, ReportProcessor reportProcessor) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.ACCESSION_REPORT_Q + "?concurrentConsumers=10")
                            .routeId(RecapConstants.ACCESSION_REPORT_ROUTE_ID).threads(10)
                            .process(reportProcessor);
                }
            });
        } catch (Exception e) {
            logger.error(RecapCommonConstants.LOG_ERROR,e);
        }
    }

}
