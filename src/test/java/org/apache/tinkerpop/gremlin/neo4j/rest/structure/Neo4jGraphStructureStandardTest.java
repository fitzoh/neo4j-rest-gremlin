package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.structure.StructureStandardSuite;
import org.junit.runner.RunWith;

/**
 * Created by fitz on 7/4/15.
 */
@RunWith(StructureStandardSuite.class)
@GraphProviderClass(provider = Neo4jGraphProvider.class, graph = Neo4jGraph.class)
public class Neo4jGraphStructureStandardTest {
}
