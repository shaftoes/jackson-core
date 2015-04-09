package com.fasterxml.jackson.core.filter;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.*;

public class BasicFilteringTest extends com.fasterxml.jackson.core.BaseTest
{
    static class NameMatchFilter extends TokenFilter
    {
        private final Set<String> _names;
        
        public NameMatchFilter(String... names) {
            _names = new HashSet<String>(Arrays.asList(names));
        }
        
        @Override
        public TokenFilter includeProperty(String name) {
            if (_names.contains(name)) {
System.err.println("Filter:Include? "+name+" -> true");
                return TokenFilter.INCLUDE_ALL;
            }
System.err.println("Filter:Include? "+name+" -> false");
            return this;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    
    private final JsonFactory JSON_F = new JsonFactory();
    
    public void testNonFiltering() throws Exception
    {
        // First, verify non-filtering
        StringWriter w = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(w);
        _writeSimpleDoc(gen);
        gen.close();
        assertEquals(aposToQuotes(
                "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}"),
                w.toString());
    }

    public void testSingleMatchFilteringWithoutPath() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value"),
                false, // includePath
                false // multipleMatches
                );
        
        _writeSimpleDoc(gen);
        gen.close();
        // Since properties MUST be contained within an Object, inclusion needs
         // to materialize surrounding Object too!
        assertEquals(aposToQuotes("{'value':3}"), w.toString());
    }

    public void testSingleMatchFilteringWithPath() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value"),
                true, // includePath
                false // multipleMatches
                );
        
        _writeSimpleDoc(gen);
        gen.close();
        assertEquals(aposToQuotes("{'ob':{'value':3}}"), w.toString());
    }

    public void testMultipleMatchFilteringWithPath1() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value0", "value2"),
                true, /* includePath */ true /* multipleMatches */ );
        _writeSimpleDoc(gen);
        gen.close();
        assertEquals(aposToQuotes("{'ob':{'value0':2,'value2':4}}"), w.toString());
    }
    
    public void testMultipleMatchFilteringWithPath2() throws Exception
    {
        StringWriter w = new StringWriter();
        
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("array", "b", "value"),
                true, true);
        try {
        _writeSimpleDoc(gen);
        gen.close();
        } finally {
            gen.flush();
            System.out.println("JSON -> <"+w+">");
        }
        assertEquals(aposToQuotes("{'array':[1,2],'ob':{'value':3},'b':true}"), w.toString());
    }
    
    protected void _writeSimpleDoc(JsonGenerator gen) throws IOException
    {
        // { "a" : 123,
        //   "array" : [ 1, 2 ],
        //   "ob" : { "value" : 3 },
        //   "b" : true
        // }

        gen.writeStartObject();

        gen.writeFieldName("a");
        gen.writeNumber(123);
        
        gen.writeFieldName("array");
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeEndArray();

        gen.writeFieldName("ob");
        gen.writeStartObject();
        gen.writeFieldName("value0");
        gen.writeNumber(2);
        gen.writeFieldName("value");
        gen.writeNumber(3);
        gen.writeFieldName("value2");
        gen.writeNumber(4);
        gen.writeEndObject();

        gen.writeFieldName("b");
        gen.writeBoolean(true);

        gen.writeEndObject();
    }
}
