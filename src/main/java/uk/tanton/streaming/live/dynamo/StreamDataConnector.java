package uk.tanton.streaming.live.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.dynamo.domain.AccountRecord;
import uk.tanton.streaming.live.dynamo.domain.Publisher;
import uk.tanton.streaming.live.dynamo.domain.StreamRecord;
import uk.tanton.streaming.live.exception.NoSuchAccountException;
import uk.tanton.streaming.live.exception.NoSuchPublisherException;
import uk.tanton.streaming.live.exception.NoSuchStreamException;

import javax.inject.Inject;

public class StreamDataConnector {
    private static final Logger LOG = LogManager.getLogger(StreamDataConnector.class);

    private final DynamoDBMapper mapper;
    private final DynamoTableConfig config;
    private final Cache<String, Publisher> publisherCache;
    private final Cache<Integer, AccountRecord> accountCache;

    @Inject
    public StreamDataConnector(DynamoDBMapper dynamoMapper, DynamoTableConfig config) {
        this.mapper = dynamoMapper;
        this.config = config;
        this.publisherCache = new Cache<>(s -> mapper.load(Publisher.class, s, configForTable(config.getPublishersTable())));
        this.accountCache = new Cache<>(i -> mapper.load(AccountRecord.class, i, configForTable(config.getAccountsTable())));
    }


    private static DynamoDBMapperConfig configForTable(String table) {
        return DynamoDBMapperConfig.builder().withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(table)).build();
    }

    public Publisher getPublisher(String username) throws NoSuchPublisherException {
        Publisher publisher = publisherCache.get(username);

        if (publisher == null) throw new NoSuchPublisherException(username);
        return publisher;
    }

    public AccountRecord getAccount(int accountId) throws NoSuchAccountException {
        AccountRecord accountRecord = accountCache.get(accountId);
        if (accountRecord == null) throw new NoSuchAccountException(accountId);
        return accountRecord;
    }

    public StreamRecord getStreamRecord(String streamId, int accountId) throws NoSuchStreamException {
        StreamRecord stream = mapper.load(StreamRecord.class, streamId, accountId, configForTable(config.getStreamsTable()));
        if (stream == null) throw new NoSuchStreamException(streamId, accountId);
        return stream;
    }

    public void updateStreamRecord(StreamRecord streamRecord) {
        mapper.save(streamRecord, configForTable(config.getStreamsTable()));
    }

}
