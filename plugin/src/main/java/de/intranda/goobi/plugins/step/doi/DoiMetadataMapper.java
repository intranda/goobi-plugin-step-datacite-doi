package de.intranda.goobi.plugins.step.doi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.sub.goobi.metadaten.MetadatenHelper;
import lombok.Setter;
import software.amazon.ion.NullValueException;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

/**
 * Class for reading the metadata necessary for a DOI out of an XML document (eg a MetsMods file).
 */
public class DoiMetadataMapper {

    // The mapping configuration to control which metadata from the MetsMods file 
	// should be used in which field of the DOI
    private Document mappingConfiguration;

    //dictionary of mappings
    private HashMap<String, ArrayList<Element>> doiMappings;

    //dictionary of list mappings    
    private HashMap<String, ArrayList<Element>> doiListMappings;

    //dictionary of pub data mappings    
    private HashMap<String, ArrayList<Element>> doiListPub;

    private ArrayList<String> lstMandatory;

    private Namespace sNS;
    @Setter
    private Prefs prefs;
    private DocStruct anchor;

    /**
     * Constructor which takes the mapping file as parameter
     * 
     * @throws IOException
     * @throws JDOMException
     */
    public DoiMetadataMapper(SubnodeConfiguration config) throws JDOMException, IOException {
    	// read configuration file
        String mapFile = config.getString("doiMapping");
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(mapFile);
        this.mappingConfiguration = (Document) builder.build(xmlFile);
        this.doiMappings = new HashMap<String, ArrayList<Element>>();
        Element rootNode = mappingConfiguration.getRootElement();

        // run through mapping configuration file to find out what shall be mapped how
        for (Element elt : rootNode.getChildren("map")) {
            ArrayList<Element> lstElts = new ArrayList<Element>();
            String key = elt.getChildText("field");
            if (doiMappings.containsKey(key)) {
                lstElts = doiMappings.get(key);
            }
            lstElts.add(elt);
            doiMappings.put(key, lstElts);
        }

        // run through mapping configuration file to find out which lists shall be mapped 
        this.doiListMappings = new HashMap<String, ArrayList<Element>>();
        for (Element elt : rootNode.getChildren("listMap")) {
            ArrayList<Element> lstElts = new ArrayList<Element>();
            String key = elt.getChildText("field");
            if (doiListMappings.containsKey(key)) {
                lstElts = doiListMappings.get(key);
            }
            lstElts.add(elt);
            doiListMappings.put(key, lstElts);
        }

        //set the namespace for xml
        this.sNS = Namespace.getNamespace("http://datacite.org/schema/kernel-4");

        // create a list of mandatory fields for DOI
        lstMandatory = new ArrayList<String>();
        lstMandatory.add("title");
        lstMandatory.add("creators");
        lstMandatory.add("publisher");
        lstMandatory.add("publicationYear");
        //lstMandatory.add("hostingInstitution");
        lstMandatory.add("resourceType");
    }

    /**
     * Given the root element of the xml file which we are examining, 
     * find the text of the entry correspoinding to the DOI field specified
     * 
     * @param field
     * @param root
     * @return
     */
//    public List<String> getValues(String field, Element root) {
//        ArrayList<String> lstDefault = new ArrayList<String>();
//        for (Element eltMap : doiMappings.get(field)) {
//
//            if (eltMap == null) {
//                return null;
//            }
//
//            //set up the default value:
//            String strDefault = eltMap.getChildText("default");
//
//            if (!strDefault.isEmpty()) {
//                lstDefault.add(strDefault);
//            }
//
//            //try to find the local value:
//            String metadata = eltMap.getChildText("metadata");
//
//            //no local value set? then return default:
//            if (metadata.isEmpty()) {
//                continue;
//            }
//
//            //otherwise
//            List<String> lstLocalValues = getValueRecursive(root, metadata);
//            if (!lstLocalValues.isEmpty()) {
//                lstDefault.addAll(lstLocalValues);
//                continue;
//            }
//
//            //could not find first choice? then try alternatives
//            for (Element eltAlt : eltMap.getChildren("altMetadata")) {
//                lstLocalValues = getValueRecursive(root, eltAlt.getText());
//                if (!lstLocalValues.isEmpty()) {
//                    lstDefault.addAll(lstLocalValues);
//                    continue;
//                }
//            }
//        }
//
//        //otherwise just return default
//        return lstDefault;
//    }

//    /**
//     * Find all child elements with the specified name, and return a list of all their values. 
//     * Note this will STOP at the first level at which it finds a hit: if there are "title" elements 
//     * at level 2 it will return all of them, and will NOT continue if look for "title" elts at lower
//     * levels.
//     * 
//     * @param root
//     * @param metadata
//     * @return
//     */
//    private List<String> getValueRecursive(Element root, String metadata) {
//        ArrayList<String> lstValues = new ArrayList<String>();
//        //if we find the correct named element, do NOT include its children in the search:
//        if (root.getName() == metadata) {
//            lstValues.add(root.getText());
//            return lstValues;
//        }
//        //recursive:
//        for (Element eltChild : root.getChildren()) {
//            lstValues.addAll(getValueRecursive(eltChild, metadata));
//        }
//        return lstValues;
//    }

