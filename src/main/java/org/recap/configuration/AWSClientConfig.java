package org.recap.configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.recap.PropertyKeyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSClientConfig {

    @Value("${" + PropertyKeyConstants.AWS_ACESSKEY + "}")
    private String awsAccessKey;

    @Value("${" + PropertyKeyConstants.AWS_ACCESS_SECRETKEY + "}")
    private String awsAccessSecretKey;

    @Bean
    public AmazonS3 getAwsClient() {
        AWSCredentials credentials = new BasicAWSCredentials(
                awsAccessKey, awsAccessSecretKey
        );

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_2)
                .withClientConfiguration(
                        new ClientConfiguration()
                .withTcpKeepAlive(true))
                .build();
    }

}
