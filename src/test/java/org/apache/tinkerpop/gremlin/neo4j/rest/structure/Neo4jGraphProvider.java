package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.AbstractGraphProvider;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by fitz on 7/4/15.
 */
public class Neo4jGraphProvider extends AbstractGraphProvider {


    @Override
    public Map<String, Object> getBaseConfiguration(String s, Class<?> aClass, String s1, LoadGraphWith.GraphData graphData) {
        return new HashMap();
    }

    @Override
    public void clear(Graph graph, Configuration configuration) throws Exception {
        if (null != graph){
            ((Neo4jGraph) graph).clear();
        } else{
            new Neo4jGraph().clear();
        }

    }

    @Override
    public Graph openTestGraph(Configuration config) {
        config.setProperty(Graph.GRAPH, Neo4jGraph.class.getName());
        return GraphFactory.open(config);
    }

    @Override
    public Set<Class> getImplementations() {
        return ImmutableSet.of(Neo4jGraph.class, Neo4jEdge.class, Neo4jElement.class, Neo4jProperty.class, Neo4jVertexProperty.class);
    }
}