    /**
     * Get the xml in strXmlFilePath, create a DOI file, and save it at strSave.
     * 
     * @param strXmlFilePath
     * @param strSave
     * @throws JDOMException
     * @throws IOException
     */
    public String getXMLStructure(String strNewHandle, BasicDoi basicDOI) throws IOException {
        Document doc = new Document();
        Element rootNew = new Element("resource", sNS);
        doc.setRootElement(rootNew);
        
        // add header information into XML
        Namespace xsiNS = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootNew.setAttribute("schemaLocation", "http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd", xsiNS);
        Element ident = new Element("identifier", sNS);
        ident.setAttribute("identifierType", "DOI");
        ident.addContent(strNewHandle);
        rootNew.addContent(ident);        
        
        //add basic DOI information into XML
        addDoi(rootNew, basicDOI);

        // convert xml into string
        XMLOutputter outter = new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat().setIndent("    "));
        String strOutput = outter.outputString(rootNew);

        //remove duplicate lines:
        String[] lines = strOutput.split("\\r?\\n");

        StringBuilder builder = new StringBuilder();
        String lastLine = "";
        for (String line : lines) {
            if (lastLine != "") {
                builder.append(System.getProperty("line.separator"));
            }
            if (!line.contentEquals(lastLine) ){
                builder.append(line);
            }
            lastLine = line;
        }
        return builder.toString();
    }

    /**
     * For each name-value pair in the basicDoi, add an elemement to the root.
     * 
     * @param rootNew
     * @param basicDOI
     */
    private void addDoi(Element rootNew, BasicDoi basicDOI) {
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
            }
        }

        if (basicDOI.getContentList() != null) {
            for (DoiListContent listContent : basicDOI.getContentList()) {
                Element eltList = new Element(listContent.getName(), sNS);
                for (Element elt : listContent.getEntries()) {
                    eltList.addContent(elt);
                }
                rootNew.addContent(eltList);
            }
        }

        if (basicDOI.getPubData() != null) {
            rootNew.addContent(basicDOI.getPubData());
        }
    }

    /**
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

    /**
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

    /**
     * Add the first and last name of a Person (if any) into the xml
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
     * @param anchor
     * @param anchor
     * @return
     * @throws MetadataTypeNotAllowedException
     */
    public BasicDoi getBasicDoi(DocStruct physical, DocStruct logical, DocStruct anchor, DigitalDocument document)
            throws MetadataTypeNotAllowedException {

        this.anchor = anchor;

        BasicDoi doi = new BasicDoi();
        doi.setTitles(getValues("title", physical, logical));
        
        //add publisher, which is just allowed once
        List<String> publishers = getValues("publisher", physical, logical);
        if (publishers.size()>1) {
            String singlePublisher = "";
            for (String pub : publishers) {
                singlePublisher += pub + "; ";
            }
            publishers = new ArrayList<String>();
            publishers.add(singlePublisher.substring(0, singlePublisher.length()-3));
        }
        
        doi.setPublishers(publishers);
        doi.setPublicationYears(getValues("publicationYear", physical, logical));
        doi.setResourceTypes(getValues("resourceType", physical, logical));

        //doi.setHostingInstitution(getValues("hostingInstitution", physical, logical).get(0));
        
        // set content
        doi.setContentList(getContentLists(physical, logical));
        
        // add values as Pairs
        for (String key : doiMappings.keySet()) {
            if (lstMandatory.contains(key)) {
                continue;
            }
            List<String> values = getValues(key, physical, logical);
            if (values == null || values.isEmpty()) {
                continue;
            }

            for (String value : values) {
                doi.addValuePair(key, value);
            }
        }
        
        try {
            addPublicationInfo(doi, physical, logical, document);
        } catch (NullValueException e) {
            //no publication info
        }

        return doi;
    }

    /**
     * get a list of all needed content information
     * 
     * @param doc
     * @param logical
     * @return
     * @throws MetadataTypeNotAllowedException
     */
    private List<DoiListContent> getContentLists(DocStruct doc, DocStruct logical) throws MetadataTypeNotAllowedException {
    	// collect a list of content information
    	List<DoiListContent> lstContent = new ArrayList<DoiListContent>();
        
    	//go through the metadata map
        
    	//first (compulsory) authors:
        DoiListContent authors = new DoiListContent("creators");
        for (Metadata mdAuthot : getMetadataValues("creators", doc)) {
            Element eltAuthor = makeAuthor(mdAuthot);
            authors.getEntries().add(eltAuthor);
        }
        lstContent.add(authors);

        //then others:
        for (String key : doiListMappings.keySet()) {

            if (lstMandatory.contains(key)) {
                continue;
            }

            for (Element elt : doiListMappings.get(key)) {
                if (key.contentEquals("contributors")) {
                    DoiListContent editors = new DoiListContent("contributors");
                    for (Metadata mdEditor : getMetadataValues("contributors", doc)) {
                        Element eltEditor = makeEditor(mdEditor);
                        editors.getEntries().add(eltEditor);
                    }
                    lstContent.add(editors);
                } else {
                    DoiListContent list = new DoiListContent(elt.getChildText("list"));
                    for (String strValue : getValues(key, doc, logical)) {
                        Element eltNew = new Element(key, sNS);
                        eltNew.addContent(strValue);
                        for (Attribute attribute : elt.getAttributes()) {
                            eltNew.setAttribute(attribute.getName(), attribute.getValue());
                        }

                        // first check for existing entries to avoid duplicates
                        boolean existsInList = false;
                        for (Element tempel : list.getEntries()) {
                        	String tempelValue = tempel.getText();
                        	if (tempelValue.equals(strValue)) {
                        		existsInList = true;
                        		break;
                        	}
						}
                        if (!existsInList) {
                        	list.getEntries().add(eltNew);
                        }
                    }
                    lstContent.add(list);
                    //have gone through all children for this key here already, do not repeat!
                    break;
                }
            }
        }

        if (lstContent.isEmpty()) {
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

        HashMap<String, ArrayList<Element>> mapping = doiMappings;
        if (!mapping.containsKey(field)) {
            mapping = doiListMappings;
        }

        ArrayList<Metadata> lstReturn = new ArrayList<Metadata>();

        
        if (mapping != null && field != null) {
            for (Element eltMap : mapping.get(field)) {
    
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
    
                ArrayList<Metadata> lstLocalValues = getMetadataFromMets(struct, metadata);
    
                if (!lstLocalValues.isEmpty()) {
                    lstReturn.addAll(lstLocalValues);
                    continue;
                }
    
                if (eltMap != null) {
                    //could not find first choice? then try alternatives
                    for (Element eltAlt : eltMap.getChildren("altMetadata")) {
                        lstLocalValues = getMetadataFromMets(struct, eltAlt.getText());
                        if (!lstLocalValues.isEmpty()) {
                            lstReturn.addAll(lstLocalValues);
                            continue;
                        }
                    }
                }
            }
        }
        //otherwise just return default
        if (lstReturn.isEmpty())
            lstReturn = lstDefault;

        return lstReturn;
    }

    /**
     * Get the values of metadata for the specified field, in the specified struct. If the value is not found there (eg in the chapter), then look for
     * it in the "logical" parent.
     * 
     * @param logical
     */
    private List<String> getValues(String field, DocStruct struct, DocStruct logical) {
        //use the vlaue or value list mapping
        return getValues(field, struct, logical, null);
    }

    /**
     * Get the values of metadata for the specified field, in the specified struct. If the value is not found there (eg in the chapter), then look for
     * it in the "logical" parent.
     * 
     * @param logical
     */
    private List<String> getValues(String field, DocStruct struct, DocStruct logical, HashMap<String, ArrayList<Element>> mapping) {

        if (mapping == null) {
            mapping = doiMappings;
            if (!mapping.containsKey(field)) {
                mapping = doiListMappings;
            }
        }

        ArrayList<String> lstReturn = new ArrayList<String>();
        ArrayList<String> lstDefault = new ArrayList<String>();
        String metadata = field;

        for (Element eltMap : mapping.get(field)) {

            if (eltMap != null) {
                //set up the default value:
                String strDefault = getDefault(eltMap);

                if (strDefault != null && !strDefault.isEmpty()) {
                    lstDefault.add(strDefault);
                }
                //try to find the local value:
                metadata = eltMap.getChildText("metadata");
            }

            List<String> lstLocalValues = getStringMetadataFromMets(struct, metadata);

            if (!lstLocalValues.isEmpty()) {
                lstReturn.addAll(lstLocalValues);
                continue;
            }

            if (eltMap != null) {
                //could not find first choice? then try alternatives
                for (Element eltAlt : eltMap.getChildren("altMetadata")) {
                    lstLocalValues = getStringMetadataFromMets(struct, eltAlt.getText());
                    if (!lstLocalValues.isEmpty()) {
                        lstReturn.addAll(lstLocalValues);
                        continue;
                    }
                }
            }

            //empty? then look in logical parent:
            lstLocalValues = getStringMetadataFromMets(logical, metadata);
            if (!lstLocalValues.isEmpty()) {
                lstReturn.addAll(lstLocalValues);
                continue;
            }

            if (eltMap != null) {
                //could not find first choice? then try alternatives
                for (Element eltAlt : eltMap.getChildren("altMetadata")) {
                    lstLocalValues = getStringMetadataFromMets(logical, eltAlt.getText());
                    if (!lstLocalValues.isEmpty()) {
                        lstReturn.addAll(lstLocalValues);
                        continue;
                    }
                }
            }
        }

        //otherwise just return default
        if (lstReturn.isEmpty())
            lstReturn = lstDefault;

        return lstReturn;
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
    private ArrayList<Metadata> getMetadataFromMets(DocStruct struct, String name) {
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
    private ArrayList<String> getStringMetadataFromMets(DocStruct struct, String name) {
        ArrayList<String> lstValues = new ArrayList<String>();

        if (name == null) {
            return lstValues;
        }

        //if the metadata is for the anchor, then chekc anchor first:
        if (this.anchor != null && name.startsWith("anchor_")) {
            struct = anchor;
            name = name.replace("anchor_", "");
        }

        if (fieldIsPerson(name)) {
            return getPersonFromMets(struct, name);
        }

        //no values?
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
     * Check whether the field is a person. 
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
    private ArrayList<String> getPersonFromMets(DocStruct struct, String name) {
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
    private ArrayList<Metadata> getMetadataPersonFromMets(DocStruct struct, String name) {
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

    /**
     * Example to look like : <relatedItems> <relatedItem relationType="IsPublishedIn" relatedItemType="Journal">
     * 
     * <relatedItemIdentifier relatedItemIdentifierType="ISSN">0370-2693</relatedItemIdentifier> <titles> <title>Physics letters B</title> </titles>
     * <publicationYear>2018</publicationYear> <volume>776</volume> <firstPage>249</firstPage> <lastPage>264</lastPage>
     * 
     * </relatedItem> </relatedItems>
     * 
     * 
     * 
     * @param doi
     * @param physical
     * @param logical
     * @param document
     */
    private void addPublicationInfo(BasicDoi doi, DocStruct physical, DocStruct logical, DigitalDocument document) {
        this.doiListPub = new HashMap<String, ArrayList<Element>>();
        Element rootNode = mappingConfiguration.getRootElement();

        for (Element elt : rootNode.getChildren("publicationData")) {
            ArrayList<Element> lstElts = new ArrayList<Element>();
            String key = elt.getChildText("field");
            if (doiListPub.containsKey(key)) {
                lstElts = doiListPub.get(key);
            }
            lstElts.add(elt);
            doiListPub.put(key, lstElts);
        }
        
        // create a related item only for specific publication types
        List<String> configDocTypes = new ArrayList<String>();
        for (Element elt : rootNode.getChildren("publicationTypeWithRelatedItem")) {
            configDocTypes.add(elt.getValue());
        }
        if (configDocTypes.contains(logical.getType().getName())){
            String relatedItemType = getPublicationType(logical);
            Element pubData = new Element("relatedItems", sNS);
            Element item = new Element("relatedItem", sNS);
            item.setAttribute("relationType", "IsPublishedIn");
            item.setAttribute("relatedItemType", relatedItemType);

            //ISSN
            Element eltId = new Element("relatedItemIdentifier", sNS);
            eltId.setAttribute("relatedItemIdentifierType", "ISSN");
            List<String> issn = getValues("ISSN", physical, logical, doiListPub);
            if (issn != null && !issn.isEmpty()) {
                eltId.setText(issn.get(0));
                item.addContent(eltId);
            }

            //Title
            Element eltTitles = new Element("titles", sNS);
            List<String> titles = getValues("title", physical, logical, doiListPub);
            Element eltTitle = null;
            if (titles != null && !titles.isEmpty()) {
                for (String title : titles) {
                    
                    eltTitle = new Element("title", sNS);
                    eltTitle.setText(title);
                    eltTitles.addContent(eltTitle);
                }
            }
            item.addContent(eltTitles);

            //publicationYear, but make sure it works
            String yearToUse = null;
            Pattern pattern = Pattern.compile("[\\d]{4}");
            List<String> years = getValues("publicationYear", physical, logical, doiListPub);
            for (String y : years) {
                Matcher matcher = pattern.matcher(y);
                if(matcher.matches()) {
                    yearToUse = y;
                    break;
                } 
            }
            // if it does not work try to cut it to 4 characters
            if (yearToUse == null) {
                for (String y : years) {
                    if (y.length()>4) {
                        String tempText = y.substring(0,4);
                        Matcher matcher = pattern.matcher(tempText);
                        if (matcher.matches()) {
                            yearToUse = tempText;
                            break;
                        };
                    }	    		
                }
            }
            // now use the year only if it is not null
            if (yearToUse != null) {
                Element eltYear = new Element("publicationYear", sNS);
                eltYear.setText(yearToUse);
                item.addContent(eltYear);
            }
            
            //volume
            Element eltVol = new Element("volume", sNS);
            List<String> vols = getValues("volume", physical, logical, doiListPub);
            if (vols != null && !vols.isEmpty()) {
                eltVol.setText(vols.get(0));
                item.addContent(eltVol);
            }
            
            //pages
            MetadatenHelper metahelper = new MetadatenHelper(prefs, document);
            MutablePair<String, String> first = metahelper.getImageNumber(physical, MetadatenHelper.PAGENUMBER_FIRST);
            if (first != null) {
                String firstPage = first.getRight();
                Element eltFirstPage = new Element("firstPage", sNS);
                eltFirstPage.setText(firstPage);
                item.addContent(eltFirstPage);
            }
            
            MutablePair<String, String> last = metahelper.getImageNumber(physical, MetadatenHelper.PAGENUMBER_LAST);
            if (first != null) {
                String lastPage = last.getRight();
                Element eltLastPage = new Element("lastPage", sNS);
                eltLastPage.setText(lastPage);
                item.addContent(eltLastPage);
            }

            pubData.addContent(item);
            doi.setPubData(pubData);
        }
    }

    /**
     * Return the type of the structure as string
     * 
     * @param logical
     * @return
     */
    private String getPublicationType(DocStruct logical) {
        try {
            switch (logical.getType().toString()) {
                case "MultiVolumeWork":
                    return "Book";
                
                case "Volume":
                    return "Book";
    
                default:
                    return "Journal";
            }
        } catch (Exception e) {
            return "Journal";
        }
    }

}