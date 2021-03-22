package de.intranda.goobi.plugins.step.doi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

/**
 * Class for reading the metadata necessary for a DOI out of an XML document (eg a MetsMods file).
 * 
 */
public class MakeDOI {

    /**
     * The mapping document: this shows which metadata from the MetsMods file should be recorded in which field of the DOI
     */
    private Document mapping;

    //dictionary of mappings
    private HashMap<String, Element> doiMappings;

    private Namespace sNS;

    private Prefs prefs;

    /**
     * ctor: takes the mapping file as param.
     * 
     * @throws IOException
     * @throws JDOMException
     */
    public MakeDOI(SubnodeConfiguration config) throws JDOMException, IOException {

        String mapFile = config.getString("doiMapping");
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(mapFile);
        this.mapping = (Document) builder.build(xmlFile);
        this.doiMappings = new HashMap<String, Element>();
        Element rootNode = mapping.getRootElement();
        for (Element elt : rootNode.getChildren()) {
            doiMappings.put(elt.getChildText("field"), elt);
        }

        //set the namespace for xml
        this.sNS = Namespace.getNamespace("http://datacite.org/schema/kernel-4");

    }

    /**
     * Given the root elt of the xml file which we are examining, find the text of the entry correspoinding to the DOI field specified
     * 
     * @param field
     * @param root
     * @return
     */
    public List<String> getValues(String field, Element root) {
        Element eltMap = doiMappings.get(field);
        if (eltMap == null) {
            return null;
        }

        //set up the default value:
        String strDefault = eltMap.getChildText("default");
        ArrayList<String> lstDefault = new ArrayList<String>();
        if (!strDefault.isEmpty()) {
            lstDefault.add(strDefault);
        }

        //try to find the local value:
        String metadata = eltMap.getChildText("metadata");

        //no local value set? then return default:
        if (metadata.isEmpty()) {
            return lstDefault;
        }

        //otherwise
        List<String> lstLocalValues = getValueRecursive(root, metadata);
        if (!lstLocalValues.isEmpty()) {
            return lstLocalValues;
        }

        //could not find first choice? then try alternatives
        for (Element eltAlt : eltMap.getChildren("altMetadata")) {
            lstLocalValues = getValueRecursive(root, eltAlt.getText());
            if (!lstLocalValues.isEmpty()) {
                return lstLocalValues;
            }
        }

        //otherwise just return default
        return lstDefault;
    }

    /**
     * Find all child elements with the specified name, and return a list of all their values. Note this will STOP at the first level at which it
     * finds a hit: if there are "title" elements at level 2 it will return all of them, and will NOT continue if look for "title" elts at lower
     * levels.
     * 
     * @param root
     * @param metadata
     * @return
     */
    private List<String> getValueRecursive(Element root, String metadata) {
        ArrayList<String> lstValues = new ArrayList<String>();
        //if we find the correct named element, do NOT include its children in the search:
        if (root.getName() == metadata) {
            lstValues.add(root.getText());
            return lstValues;
        }
        //recursive:
        for (Element eltChild : root.getChildren()) {
            lstValues.addAll(getValueRecursive(eltChild, metadata));
        }
        return lstValues;
    }

    /**
     * Get the xml in strXmlFilePath, create a DOI file, and save it at strSave.
     * 
     * @param strXmlFilePath
     * @param strSave
     * @throws JDOMException
     * @throws IOException
     */
    public String getXMLStructure(String strNewHandle, BasicDoi basicDOI) throws IOException {

        Document docEAD = new Document();
        Element rootNew = new Element("resource", sNS);
        docEAD.setRootElement(rootNew);
        makeHeader(rootNew, strNewHandle);

        //addDOI
        addDoi(rootNew, basicDOI);

        //now save:
        XMLOutputter outter = new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat().setIndent("    "));
        String strOutput = outter.outputString(rootNew);

