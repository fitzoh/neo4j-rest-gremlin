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
public class EdgeResponseIterator implements Iterator<EdgeData> {
    private static JsonFactory factory = new JsonFactory();

    JsonParser parser;
    boolean hasNext;
    EdgeData next;


    public EdgeResponseIterator(InputStream jsonResponse) throws Neo4jRestException {
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
        parser.nextToken();//type(r)
        parser.nextToken();//id(r)
        parser.nextToken();//id(n)
        parser.nextToken();//id(m)
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
                //type(r), id(r), id(n), id(m)
                String label = parser.getText();
                long id = parser.getLongValue();
                long inId = parser.getLongValue();
                long outId = parser.getLongValue();
                next = new EdgeData(label, id, inId, outId);
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
    public EdgeData next() {
        if (hasNext){
            EdgeData current = next;
            findNext();
            return current;
        } else{
            throw new NoSuchElementException();
        }
    }
}
