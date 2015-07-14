package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity.EdgeData;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity.EdgeResponseIterator;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity.VertexResponseIterator;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * Created by fitz on 7/2/15.
 */
public class Neo4jVertex extends Neo4jElement implements Vertex {

    protected Neo4jVertex(long id, Neo4jGraph graph){
        super(id, graph);
    }

    public Edge addEdge(String label, Vertex inVertex, Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        if (ElementHelper.getIdValue(keyValues).isPresent())
            throw Vertex.Exceptions.userSuppliedIdsNotSupported();

        long inId = ((Neo4jVertex) inVertex).id;
        String cypher = String.format("match n where id(n) = {inId} match m where id(m) = {outId} create (n)-[r:%s {properties}]->(m) return id(r)", label);
        Map<String, Object> properties = ElementHelper.asMap(keyValues);
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "inId", inId,
                "outId", id, "properties", properties);
        String response = graph.executeCypher(statementList).readEntity(String.class);
        ObjectMapper mapper = new ObjectMapper();
        try{
            long edgeId = mapper.readTree(response).get("results").get(0).get("data").get(0).get("row").get(0).asLong();
            return new Neo4jEdge(label, edgeId, inId, this.id, graph);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public <V> VertexProperty<V> property(String key, V value) {
        String cypher = String.format("match n where id(n) = {id} set n.%s = {value}", key);
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "id", id, "value", value);
        graph.executeCypher(statementList);
        return new Neo4jVertexProperty(this, key, value);
    }

    public <V> VertexProperty<V> property(String key, V value, Object... keyValues) {
        return null;
    }

    public <V> VertexProperty<V> property(VertexProperty.Cardinality cardinality, String key, V value, Object... keyValues) {
        return property(key, value);
    }

    public Iterator<Edge> edges(Direction direction, String... edgeLabels) {
        String cypher;
        if (direction.equals(Direction.IN)){
            cypher = "match (n)-[r]->(m) where id(n) = {id} return type(r), id(r), id(n), id(m)";
        } else if (direction.equals(Direction.OUT)){
            cypher = "match (n)<-[r]-(m) where id(n) = {id} return type(r), id(r), id(n), id(m)";
        } else {
            cypher = "match (n)-[r]-(m) where id(n) = {id} return type(r), id(r), id(n), id(m)";
        }
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "id", id);
        InputStream response = graph.executeCypher(statementList).readEntity(InputStream.class);
        Iterator<EdgeData> edgeDataIterator = new EdgeResponseIterator(response);
        return IteratorUtils.map(edgeDataIterator, ed -> new Neo4jEdge(ed.label, ed.id, ed.inId, ed.outId, this.graph));
    }

    public Iterator<Vertex> vertices(Direction direction, String... edgeLabels) {
        String cypher;
        if (direction.equals(Direction.IN)){
            cypher = "match (n)-[r]->(m) where id(n) = {id} return id(m)";
        } else if (direction.equals(Direction.OUT)){
            cypher = "match (n)<-[r]-(m) where id(n) = {id} return id(m)";
        } else {
            cypher = "match (n)-[r]-(m) where id(n) = {id} return id(m)";
        }
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "id", id);
        InputStream response = graph.executeCypher(statementList).readEntity(InputStream.class);
        Iterator<Long> idIterator = new VertexResponseIterator(response);
        return IteratorUtils.map(idIterator, id -> new Neo4jVertex(id, this.graph));
    }

    public Object id() {
        return id;
    }

    public String label() {
        String cypher = "match n where id(n) = {id}  return labels(n) ";
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "id", id);
        String response = graph.executeCypher(statementList).readEntity(String.class);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(response).get("results").get(0).get("data").get(0).get("row").get(0).get(0).textValue();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public Graph graph() {
        return graph;
    }

    public void remove() {
        String cypher = "match n where id(n) = {id} delete n";
        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "id", id);
        graph.executeCypher(statementList);
    }

    public <V> Iterator<VertexProperty<V>> properties(String... propertyKeys) {
        String path = String.format("/db/data/node/%s/properties", id);
        Map<String, Object> response = graph.target.path(path).request(MediaType.APPLICATION_JSON_TYPE).get().readEntity(HashMap.class);
        if (propertyKeys.length == 0){
            return (Iterator) response.entrySet().stream().map(entry -> new Neo4jVertexProperty(this, entry.getKey(), entry.getValue())).iterator();
        } else
            return (Iterator) Arrays.asList(propertyKeys).stream().map(key -> new Neo4jVertexProperty(this, key, response.get(key))).iterator();
    }

    @Override
    public boolean equals(Object o) {
        return ElementHelper.areEqual(this, o);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

    @Override
    public String toString() {
        return StringFactory.vertexString(this);
    }
}
