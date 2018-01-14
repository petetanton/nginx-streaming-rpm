package uk.tanton.streaming.live.dynamo.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

@DynamoDBTable(tableName = "OVERWRITTEN-BY-CONFIG")
public class Publisher {
    @DynamoDBHashKey
    private String username;
    @DynamoDBAttribute
    private int accountId;
    @DynamoDBAttribute
    private String passwordHash;
    @DynamoDBAttribute
    private String passwordSalt;
    @DynamoDBAttribute
    private Date validFrom;
    @DynamoDBAttribute
    private Date validTo;

    public Publisher(int accountId, String passwordHash, String passwordSalt, String username, Date validFrom, Date validTo) {
        this.accountId = accountId;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.username = username;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Publisher() {
//        Empty constructor required for dynamo mapper
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Publisher publisher = (Publisher) o;

        return new EqualsBuilder()
                .append(username, publisher.username)
                .append(accountId, publisher.accountId)
                .append(passwordHash, publisher.passwordHash)
                .append(passwordSalt, publisher.passwordSalt)
                .append(validFrom, publisher.validFrom)
                .append(validTo, publisher.validTo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(username)
                .append(accountId)
                .append(passwordHash)
                .append(passwordSalt)
                .append(validFrom)
                .append(validTo)
                .toHashCode();
    }
}
