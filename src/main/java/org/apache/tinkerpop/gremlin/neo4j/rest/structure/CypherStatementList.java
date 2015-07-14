package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by fitz on 7/2/15.
 */
public class CypherStatementList {

    List<CypherStatement> statements;

    public CypherStatementList(List<CypherStatement> statements){
        this.statements = new ArrayList(statements);
    }

    public CypherStatementList(CypherStatement... statements){
        this(Arrays.asList(statements));
    }

    public static CypherStatementList singleStatement(String statement, Object... parameters){
        return new CypherStatementList(new CypherStatement(statement, parameters));
    }

    public List<CypherStatement> getStatements(){
        return statements;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("statements", statements)
                .toString();
    }
}
