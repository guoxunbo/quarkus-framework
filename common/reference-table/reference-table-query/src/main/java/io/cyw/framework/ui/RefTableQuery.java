package io.cyw.framework.ui;

import io.cyw.framework.queryhandling.QueryHandler;
import io.cyw.framework.ui.entity.ReferenceTable;
import io.cyw.framework.ui.query.reftable.GetTableByNameQuery;

public class RefTableQuery {

    @QueryHandler
    public ReferenceTable getByName(GetTableByNameQuery query) {
        // todo  getFieldByTableName
        return null;
    }
}
