package uk.tanton.streaming.live.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import uk.tanton.streaming.live.dynamo.domain.Publisher;

import javax.inject.Inject;

public class StreamDataConnector {

    private final DynamoDBMapper mapper;
    private final DynamoTableConfig config;

    @Inject
    public StreamDataConnector(final DynamoDBMapper dynamoMapper, final DynamoTableConfig config) {
        this.mapper = dynamoMapper;
        this.config = config;
    }

    public Publisher getPublisher(final String username) {
        return mapper.load(Publisher.class, username, configForTable(config.getPublishersTable()));
    }



    private static DynamoDBMapperConfig configForTable(final String table) {
        return DynamoDBMapperConfig.builder().withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(table)).build();
    }
}
