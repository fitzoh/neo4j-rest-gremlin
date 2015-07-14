package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by fitz on 7/2/15.
 */
public class Neo4jEdge extends Neo4jElement implements Edge {

    private String label;
    private long inVId;
    private long outVId;

    protected Neo4jEdge(String label, long id, long inVId, long outVId, Neo4jGraph graph){
        super(id, graph);
        this.label = label;
        this.inVId = inVId;
        this.outVId = outVId;
    }

    public Iterator<Vertex> vertices(Direction direction) {
        switch (direction) {
            case OUT:
                return IteratorUtils.of(new Neo4jVertex(this.outVId, this.graph));
            case IN:
                return IteratorUtils.of(new Neo4jVertex(this.inVId, this.graph));
            default:
                return IteratorUtils.of(new Neo4jVertex(this.outVId, this.graph), new Neo4jVertex(this.inVId, this.graph));
        }
    }

    public String label() {
        return label;
    }

    @Override
    public <V> Property<V> property(String key) {
        if(this.removed) {
            throw Element.Exceptions.elementAlreadyRemoved(this.getClass(), this.id());
        }
        Iterator<Property<V>> iter = properties();
        while (iter.hasNext()){
            Property prop = iter.next();
            if (prop.key().equals(key)){
                return prop;
            }
        }
        return Property.empty();
    }

    @Override
    public <V> V value(String key) throws NoSuchElementException {
        if(this.removed) {
            throw Element.Exceptions.elementAlreadyRemoved(this.getClass(), this.id());
        }
        return (V) property(key).<V>value();
    }

    public <V> Property<V> property(String key, V value) {
        if(this.removed) {
            throw Element.Exceptions.elementAlreadyRemoved(this.getClass(), this.id());
        }
        String cypher = String.format("match ()-[r]->() where id(r) = {id} set r.%s = {value}", key);
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "id", id, "value", value);
        graph.executeCypher(statementList);
        return new Neo4jProperty(this, key, value);
    }

    public void remove() {
        if(this.removed) {
            throw Element.Exceptions.elementAlreadyRemoved(this.getClass(), this.id());
        }
        String cypher = "match ()-[r]->() where id(r) = {id} delete r";
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "id", id);
        graph.executeCypher(statementList);
        this.removed = true;
    }

    public <V> Iterator<Property<V>> properties(String... propertyKeys) {
        if(this.removed) {
            throw Element.Exceptions.elementAlreadyRemoved(this.getClass(), this.id());
        }
        String path = String.format("/db/data/relationship/%s/properties", id);
        Map<String, Object> response = graph.target.path(path).request(MediaType.APPLICATION_JSON_TYPE).get().readEntity(HashMap.class);
        if (propertyKeys.length == 0){
            return (Iterator) response.entrySet().stream().map(entry -> new Neo4jProperty(this, entry.getKey(), entry.getValue())).iterator();
        } else
            return (Iterator) Arrays.asList(propertyKeys).stream().map(key -> new Neo4jProperty(this, key, response.get(key))).iterator();

    }

    @Override
    public String toString() {
        return StringFactory.edgeString(this);
    }
}
