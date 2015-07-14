package org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

/**
 * Created by fitz on 7/3/15.
 */
public class CypherResponse {
    List<Object> results;
    List<Object> errors;

    public CypherResponse(){

    }

    public CypherResponse(List<Object> results, List<Object> errors){
        this.results = results;
        this.errors = errors;
    }

    public List<Object> getResults(){
        return results;
    }

    public List<Object> getErrors(){
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CypherResponse that = (CypherResponse) o;
        return Objects.equals(results, that.results) &&
                Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, errors);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("results", results)
                .add("errors", errors)
                .toString();
    }
}
