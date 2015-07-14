package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by fitz on 7/2/15.
 */
public abstract class Neo4jElement implements Element {

    protected final long id;
    protected final Neo4jGraph graph;
    public boolean removed = false;

    protected Neo4jElement(long id, Neo4jGraph graph){
        this.id = id;
        this.graph = graph;
    }

    @Override
    public Graph graph() {
        return this.graph;
    }

    @Override
    public Object id() {
        return id;
    }



    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

}
