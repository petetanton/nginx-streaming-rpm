package uk.tanton.streaming.live.dynamo.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Date;

@DynamoDBTable(tableName = "OVERWRITTEN-BY-CONFIG")
public class StreamRecord {

    @DynamoDBHashKey(attributeName = "stream_id")
    private String streamId;

    @DynamoDBRangeKey(attributeName = "account_id")
    private int accountId;

    @DynamoDBAttribute
    @DynamoDBTypeConvertedEnum
    private StreamStatus streamStatus;

    @DynamoDBAttribute
    private String streamOrigin;

    @DynamoDBAttribute
    private Date dateStarted;

    @DynamoDBAttribute
    private Date dateEnded;

    public Date getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(Date dateEnded) {
        this.dateEnded = dateEnded;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public void setStreamOrigin(String streamOrigin) {
        this.streamOrigin = streamOrigin;
    }

    public void setStreamStatus(StreamStatus streamStatus) {
        this.streamStatus = streamStatus;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getStreamId() {
        return streamId;
    }

    public StreamStatus getStreamStatus() {
        return streamStatus;
    }

    public String getStreamOrigin() {
        return streamOrigin;
    }
}
