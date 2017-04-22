package uk.tanton.streaming.live.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.dynamo.domain.Publisher;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StreamDataConnector {
    private static final Logger LOG = LogManager.getLogger(StreamDataConnector.class);

    private final DynamoDBMapper mapper;
    private final DynamoTableConfig config;
    private final LoadingCache<String, Publisher> publisherLoadingCache;

    @Inject
    public StreamDataConnector(final DynamoDBMapper dynamoMapper, final DynamoTableConfig config) {
        this.mapper = dynamoMapper;
        this.config = config;
        this.publisherLoadingCache = CacheBuilder.newBuilder()
                .maximumSize(5)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Publisher>() {
                    @Override
                    public Publisher load(String s) throws Exception {
                        LOG.info("loading cache for value " + s);
                        return mapper.load(Publisher.class, s, configForTable(config.getPublishersTable()));
                    }
                });
    }

    private static DynamoDBMapperConfig configForTable(final String table) {
        return DynamoDBMapperConfig.builder().withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(table)).build();
    }

    public Publisher getPublisher(final String username) {
        try {
            return this.publisherLoadingCache.get(username);
        } catch (ExecutionException e) {
            LOG.error(e);
            return mapper.load(Publisher.class, username, configForTable(config.getPublishersTable()));
        }
    }
}
