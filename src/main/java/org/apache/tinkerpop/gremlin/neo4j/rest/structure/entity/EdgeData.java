package org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity;

/**
 * Created by fitz on 7/13/15.
 */
public class EdgeData {
    public final String label;
    public final long id;
    public final long inId;
    public final long outId;

    public EdgeData(String label, long id, long inId, long outId){
        this.label = label;
        this.id = id;
        this.inId = inId;
        this.outId = outId;
    }
}