        return strOutput;
    }

    /**
     * set the resource attribute, and the identifier and creators nodes
     * 
     * @param root The new xml file to create
     * @param strDOI
     * @param eltOriginal the original xml file to get the content from.
     */
    private void makeHeader(Element root, String strDOI) {
        Namespace xsiNS = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("schemaLocation", "http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd", xsiNS);

        //DOI
        Element ident = new Element("identifier", sNS);
        ident.setAttribute("identifierType", "DOI");
        ident.addContent(strDOI);
        root.addContent(ident);

    }

    /**
     * For each name-value pair in the basicDoi, add an elemement to the root.
     * 
     * @param rootNew
     * @param basicDOI
     */
    private void addDoi(Element rootNew, BasicDoi basicDOI) {

//        this.sNS = rootNew.getNamespace();

        //Add the elts with children:
        Element titles = new Element("titles", sNS);
        rootNew.addContent(titles);

        for (Pair<String, List<String>> pair : basicDOI.getValues()) {

            String strName = pair.getLeft();
            List<String> lstValues = pair.getRight();

            for (String strValue : lstValues) {
                Element elt = new Element(strName, sNS);

                elt.addContent(strValue);

                if (strName.contentEquals("title")) {
                    titles.addContent(elt);

                } else if (strName.contentEquals("resourceType")) {
                    elt.setAttribute("resourceTypeGeneral", "Text");
                    rootNew.addContent(elt);

                } else {
                    rootNew.addContent(elt);
                }
                //                }
            }
        }

        if (basicDOI.lstContent != null) {
            for (DoiListContent listContent : basicDOI.lstContent) {

                Element eltList = new Element(listContent.strListName, sNS);

                for (Element elt : listContent.lstEntries) {
                    eltList.addContent(elt);
                }

                rootNew.addContent(eltList);
            }
        }
    }

    /*
     * Specific structure for an Editor
     */
    private Element makeEditor(Metadata mdPerson) {

        Person editor = (Person) mdPerson;
        Element eltEditor = new Element("contributor", sNS);
        eltEditor.setAttribute("contributorType", "Editor");

        Element eltName = new Element("contributorName", sNS);
        eltName.addContent(editor.getDisplayname());
        eltEditor.addContent(eltName);

        addName(editor, eltEditor);

        return eltEditor;
    }

    /*
     * Specific structure for an Author
     */
    private Element makeAuthor(Metadata mdPerson) {

        Person author = (Person) mdPerson;
        Element eltAuthor = new Element("creator", sNS);
        Element eltName = new Element("creatorName", sNS);
        String strName = author.getDisplayname();
        if (strName == null || strName.isEmpty()) {
            strName = mdPerson.getValue();
        }

        eltName.addContent(strName);
        eltAuthor.addContent(eltName);

        addName(author, eltAuthor);

        return eltAuthor;
    }

    /*
     * Add the first and last name of a Person, if any
     */
    private void addName(Person editor, Element eltEditor) {

        String strFirst = editor.getFirstname();
        String strLast = editor.getLastname();

        if (strFirst != null && !strFirst.isEmpty()) {
            Element eltGivenName = new Element("givenName", sNS);
            eltGivenName.addContent(strFirst);
            eltEditor.addContent(eltGivenName);
        }
        if (strLast != null && !strLast.isEmpty()) {
            Element eltFamilyName = new Element("familyName", sNS);
            eltFamilyName.addContent(strLast);
            eltEditor.addContent(eltFamilyName);
        }
    }

    /**
     * Given the root of an xml tree, get the basic DOI info.
     * 
     * @param physical
     * @return
     * @throws MetadataTypeNotAllowedException
     */
    public BasicDoi getBasicDoi(DocStruct physical) throws MetadataTypeNotAllowedException {
        BasicDoi doi = new BasicDoi();
        doi.TITLES = getValues("title", physical);
        //        doi.CREATORS = getValues("author", physical);
        doi.PUBLISHER = getValues("publisher", physical);
        doi.PUBLICATIONYEAR = getValues("publicationYear", physical);
        doi.RESOURCETYPE = getValues("resourceType", physical);

        doi.lstContent = getContentLists(physical);
        return doi;
    }

    private List<DoiListContent> getContentLists(DocStruct doc) throws MetadataTypeNotAllowedException {

        List<DoiListContent> lstContent = new ArrayList<DoiListContent>();

        //go through the maetadata map
        //first (compulsory) authors:
        DoiListContent authors = new DoiListContent("creators");
        for (Metadata mdAuthot : getMetadataValues("author", doc)) {

            Element eltAuthor = makeAuthor(mdAuthot);
            authors.lstEntries.add(eltAuthor);
        }

        lstContent.add(authors);

        //then (optional) editors:
        if (doiMappings.containsKey("editor")) {

            DoiListContent editors = new DoiListContent("contributors");
            for (Metadata mdEditor : getMetadataValues("editor", doc)) {

                Element eltEditor = makeEditor(mdEditor);
                editors.lstEntries.add(eltEditor);
            }

            lstContent.add(editors);
        }

        if (lstContent.isEmpty())

        {
            return null;
        }
        //otherwise
        return lstContent;
    }

    /**
     * Get the values of metadata for the specified field, in the specified struct.
     * 
     * @throws MetadataTypeNotAllowedException
     */
    private List<Metadata> getMetadataValues(String field, DocStruct struct) throws MetadataTypeNotAllowedException {
        ArrayList<Metadata> lstDefault = new ArrayList<Metadata>();
        String metadata = field;
        Element eltMap = doiMappings.get(field);
        if (eltMap != null) {
            String strDefault = getDefault(eltMap);
            if (strDefault != null && !strDefault.isEmpty()) {
                MetadataType type = prefs.getMetadataTypeByName(eltMap.getChildText("metadata"));
                Metadata mdDefault = new Metadata(type);
                if (type.getIsPerson()) {
                    mdDefault = new Person(type);
                }

                mdDefault.setValue(strDefault);
                lstDefault.add(mdDefault);
            }
            //try to find the local value:
            metadata = eltMap.getChildText("metadata");
        }

        List<Metadata> lstLocalValues = getMetadataFromMets(struct, metadata);

        if (!lstLocalValues.isEmpty()) {
            return lstLocalValues;
        }

        if (eltMap != null) {
            //could not find first choice? then try alternatives
            for (Element eltAlt : eltMap.getChildren("altMetadata")) {
                lstLocalValues = getMetadataFromMets(struct, eltAlt.getText());
                if (!lstLocalValues.isEmpty()) {
                    return lstLocalValues;
                }
            }
        }

        //otherwise just return default
        return lstDefault;
    }

    /**
     * Get the values of metadata for the specified field, in the specified struct.
     */
    private List<String> getValues(String field, DocStruct struct) {
        ArrayList<String> lstDefault = new ArrayList<String>();
        String metadata = field;
        Element eltMap = doiMappings.get(field);
        if (eltMap != null) {
            //set up the default value:
            String strDefault = getDefault(eltMap);            
            if (!strDefault.isEmpty()) {
                lstDefault.add(strDefault);
            }
            //try to find the local value:
            metadata = eltMap.getChildText("metadata");
        }

        List<String> lstLocalValues = getStringMetadataFromMets(struct, metadata);

        if (!lstLocalValues.isEmpty()) {
            return lstLocalValues;
        }

        if (eltMap != null) {
            //could not find first choice? then try alternatives
            for (Element eltAlt : eltMap.getChildren("altMetadata")) {
                lstLocalValues = getStringMetadataFromMets(struct, eltAlt.getText());
                if (!lstLocalValues.isEmpty()) {
                    return lstLocalValues;
                }
            }
        }

        //otherwise just return default
        return lstDefault;
    }

    private String getDefault(Element eltMap) {
        String strDefault = eltMap.getChildText("default");
        
        if (strDefault != null && strDefault.contentEquals("#CurrentYear")) {
            strDefault = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        }
        
        return strDefault;
    }

    /**
     * Get all metadata of type "name" in the specified struct.
     */
    private List<Metadata> getMetadataFromMets(DocStruct struct, String name) {

        if (fieldIsPerson(name)) {
            return getMetadataPersonFromMets(struct, name);
        }

        //no values?
        ArrayList<Metadata> lstValues = new ArrayList<Metadata>();
        if (struct.getAllMetadata() == null) {
            return lstValues;
        }

        for (Metadata mdata : struct.getAllMetadata()) {
            if (mdata.getType().getName().equalsIgnoreCase(name)) {
                lstValues.add(mdata);
            }
        }
        return lstValues;
    }

    /**
     * Get all metadata of type "name" in the specified struct.
     */
    private List<String> getStringMetadataFromMets(DocStruct struct, String name) {

        if (fieldIsPerson(name)) {
            return getPersonFromMets(struct, name);
        }

        //no values?
        ArrayList<String> lstValues = new ArrayList<String>();
        if (struct.getAllMetadata() == null) {
            return lstValues;
        }

        for (Metadata mdata : struct.getAllMetadata()) {
            if (mdata.getType().getName().equalsIgnoreCase(name)) {
                lstValues.add(mdata.getValue());
            }
        }
        return lstValues;
    }

    /**
     * Check whther the field is a person. Uses the current ruleset.
     * 
     * @param name
     * @return
     */
    private boolean fieldIsPerson(String name) {

        if (name == null) {
            return false;
        }

        MetadataType type = prefs.getMetadataTypeByName(name);
        return type != null && type.getIsPerson();
    }

    /**
     * Get all persons of type "name" in the specified struct.
     */
    private List<String> getPersonFromMets(DocStruct struct, String name) {

        ArrayList<String> lstValues = new ArrayList<String>();

        //no values?
        if (struct.getAllPersons() == null) {
            return lstValues;
        }

        for (Person mdata : struct.getAllPersons()) {
            if (mdata.getRole().equalsIgnoreCase(name)) {
                String strName = mdata.getDisplayname();
                if (strName == null || strName.isEmpty()) {
                    strName = mdata.getLastname();
                }
                if (strName == null || strName.isEmpty()) {
                    strName = mdata.getInstitution();
                }
                lstValues.add(strName);
            }
        }
        return lstValues;
    }

    /**
     * Get all persons of type "name" in the specified struct.
     */
    private List<Metadata> getMetadataPersonFromMets(DocStruct struct, String name) {

        ArrayList<Metadata> lstValues = new ArrayList<Metadata>();

        //no values?
        if (struct.getAllPersons() == null) {
            return lstValues;
        }

        for (Person mdata : struct.getAllPersons()) {
            if (mdata.getRole().equalsIgnoreCase(name)) {
                lstValues.add(mdata);
            }
        }
        return lstValues;
    }

    /*
     * Setter
     */
    public void setPrefs(Prefs prefs2) {
        this.prefs = prefs2;
    }

}