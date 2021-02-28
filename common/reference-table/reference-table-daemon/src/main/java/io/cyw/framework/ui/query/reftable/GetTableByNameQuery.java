package io.cyw.framework.ui.query.reftable;

public class GetTableByNameQuery {

    private final String tableName;

    public GetTableByNameQuery(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
