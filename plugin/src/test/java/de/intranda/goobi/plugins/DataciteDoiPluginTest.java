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

        String strConfig = "/opt/digiverso/goobi/test/plugin_intranda_step_datacite_doi.xml";
        String strMeta = "/opt/digiverso/goobi/test/meta.xml";
        String strRS = "/opt/digiverso/goobi/rulesets/ruleset_mpirg.xml";

        XMLConfiguration xmlConfig = new XMLConfiguration(strConfig); //ConfigPlugins.getPluginConfig("whatever");
        xmlConfig.setExpressionEngine(new XPathExpressionEngine());
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());

        SubnodeConfiguration myconfig = null;
        myconfig = xmlConfig.configurationAt("/config");
        MakeDOI maker = new MakeDOI(myconfig);

        String[] typesForDOI = myconfig.getStringArray("typeForDOI");
        
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

}