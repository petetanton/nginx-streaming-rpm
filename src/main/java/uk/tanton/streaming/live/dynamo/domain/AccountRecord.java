package uk.tanton.streaming.live.dynamo.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "OVERWRITTEN-BY-CONFIG")
public class AccountRecord {

    @DynamoDBHashKey(attributeName = "account_id")
    private int accountId;

    @DynamoDBAttribute
    private String name;

    @DynamoDBAttribute
    private String email;

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
