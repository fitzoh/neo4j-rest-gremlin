package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.*;

/**
 * Created by fitz on 7/4/15.
 */
public class Neo4jVertexProperty<V> implements VertexProperty<V> {

    protected final Neo4jVertex vertex;
    protected final String key;
    protected final V value;
    protected boolean removed = false;

    protected Neo4jVertexProperty(final Neo4jVertex vertex, final String key, final V value) {
        this.vertex = vertex;
        this.key = key;
        this.value = value;
    }



    @Override
    public Vertex element() {
        return this.vertex;
    }

    @Override
    public Object id() {
        // TODO: Neo4j needs a better ID system for VertexProperties
        return (long) (this.key.hashCode() + this.value.hashCode() + this.vertex.id().hashCode());
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public V value() throws NoSuchElementException {
        return this.value;
    }

    @Override
    public boolean isPresent() {
        return null != this.value;
    }

    @Override
    public <U> Iterator<Property<U>> properties(final String... propertyKeys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <U> Property<U> property(final String key, final U value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
        if (this.removed) return;
        String path = String.format("/db/data/node/%s/properties/%s", vertex.id, key);
        this.vertex.graph.target.path(path).request().delete().close();
        this.removed = true;
    }

    @Override
    public Set<String> keys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode((Element) this);
    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }
}
