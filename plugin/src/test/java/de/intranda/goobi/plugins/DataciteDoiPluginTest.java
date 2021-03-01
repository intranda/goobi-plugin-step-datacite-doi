package de.intranda.goobi.plugins;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.beans.Ruleset;
import org.jdom2.JDOMException;
import org.junit.Test;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.datacite.mds.doi.PostDOI;
import de.intranda.goobi.plugins.step.datacite.mds.metadata.GetMetadata;
import de.intranda.goobi.plugins.step.datacite.mds.metadata.PostMetadata;
import de.intranda.goobi.plugins.step.doi.BasicDoi;
import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.doi.DoiListContent;
import de.intranda.goobi.plugins.step.doi.MakeDOI;
import de.intranda.goobi.plugins.step.http.HTTPResponse;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;

public class DataciteDoiPluginTest {

    @Test
    public void testVersion() throws IOException {
        String s = "xyz";
        assertNotNull(s);

    }

    //Testing:
    public static void main(String[] args)
            throws IOException, JDOMException, ConfigurationException, PreferencesException, ReadException, UGHException {

        String strConfig = "/home/joel/git/Stuttgart/plugin_intranda_step_datacite_doi.xml";
        String strMeta = "/opt/digiverso/goobi/metadata/25/meta.xml";
        String strRS = "/opt/digiverso/goobi/rulesets/ruleset_mpirg.xml";

        XMLConfiguration xmlConfig = new XMLConfiguration(strConfig); //ConfigPlugins.getPluginConfig("whatever");
        xmlConfig.setExpressionEngine(new XPathExpressionEngine());
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());

        SubnodeConfiguration myconfig = null;
        myconfig = xmlConfig.configurationAt("/config");
        MakeDOI maker = new MakeDOI(myconfig);

        Prefs prefs = new Prefs();
        prefs.loadPrefs(strRS);
        maker.setPrefs(prefs);
        
        MetsMods mm = new MetsMods(prefs);
        mm.read(strMeta);

        DigitalDocument digitalDocument = mm.getDigitalDocument();
        DocStruct logical = digitalDocument.getLogicalDocStruct();

        BasicDoi doi = maker.getBasicDoi(logical);

        String str = maker.getXMLStructure("new", doi);

        System.out.print(str);
    }

    //    private static BasicDoi getBasicDoi() {
    //        
    //        BasicDoi doi =   new BasicDoi();
    //        doi.TITLES = new ArrayList<String>();
    //        doi.PUBLISHER = new ArrayList<String>();
    //        doi.PUBLICATIONYEAR = new ArrayList<String>();
    //        doi.RESOURCETYPE = new ArrayList<String>();
    //
    //        doi.TITLES.add("hello");
    //        
    //        DoiListContent lstAutohrs =new DoiListContent("creators");
    //        lstAutohrs.lstEntries.add(new )
    //        doi.lstContent.add();
    //        doi.CREATORS.add("Smith, Fred");
    //        doi.CREATORS.add("Smith, Wilma");
    //               
    //        return doi;
    //    }
    
//
//    /**
//     * Static entry point from DoiHandler for testing
//     * 
//     * @param args
//     * @throws DoiException
//     * @throws IOException
//     * @throws ParseException
//     * @throws ConfigurationException
//     * @throws JDOMException
//     */
//    public static void main(String[] args) throws DoiException {
//        System.out.println("Start DOI");
//
//        AccessObject ao = new AccessObject("YZVP.GOOBI", "eo9iaH5u");
//        ao.SERVICE_ADDRESS = "https://mds.test.datacite.org/";
//
//        String strHandle = "10.80831/go-goobi-37876-1";
//        String metadata =
//                "<resource xmlns=\"http://datacite.org/schema/kernel-4\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd\">\r\n"
//                        + "    <identifier identifierType=\"DOI\">10.80831/go-goobi-37876</identifier>\r\n" + "    <creators>\r\n"
//                        + "        <creator>\r\n" + "            <creatorName>intranda</creatorName>\r\n" + "        </creator>\r\n"
//                        + "    </creators>\r\n" + "    <title>¬Das preußische Rentengut</TITLE>\r\n" + "    <author>Aal, Arthur</AUTHORS>\r\n"
//                        + "    <publisher>intranda</PUBLISHER>\r\n" + "    <publicationYear>1901</publicationYear>\r\n" + "</resource>";
//
//        PostMetadata postMD = new PostMetadata(ao);
//        HTTPResponse response = postMD.forDoi(strHandle, metadata);
//        System.out.println(response.toString());
//
//        String strNewURL = "https://viewer.goobi.io/idresolver?handle=" + strHandle;
//
//        PostDOI postDoi = new PostDOI(ao);
//        HTTPResponse response2 = postDoi.newURL(strHandle, strNewURL);
//        System.out.println(response2.toString());
//
//        GetMetadata getMD = new GetMetadata(ao);
//        HTTPResponse response3 = getMD.forDoi(strHandle);
//        System.out.println(response3.toString());
//    }
    
    
    //    /**
    //     * Static entry point for testing MakeDOI
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
}