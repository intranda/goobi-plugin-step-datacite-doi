package de.intranda.goobi.plugins.step.doi;

import java.util.ArrayList;

import org.jdom2.Element;

import lombok.Getter;

/**
 * Class representing a DOI list of entries, eg Contributors.
 */
public class DoiListContent {

	@Getter
    private String name;
    @Getter
    private ArrayList<Element> entries;

    public DoiListContent(String listName) {
    	name = listName;
        entries = new ArrayList<Element>();
    }

}
