/**
 *  Copyright 2021 Martynas Jusevičius <martynas@atomgraph.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.atomgraph.linkeddatahub.imports.stream.csv;

import com.atomgraph.core.client.GraphStoreClient;
import com.atomgraph.etl.csv.ModelTransformer;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;
import java.util.function.BiFunction;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 *
 * @author {@literal Martynas Jusevičius <martynas@atomgraph.com>}
 */
public class CSVGraphStoreRowProcessor implements RowProcessor // extends com.atomgraph.etl.csv.stream.CSVStreamRDFProcessor
{

    private final GraphStoreClient graphStoreClient;
    private final String base;
    private final BiFunction<Query, Model, Model> function = new ModelTransformer();
    private final Query query;
    private int subjectCount, tripleCount;

    /**
     * Constructs row processor.
     * 
     * @param graphStoreClient the GSP client
     * @param base base URI
     * @param query transformation query
     */
    public CSVGraphStoreRowProcessor(GraphStoreClient graphStoreClient, String base, Query query)
    {
        this.graphStoreClient = graphStoreClient;
        this.base = base;
        this.query = query;
    }

    @Override
    public void processStarted(ParsingContext context)
    {
        subjectCount = tripleCount = 0;
    }

    @Override
    public void rowProcessed(String[] row, ParsingContext context)
    {
        Model rowModel = transformRow(row, context);
        getGraphStoreClient().add(null, rowModel); // Graph name not specified, will be assigned by the server. Exceptions get swallowed by the client! TO-DO: wait for completion
    }
    
    /**
     * Transforms CSV row into an an RDF graph.
     * First a generic CSV/RDF graph is constructed. Then the transformation query is applied on it.
     * 
     * @param row CSV row
     * @param context parsing context
     * @return RDF result
     */
    public Model transformRow(String[] row, ParsingContext context)
    {
        Model rowModel = ModelFactory.createDefaultModel();
        Resource subject = rowModel.createResource();
        subjectCount++;
        
        int cellNo = 0;
        for (String cell : row)
        {
            if (cell != null && context.headers()[cellNo] != null)
            {
                String fragmentId = IRILib.encodeUriComponent(context.headers()[cellNo]);
                Property property = rowModel.createProperty(getBase(), "#" + fragmentId);
                subject.addProperty(property, cell);
                tripleCount++;
            }
            cellNo++;
        }

        return getFunction().apply(getQuery(), rowModel); // transform row
    }
    
    @Override
    public void processEnded(ParsingContext context)
    {
    }

    /**
     * Returns the Graph Store Protocol client.
     * 
     * @return client
     */
    public GraphStoreClient getGraphStoreClient()
    {
        return graphStoreClient;
    }
    
    /**
     * Returns base URI.
     * @return base URI string
     */
    public String getBase()
    {
        return base;
    }
    
    /**
     * Returns transformation function.
     * It transforms an RDF model to another model.
     * 
     * @return function
     */
    public BiFunction<Query, Model, Model> getFunction()
    {
        return function;
    }
    
    /**
     * Returns the transformation query.
     * 
     * @return SPARQL query
     */
    public Query getQuery()
    {
        return query;
    }
    
    /**
     * Returns the cumulative count of RDF subject resources.
     * 
     * @return subject count
     */
    public int getSubjectCount()
    {
        return subjectCount;
    }
    
    /**
     * Returns the cumulative count of RDF triples.
     * 
     * @return triple count
     */
    public int getTripleCount()
    {
        return tripleCount;
    }
    
}
