package uk.tanton.streaming.live.modules;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.util.StringUtils;
import dagger.Module;
import dagger.Provides;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.dynamo.DynamoTableConfig;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Module
public class AwsModules {
    private static final Logger LOG = LogManager.getLogger(AwsModules.class);
    private static final String ACCOUNTS_TABLE = "uk.tanton.streaming.live.dynamo.accountsTable";
    private static final String PUBLISHERS_TABLE = "uk.tanton.streaming.live.dynamo.publishersTable";
    private static final String STREAM_TABLE = "uk.tanton.streaming.live.dynamo.streamTable";

    @Provides
    @Singleton
    @Named("dynamo")
    AmazonDynamoDBAsync provideDynamoClient() {
        return AmazonDynamoDBAsyncClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(getCredentialsProvider())
                .build();
    }

    @Provides
    @Singleton
    @Named("streamDataConnector")
    public StreamDataConnector provideStreamDataConnector(@Named("dynamo") AmazonDynamoDBAsync dynamo) {
        return new StreamDataConnector(new DynamoDBMapper(dynamo), getDynamoTableConfig());
    }

    @Provides
    @Singleton
    @Named("sqs")
    public AmazonSQS provideSQSClient() {
        return AmazonSQSClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(getCredentialsProvider())
                .build();
    }

    private AWSCredentialsProvider getCredentialsProvider() {

        return new DefaultAWSCredentialsProviderChain();
//        EnvironmentVariableCredentialsProvider environment = new EnvironmentVariableCredentialsProvider();
//        if (StringUtils.isNullOrEmpty(environment.getCredentials().getAWSAccessKeyId())) {
//            return new ProfileCredentialsProvider("pete-work");
//        } else {
//        }
    }


    private DynamoTableConfig getDynamoTableConfig() {
        DynamoTableConfig dynamoTableConfig;
        if (!StringUtils.isNullOrEmpty(System.getProperty(ACCOUNTS_TABLE))) {
            dynamoTableConfig = new DynamoTableConfig(
                    System.getProperty(ACCOUNTS_TABLE),
                    System.getProperty(PUBLISHERS_TABLE),
                    System.getProperty(STREAM_TABLE)
            );
        } else {
            Map<String, String> userData = new HashMap<>();
            final String userData1 = EC2MetadataUtils.getUserData();
            LOG.info(String.format("Userdata: %s", userData1));
            for (String s : userData1.split("\n")) {
                userData.put(s.split("=")[0], s.split("=")[1]);
            }
            dynamoTableConfig = new DynamoTableConfig(userData.get("accountsTable"), userData.get("publishersTable"), userData.get("streamsTable"));
        }

        LOG.info(String.format("Loading tables: %s", dynamoTableConfig.toString()));
        return dynamoTableConfig;
    }
}
