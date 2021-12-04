package de.intranda.goobi.plugins.step.doi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import lombok.Getter;
import lombok.Setter;

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

	@Getter @Setter
	private List<String> fields;
    @Getter @Setter
    private List<String> titles;
    @Getter @Setter
    private List<String> publishers;
    @Getter @Setter
    private List<String> publicationYears;
    @Getter @Setter
    private List<String> resourceTypes;

    @Getter @Setter
    private List<DoiListContent> contentList;

    private List<Pair<String, List<String>>> valueList;
    @Getter @Setter
    private Element pubData;

    /**
     * Constructor
     */
    public BasicDoi() {
        fields = new ArrayList<String>();
        fields.add("title");
        fields.add("publisher");
        fields.add("publicationYear");
        fields.add("resourceType");
        contentList = new ArrayList<DoiListContent>();
    }

    /**
     * Returns a list of Field-Value pairs with the basic DOI information.
     * 
     * @return
     */
    public List<Pair<String, List<String>>> getValues() {
        if (valueList == null) {
        	valueList = new ArrayList<Pair<String, List<String>>>();
        	valueList.add(Pair.of("title", titles));
        	valueList.add(Pair.of("publisher", publishers));
        	valueList.add(Pair.of("publicationYear", publicationYears));
            valueList.add(Pair.of("resourceType", resourceTypes));
        }
        return valueList;
    }

    /**
     * Add a new value pair
     * 
     * @param strName
     * @param strValue
     */
    public void addValuePair(String strName, String strValue) {
        for (Pair<String, List<String>> pair : getValues()) {
            if (pair.getKey().contentEquals(strName)) {
                pair.getValue().add(strValue);
                return;
            }
        }

        //otherwise:
        List<String> lstNew = new ArrayList<String>();
        lstNew.add(strValue);
        valueList.add(Pair.of(strName, lstNew));
    }
    
}