package org.apache.tinkerpop.gremlin.neo4j.rest.structure.entity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.tinkerpop.gremlin.neo4j.rest.structure.Neo4jRestException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by fitz on 7/10/15.
 */
public class VertexResponseIterator implements Iterator<Long> {
    private static JsonFactory factory = new JsonFactory();

    JsonParser parser;
    boolean hasNext;
    long next;

    public VertexResponseIterator(InputStream jsonResponse) throws Neo4jRestException {
        try{
            parser = factory.createParser(jsonResponse);
            hasNext = true;
            advanceToData();
            findNext();
        } catch (IOException e){
            throw new Neo4jRestException();
        }
    }

    private void advanceToData() throws IOException{
        parser.nextToken();//{
        parser.nextToken();//results :
        parser.nextToken();//[
        parser.nextToken();//{
        parser.nextToken();//columns
        parser.nextToken();//[
        parser.nextToken();//id(n)
        parser.nextToken();//]
        parser.nextToken();//data :
        parser.nextToken();//[
    }

    private void findNext(){
        try{
            if (parser.nextToken().equals(JsonToken.END_ARRAY)){
                hasNext = false;
                parser.close();
            } else { //{
                parser.nextToken();//row:
                parser.nextToken();//[
                parser.nextToken();//id
                next = parser.getLongValue();
                parser.nextToken();//]
                parser.nextToken();//}
            }
        } catch (IOException e){
            hasNext = false;
        }
    }

    @Override

    public boolean hasNext(){
        return hasNext;
    }

    @Override
    public Long next() {
        if (hasNext){
            long result = next;
            findNext();
            return result;
        } else{
            throw new NoSuchElementException();
        }
    }
}
