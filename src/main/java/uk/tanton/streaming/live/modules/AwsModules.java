package uk.tanton.streaming.live.modules;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class AwsModules {

    @Provides
    @Singleton
    AmazonDynamoDBAsync provideDynamoClient(@Named("awsProfile") AWSCredentialsProvider awsProfile) {
        return AmazonDynamoDBAsyncClientBuilder.standard().withCredentials(awsProfile).build();
    }

    @Provides
    @Named("awsProfile")
    AWSCredentialsProvider provideAwsProfile() {
        return new EC2ContainerCredentialsProviderWrapper();
    }
}
