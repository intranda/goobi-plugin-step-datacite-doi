package de.intranda.goobi.plugins.step.doi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
//import org.wiztools.xsdgen.ParseException;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;

/**
 * Class for reading the metadata necessary for a DOI out of an XML document (eg a MetsMods file).
 * 
 */
public class MakeDOI {

    /**
     * The mapping document: this shows which metadata from the MetsMods file should be recorded in which filed of the DOI
     * 
     */
    private Document mapping;

    //dictionary of mappings
    private HashMap<String, Element> doiMappings;

    private String strCreator;

    private Prefs prefs;

    //    /**
    //     * Static entry point for testing
    //     * 
    //     * @param args
    //     * @throws IOException
    //     * @throws ParseException
    //     * @throws ConfigurationException
    //     * @throws JDOMException
    //     */
    //    public static void main(String[] args) throws IOException, ConfigurationException, JDOMException {
    //        System.out.println("Start DOI");
    //        MakeDOI makeDoi = new MakeDOI(args[0]);
    //
    //        File xmlFile = new File("/home/joel/XML/orig.xml");
    //        SAXBuilder builder = new SAXBuilder();
    //        Document document = (Document) builder.build(xmlFile);
    //
    //        String strOut = makeDoi.getXMLStructure(document, "/home/joel/XML/doi_final.xml", "handle/number");
    //        System.out.println("Finished");
    //    }

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

        this.strCreator = config.getString("creator", "");
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
        Element rootNew = new Element("resource");
        docEAD.setRootElement(rootNew);
        makeHeader(rootNew, strNewHandle);

        //addDOI
        addDoi(rootNew, basicDOI);

        //now save:
        XMLOutputter outter = new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat().setIndent("    "));
        String strOutput = outter.outputString(rootNew);

        //hack: need to remove namespace def:
        strOutput = strOutput.replace(":xxxx", "");
        strOutput = strOutput.replace("xxxx:", "xmlns:");

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
        Namespace sNS = Namespace.getNamespace("xxxx", "http://datacite.org/schema/kernel-4");
        root.setAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", sNS);
        root.setAttribute("schemaLocation", "http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd", sNS);

        //DOI
        Element ident = new Element("identifier");
        ident.setAttribute("identifierType", "DOI");
        ident.addContent(strDOI);
        root.addContent(ident);

        //        //Creators
        //        Element creators = new Element("creators");
        //        Element creator = new Element("creator");
        //        Element creatorName = new Element("creatorName");
        //        creatorName.addContent(this.strCreator);
        //        creator.addContent(creatorName);

        //        List<String> lstCreatorNames = getValues("creatorName", root);
        //
        //        for (String strCreatorName : lstCreatorNames) {
        //            Element creatorName = new Element("creatorName");
        //            creatorName.addContent(strCreatorName);
        //            creator.addContent(creatorName);
        //        //        }
        //
        //        creators.addContent(creator);
        //        root.addContent(creators);
    }

    /**
     * For each name-value pair in the basicDoi, add an elemement to the root.
     * 
     * @param rootNew
     * @param basicDOI
     */
    private void addDoi(Element rootNew, BasicDoi basicDOI) {

        Namespace sNS = rootNew.getNamespace();

        //Add the elts with children:
        Element creators = new Element("creators", sNS);
        rootNew.addContent(creators);
        Element titles = new Element("titles", sNS);
        rootNew.addContent(titles);
        
        for (Pair<String, List<String>> pair : basicDOI.getValues()) {

            String strName = pair.getLeft();
            List<String> lstValues = pair.getRight();

            for (String strValue : lstValues) {
                Element elt = new Element(strName, sNS);
                elt.addContent(strValue);
                
                if (strName.contentEquals("creator")) {
                    creators.addContent(elt);
                    
                } else if (strName.contentEquals("title")) {
                    titles.addContent(elt);
                    
                } else if (strName.contentEquals("resourceType")) {
                    elt.setAttribute("resourceTypeGeneral", "Text");
                    rootNew.addContent(elt);
                    
                } else {
                    rootNew.addContent(elt);
                }
            }
        }
    }

    /**
     * Given the root of an xml tree, get the basic DOI info.
     * 
     * @param physical
     * @return
     */
    public BasicDoi getBasicDoi(DocStruct physical) {
        BasicDoi doi = new BasicDoi();
        doi.TITLES = getValues("title", physical);
        doi.CREATORS = getValues("author", physical);
        doi.PUBLISHER = getValues("publisher", physical);
        doi.PUBLICATIONYEAR = getValues("publicationYear", physical);
        doi.RESOURCETYPE = getValues("resourceType", physical);
        return doi;
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
            String strDefault = eltMap.getChildText("default");
            if (!strDefault.isEmpty()) {
                lstDefault.add(strDefault);
            }
            //try to find the local value:
            metadata = eltMap.getChildText("metadata");
        }

        List<String> lstLocalValues = getMetedataFromMets(struct, metadata);

        if (!lstLocalValues.isEmpty()) {
            return lstLocalValues;
        }

        if (eltMap != null) {
            //could not find first choice? then try alternatives
            for (Element eltAlt : eltMap.getChildren("altMetadata")) {
                lstLocalValues = getMetedataFromMets(struct, eltAlt.getText());
                if (!lstLocalValues.isEmpty()) {
                    return lstLocalValues;
                }
            }
        }

        //otherwise just return default
        return lstDefault;
    }

    /**
     * Get all metadata of type "name" in the specified struct.
     */
    private List<String> getMetedataFromMets(DocStruct struct, String name) {

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

    /*
     * Setter
     */
    public void setPrefs(Prefs prefs2) {
        this.prefs = prefs2;
    }

}