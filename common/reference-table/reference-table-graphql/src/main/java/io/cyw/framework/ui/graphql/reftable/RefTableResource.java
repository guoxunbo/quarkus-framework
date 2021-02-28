package io.cyw.framework.ui.graphql.reftable;

import io.cyw.framework.messaging.responsetypes.ResponseTypes;
import io.cyw.framework.queryhandling.QueryGateway;
import io.cyw.framework.ui.entity.ReferenceTable;
import io.cyw.framework.ui.query.reftable.GetTableByNameQuery;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@GraphQLApi
public class RefTableResource {

    @Inject
    private QueryGateway queryGateway;

    @Query("refTable")
    @Description("get referenceTable By name")
    public CompletionStage<Optional<ReferenceTable>> table(@Name("tableName") String name) {
        return queryGateway.query(new GetTableByNameQuery(name), ResponseTypes.optionalInstanceOf(ReferenceTable.class));
    }

}
