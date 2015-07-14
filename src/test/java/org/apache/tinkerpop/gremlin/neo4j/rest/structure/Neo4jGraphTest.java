package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity.VertexResponseIterator;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import sun.security.util.ByteArrayTagOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

/**
 * Created by fitz on 7/3/15.
 */
public class Neo4jGraphTest {

    @Test
    public void test(){
        Neo4jGraph g = new Neo4jGraph();
        g.clear();
        Vertex v1 = g.addVertex(T.label, "thing");
        Vertex v2 = g.addVertex(T.label, "thing");
        Vertex v3 = g.addVertex(T.label, "thing");


    }
    
}