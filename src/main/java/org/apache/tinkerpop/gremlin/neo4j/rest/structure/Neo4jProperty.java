package org.apache.tinkerpop.gremlin.neo4j.rest.structure;

import com.google.common.base.Objects;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.NoSuchElementException;

/**
 * Created by fitz on 7/4/15.
 */
public class Neo4jProperty<V> implements Property<V> {


    protected final Element element;
    protected final String key;
    protected final Neo4jGraph graph;
    protected V value;
    protected boolean removed = false;

    protected Neo4jProperty(final Element element, final String key, final V value) {
        this.element = element;
        this.key = key;
        this.value = value;
        this.graph = ((Neo4jElement) element).graph;
    }

    @Override
    public String key() {
        return key;
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
    public Element element() {
        return element;
    }

    @Override
    public void remove() {

    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }
}
