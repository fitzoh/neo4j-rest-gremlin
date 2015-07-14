package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.google.common.base.MoreObjects;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by fitz on 7/2/15.
 */

public class CypherStatement {

    private String statement;
    private Map<String, Object> parameters;

    public CypherStatement(String statement, Map<String, Object> parameters){
        this.statement = statement;
        this.parameters = parameters;
    }

    public CypherStatement(String statement, Object... paramKeyValues){
        this(statement, new HashMap());
        for (int i = 0; i < paramKeyValues.length; i+=2) {
            parameters.put((String) paramKeyValues[i], paramKeyValues[i+1]);
        }
    }

    public String getStatement(){
        return statement;
    }

    public Map<String, Object> getParameters(){
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CypherStatement statement1 = (CypherStatement) o;
        return Objects.equals(statement, statement1.statement) &&
                Objects.equals(parameters, statement1.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statement, parameters);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("statement", statement)
                .add("parameters", parameters)
                .toString();
    }
}
