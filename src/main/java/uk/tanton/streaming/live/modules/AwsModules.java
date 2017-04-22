package uk.tanton.streaming.live.modules;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
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
import uk.tanton.streaming.live.dynamo.DynamoTableConfig;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Module
public class AwsModules {

    @Provides
    @Singleton
    @Named("dynamo")
    AmazonDynamoDBAsync provideDynamoClient() {
        return AmazonDynamoDBAsyncClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(new ProfileCredentialsProvider("pete-work"))
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
                .withCredentials(new ProfileCredentialsProvider("pete-work"))
                .build();
    }


    private DynamoTableConfig getDynamoTableConfig() {
        System.out.println("accounts prop: " + System.getProperty("uk.tanton.streaming.live.dynamo.accountsTable"));
        if (!StringUtils.isNullOrEmpty(System.getProperty("uk.tanton.streaming.live.dynamo.accountsTable"))) {
            return new DynamoTableConfig(
                    System.getProperty("uk.tanton.streaming.live.dynamo.accountsTable"),
                    System.getProperty("uk.tanton.streaming.live.dynamo.publishersTable"),
                    System.getProperty("uk.tanton.streaming.live.dynamo.streamTable")
            );
        } else {
            Map<String, String> userData = new HashMap<>();
            final String userData1 = EC2MetadataUtils.getUserData();
            System.out.println(String.format("Userdata: %s", userData1));
            for (String s : userData1.split("\n")) {
                userData.put(s.split("=")[0], s.split("=")[1]);
            }
            return new DynamoTableConfig(userData.get("accountsTable"), userData.get("publishersTable"), userData.get("streamsTable"));
        }
    }
}
