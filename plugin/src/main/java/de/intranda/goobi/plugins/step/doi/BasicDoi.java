package de.intranda.goobi.plugins.step.doi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Most basic necessary info for a DataCite DOI: it must contain at least 
 * 
 * - title
 * - author
 * - publisher
 * - publication date
 * - resource type
 * 
 * Here Author must be included in the lstContent, which allows for metadata with more detail.
 */
public class BasicDoi {
    
    public List<String> lstFields;
    public List<String> TITLES;
    public List<String> PUBLISHER;
    public List<String> PUBLICATIONYEAR;
    public List<String> RESOURCETYPE;

    public List<DoiListContent> lstContent;
    
    /**
     * Constructor
     */
    public BasicDoi() {
        lstFields = new ArrayList<String>();
        lstFields.add("title");
        lstFields.add("publisher");
        lstFields.add("publicationYear");
        lstFields.add("resourceType");
        
        lstContent = new ArrayList<DoiListContent>();
    }

    /**
     * Returns a list of Field-Value pairs with the basic DOI information.
     * 
     * @return
     */
    public List<Pair<String, List<String>>> getValues() {
        List<Pair<String, List<String>>> lstValues = new ArrayList<Pair<String, List<String>>>();
        lstValues.add(Pair.of("title", TITLES));
        lstValues.add(Pair.of("publisher", PUBLISHER));
        lstValues.add(Pair.of("publicationYear", PUBLICATIONYEAR));
        lstValues.add(Pair.of("resourceType", RESOURCETYPE));
        return lstValues;
    }
}
