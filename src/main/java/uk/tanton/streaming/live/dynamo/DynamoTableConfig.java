package uk.tanton.streaming.live.dynamo;

public class DynamoTableConfig {

    private final String accountsTable;
    private final String publishersTable;
    private final String streamsTable;

    public DynamoTableConfig(String accountsTable, String publishersTable, String streamsTable) {
        this.accountsTable = accountsTable;
        this.publishersTable = publishersTable;
        this.streamsTable = streamsTable;
    }

    public String getAccountsTable() {
        return accountsTable;
    }

    public String getPublishersTable() {
        return publishersTable;
    }

    public String getStreamsTable() {
        return streamsTable;
    }

    @Override
    public String toString() {
        return "DynamoTableConfig{" +
                "accountsTable='" + accountsTable + '\'' +
                ", publishersTable='" + publishersTable + '\'' +
                ", streamsTable='" + streamsTable + '\'' +
                '}';
    }
}
