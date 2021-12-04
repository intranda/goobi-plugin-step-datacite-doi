package de.intranda.goobi.plugins;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.jdom2.JDOMException;
import org.junit.Test;

import de.intranda.goobi.plugins.step.doi.BasicDoi;
import de.intranda.goobi.plugins.step.doi.DataciteDoiStepPlugin;
import de.intranda.goobi.plugins.step.doi.DoiMetadataMapper;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;

public class DataciteDoiPluginTest {

    @Test
    public void testVersion() throws IOException, ConfigurationException, PreferencesException, ReadException, JDOMException, UGHException {
        String s = "xyz";
        assertNotNull(s);

        //        main(null);
    }

    //Testing:
    //    @Test
    public void testXml() throws IOException, JDOMException, ConfigurationException, PreferencesException, ReadException, UGHException {

        //        String strConfig = "/opt/digiverso/goobi/test/plugin_intranda_step_datacite_doi.xml";
        //        String strMeta = "/opt/digiverso/goobi/test/meta.xml";
        //        String strRS = "/opt/digiverso/goobi/test/ruleset1.xml";

        String strConfig = "/opt/digiverso/goobi/test/plugin_intranda_step_datacite_doi-test.xml";
        String strMeta = "/opt/digiverso/goobi/test/stutt/meta.xml";
        String strRS = "/opt/digiverso/goobi/test/stutt/ruleset-stutt.xml";

        //        String strConfig = "/home/joel/git/Stuttgart/test/plugin_intranda_step_datacite_doi.xml";
        //        String strMeta = "/home/joel/git/Stuttgart/test/meta.xml";
        //        String strRS = "/home/joel/git/Stuttgart/test/ruleset-stutt.xml";

        XMLConfiguration xmlConfig = new XMLConfiguration(strConfig); //ConfigPlugins.getPluginConfig("whatever");
        xmlConfig.setExpressionEngine(new XPathExpressionEngine());
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());

        SubnodeConfiguration myconfig = null;
        myconfig = xmlConfig.configurationAt("/config");
        DoiMetadataMapper maker = new DoiMetadataMapper(myconfig);

        String[] typesForDOI = myconfig.getStringArray("typeForDOI");

        Prefs prefs = new Prefs();
        prefs.loadPrefs(strRS);
        maker.setPrefs(prefs);

        MetsMods mm = new MetsMods(prefs);
        mm.read(strMeta);

        DigitalDocument digitalDocument = mm.getDigitalDocument();
        DocStruct logical = digitalDocument.getLogicalDocStruct();

        DocStruct anchor = null;
        if (logical.getType().isAnchor()) {
            anchor = logical;
            logical = logical.getAllChildren().get(0);
        }

        ArrayList<DocStruct> lstArticles = new ArrayList<>();
        DataciteDoiStepPlugin plugin = new DataciteDoiStepPlugin();
        plugin.setTypesForDOI(myconfig.getStringArray("typeForDOI"));
        plugin.getStructureElementsToRegister(lstArticles, logical);

        int i = 0;

        for (DocStruct article : lstArticles) {

            //get the id of the article, or if it has none, the id of the parent:
            String strId = plugin.getDocStructId(article, logical);

            //add handle
            BasicDoi doi = maker.getBasicDoi(article, logical, anchor, digitalDocument);

            String str = maker.getXMLStructure("new", doi);

            System.out.print(str);
            System.out.println();
            System.out.println();
            //                strDOI = plugin.addDoi(article, strId, prefs, i, anchor);
            //                Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "DOI created: " + strDOI);
            //                i++;
            ////            }
            //            //otherwise just update the existing handle
            //            else {
            //                handler.updateData(article, strHandle, anchor);
            //                Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "DOI updated: " + strHandle);
            //            }
        }

        //        BasicDoi doi = maker.getBasicDoi(logical, anchor);
        //
        //        String str = maker.getXMLStructure("new", doi);
        //
        //        System.out.print(str);
    }

}