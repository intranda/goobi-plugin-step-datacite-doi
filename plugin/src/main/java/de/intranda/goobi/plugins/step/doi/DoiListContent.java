package de.intranda.goobi.plugins.step.doi;

import java.util.ArrayList;

import org.jdom2.Element;

/**
 * Class representing a DOI list of entries, eg Contributors. 
 * 
 */
public class DoiListContent {

    public String strListName;
    
    public ArrayList<Element> lstEntries;
    
    public DoiListContent(String listName) {

        strListName = listName;
        lstEntries = new ArrayList<Element>();
    }

}
