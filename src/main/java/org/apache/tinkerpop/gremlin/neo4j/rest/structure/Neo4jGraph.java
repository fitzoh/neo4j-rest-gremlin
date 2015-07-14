package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity.EdgeData;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity.EdgeResponseIterator;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity.VertexResponseIterator;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by fitz on 7/1/15.
 */

@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
public class Neo4jGraph implements Graph {

    WebTarget target;

    protected Features features = new Neo4jGraphFeatures();

    protected Neo4jGraph(){
        target = ClientBuilder.newClient().register(JacksonFeature.class).target("http://localhost:7474");
    }

    public Neo4jGraph(Configuration configuration){
        this();
    }

    public static Neo4jGraph open(final Configuration configuration) {
        if (null == configuration) throw Graph.Exceptions.argumentCanNotBeNull("configuration");
        configuration.setProperty(Graph.GRAPH, Neo4jGraph.class.getName());
        return new Neo4jGraph(configuration);
    }

    public Vertex addVertex(Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        if (ElementHelper.getIdValue(keyValues).isPresent())
            throw Vertex.Exceptions.userSuppliedIdsNotSupported();

        Optional<String> label = ElementHelper.getLabelValue(keyValues);

        String cypher = label.isPresent() ?
                String.format("create (n:%s {properties}) return id(n)", label.get()) :
                "create (n {properties}) return id(n), n";

        Map<String, Object> properties = ElementHelper.remove(T.label, keyValues)
                .map(ElementHelper::asMap).orElse(Collections.emptyMap());

        CypherStatementList statementList = CypherStatementList.singleStatement(cypher, "properties", properties);
        String response = executeCypher(statementList).readEntity(String.class);
        ObjectMapper mapper = new ObjectMapper();
        try{
            long id = mapper.readTree(response).get("results").get(0).get("data").get(0).get("row").get(0).asLong();
            return new Neo4jVertex(id, this);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    public GraphComputer compute() throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    public Iterator<Vertex> vertices(Object... vertexIds) {
        String cypher;
        CypherStatementList statementList;
        if (vertexIds.length == 0){
            cypher = "match n return id(n)";
            statementList = CypherStatementList.singleStatement(cypher);
        } else {
            List<Long> idList = Stream.of(vertexIds)
                    .map(id -> {
                        if (id instanceof Number)
                            return ((Number) id).longValue();
                        else if (id instanceof String)
                            return Long.valueOf(id.toString());
                        else if (id instanceof Neo4jVertex) {
                            return (Long) ((Neo4jVertex) id).id();
                        } else
                            throw new IllegalArgumentException("Unknown vertex id type: " + id);
                    }).collect(Collectors.toList());
            cypher = "match n  where id(n) in {idList} return id(n)";
            statementList = CypherStatementList.singleStatement(cypher, "idList", idList);
        }
        InputStream response = executeCypher(statementList).readEntity(InputStream.class);
        Iterator<Long> idIter = new VertexResponseIterator(response);
        return IteratorUtils.map(idIter, id -> new Neo4jVertex(id, this));
    }

    public Iterator<Edge> edges(Object... edgeIds) {  String cypher;
        CypherStatementList statementList;
        if (edgeIds.length == 0){
            cypher = "match (n)-[r]->(m) return type(r), id(r), id(n), id(m)";
            statementList = CypherStatementList.singleStatement(cypher);
        } else {
            List<Long> idList = Stream.of(edgeIds)
                    .map(id -> {
                        if (id instanceof Number)
                            return ((Number) id).longValue();
                        else if (id instanceof String)
                            return Long.valueOf(id.toString());
                        else if (id instanceof Neo4jEdge) {
                            return (Long) ((Neo4jEdge) id).id();
                        } else
                            throw new IllegalArgumentException("Unknown edge id type: " + id);
                    }).collect(Collectors.toList());
            cypher = "match (n)-[r]->(m)  where id(r) in {idList} return  type(r), id(r), id(n), id(m)";
            statementList = CypherStatementList.singleStatement(cypher, "idList", idList);
        }
        InputStream response = executeCypher(statementList).readEntity(InputStream.class);
        Iterator<EdgeData> idIter = new EdgeResponseIterator(response);
        return IteratorUtils.map(idIter, ed -> new Neo4jEdge(ed.label, ed.id, ed.inId, ed.outId, this));

    }

    public Transaction tx() {
        return null;
    }

    public Variables variables() {
        return null;
    }

    public Configuration configuration() {
        return null;
    }

    @Override
    public Features features() {
        return features;
    }

    public void close() throws Exception {

    }

    public void clear(){
        String cypher= "match n optional match (n)-[r]-() delete n, r";
        String response = executeCypher(CypherStatementList.singleStatement(cypher)).readEntity(String.class);
    }

    protected Response executeCypher(CypherStatementList statementList){
        return target.path("/db/data/transaction/commit")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(statementList));
    }

    public class Neo4jGraphFeatures implements Features {
        protected GraphFeatures graphFeatures = new Neo4jGraphGraphFeatures();
        protected VertexFeatures vertexFeatures = new Neo4jVertexFeatures();
        protected EdgeFeatures edgeFeatures = new Neo4jEdgeFeatures();

        @Override
        public GraphFeatures graph() {
            return graphFeatures;
        }

        @Override
        public VertexFeatures vertex() {
            return vertexFeatures;
        }

        @Override
        public EdgeFeatures edge() {
            return edgeFeatures;
        }

        @Override
        public String toString() {
            return StringFactory.featureString(this);
        }
        public class Neo4jVariableFeatures implements VariableFeatures{
            @Override
            public boolean supportsVariables() {
                return false;
            }
        }

        public class Neo4jGraphGraphFeatures implements GraphFeatures {

            private VariableFeatures variableFeatures = new Neo4jVariableFeatures();

            Neo4jGraphGraphFeatures() {
            }

            @Override
            public boolean supportsComputer() {
                return false;
            }

            @Override
            public VariableFeatures variables() {
                return variableFeatures;
            }

            @Override
            public boolean supportsTransactions(){
                return false;
            }

            @Override
            public boolean supportsThreadedTransactions() {
                return false;
            }
        }

        public class Neo4jVertexFeatures extends Neo4jElementFeatures implements VertexFeatures {

            private final VertexPropertyFeatures vertexPropertyFeatures = new Neo4jVertexPropertyFeatures();

            protected Neo4jVertexFeatures() {
            }

            @Override
            public VertexPropertyFeatures properties() {
                return vertexPropertyFeatures;
            }

            @Override
            public boolean supportsMetaProperties() {
                return false;
            }

            @Override
            public boolean supportsMultiProperties() {
                return false;
            }

            @Override
            public boolean supportsUserSuppliedIds() {
                return false;
            }

            @Override
            public VertexProperty.Cardinality getCardinality(final String key) {
                return VertexProperty.Cardinality.single;
            }
        }

        public class Neo4jEdgeFeatures extends Neo4jElementFeatures implements EdgeFeatures {

            private final EdgePropertyFeatures edgePropertyFeatures = new Neo4jEdgePropertyFeatures();

            Neo4jEdgeFeatures() {
            }

            @Override
            public EdgePropertyFeatures properties() {
                return edgePropertyFeatures;
            }
        }

        public class Neo4jElementFeatures implements ElementFeatures {

            Neo4jElementFeatures() {
            }

            @Override
            public boolean supportsUserSuppliedIds() {
                return false;
            }

            @Override
            public boolean supportsStringIds() {
                return false;
            }

            @Override
            public boolean supportsUuidIds() {
                return false;
            }

            @Override
            public boolean supportsAnyIds() {
                return false;
            }

            @Override
            public boolean supportsCustomIds() {
                return false;
            }
        }

        public class Neo4jVertexPropertyFeatures implements VertexPropertyFeatures {

            Neo4jVertexPropertyFeatures() {
            }

            @Override
            public boolean supportsMapValues() {
                return false;
            }

            @Override
            public boolean supportsMixedListValues() {
                return false;
            }

            @Override
            public boolean supportsSerializableValues() {
                return false;
            }

            @Override
            public boolean supportsUniformListValues() {
                return false;
            }

            @Override
            public boolean supportsUserSuppliedIds() {
                return false;
            }

            @Override
            public boolean supportsAnyIds() {
                return false;
            }
        }

        public class Neo4jEdgePropertyFeatures implements EdgePropertyFeatures {

            Neo4jEdgePropertyFeatures() {
            }

            @Override
            public boolean supportsMapValues() {
                return false;
            }

            @Override
            public boolean supportsMixedListValues() {
                return false;
            }

            @Override
            public boolean supportsSerializableValues() {
                return false;
            }

            @Override
            public boolean supportsUniformListValues() {
                return false;
            }
        }
    }
}
