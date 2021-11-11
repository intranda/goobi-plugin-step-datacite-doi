package de.intranda.goobi.plugins.step.doi;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This file is part of a plugin for Goobi - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;
import org.jdom2.JDOMException;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.handle.hdllib.HandleException;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.UGHException;

@PluginImplementation
@Log4j2
public class DataciteDoiStepPlugin implements IStepPluginVersion2 {

    @Getter
    private String title = "intranda_step_datacite_doi";
    @Getter
    private Step step;
    @Getter
    private String value;
    @Getter
    private boolean allowTaskFinishButtons;
    private String returnPath;
    @Getter
    @Setter
    private MetadataType urn;
    @Getter
    @Setter
    private SubnodeConfiguration config;
    private DoiHandler handler;
    public String[] typesForDOI;

    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        this.step = step;

        // read parameters from correct block in configuration file
        config = ConfigPlugins.getProjectAndStepConfig(title, step);
        allowTaskFinishButtons = config.getBoolean("allowTaskFinishButtons", false);
        log.info("Datacite Doi step plugin initialized");
    }

    /**
     * Carry out the plugin: - get the current digital document - for each physical and logical element of the document, create and register a doi
     * - write the dois into the MetsMods file for the document
     * 
     */
    @Override
    public PluginReturnValue run() {
        boolean successfull = true;
        String strDOI = "";

        try {
            //read the metatdata
            Process process = step.getProzess();
            Prefs prefs = process.getRegelsatz().getPreferences();
            String strUrn = config.getString("doiMetadata", "DOI");
            typesForDOI = config.getStringArray("typeForDOI");
            urn = prefs.getMetadataTypeByName(strUrn);

            Fileformat fileformat = process.readMetadataFile();
            DigitalDocument digitalDocument = fileformat.getDigitalDocument();
            DocStruct logical = digitalDocument.getLogicalDocStruct();

            DocStruct anchor = null;
            if (logical.getType().isAnchor()) {
                anchor = logical;
                logical = logical.getAllChildren().get(0);
            }

            ArrayList<DocStruct> lstArticles = new ArrayList<>();
            getArticles(lstArticles, logical);

            if (lstArticles.isEmpty()) {
                Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "No DOIs created");
            }
            int i = 0;

            this.handler = new DoiHandler(config, prefs);

            for (DocStruct article : lstArticles) {

                //get the id of the article, or if it has none, the id of the parent:
                String strId = getId(article, logical);

                //add doi
                //already has a doi?
                String strHandle = getHandle(article);
                //If not, create a new doi
                if (strHandle == null) {
                    strDOI = addDoi(article, strId, prefs, i, logical, anchor,  digitalDocument);
                    Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "DOI created: " + strDOI);
                    i++;
                }
                //otherwise just update the existing doi
                else {
                    handler.updateData(article, strHandle, logical, anchor,  digitalDocument);
                    Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "DOI updated: " + strHandle);
                }
            }

            //and save the metadata again.
            process.writeMetadataFile(fileformat);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.ERROR, "Error writing DOI: " + e.getMessage());
            successfull = false;
        }

        log.info("DataCite Doi step plugin executed");

        if (!successfull) {
            return PluginReturnValue.ERROR;
        }

        return PluginReturnValue.FINISH;
    }

    //Collect all sub structs of the type typeForDOI
    public void getArticles(ArrayList<DocStruct> lstArticles, DocStruct logical) {

        //If no specific types, just add the top doctype:
        Boolean boTypes = false;
        if (typesForDOI != null && typesForDOI.length > 0) {
            for (String strType : typesForDOI) {
                if (strType != null && StringUtils.isNotBlank(strType)) {
                    boTypes = true;
                    break;
                }
            }
        }

        if (!boTypes) {
            lstArticles.add(logical);
            return;
        }

        //otherwise add all docstructs mentioned in typesForDOI
        for (String strType : typesForDOI) {

            log.debug("Adding type: " + strType);
            if (logical.getType().getName().contentEquals(strType)) {
                lstArticles.add(logical);
                break;
            }
        }

        //Check children:
        if (logical.getAllChildren() != null) {
            for (DocStruct child : logical.getAllChildren()) {
                getArticles(lstArticles, child);
            }
        }

    }

    /**
     * If the element already has a doi, return it, otherwise return null.
     */
    private String getHandle(DocStruct docstruct) {
        List<? extends Metadata> lstURN = docstruct.getAllMetadataByType(urn);
        if (lstURN.size() == 1) {
            return lstURN.get(0).getValue();
        }
        //otherwise
        return null;
    }

    /**
     * Add doi (eg "_urn" or "DOI") metadata to the docstruct.
     */
    private void setHandle(DocStruct docstruct, String strHandle) throws MetadataTypeNotAllowedException {
        Metadata md = new Metadata(urn);
        md.setValue(strHandle);
        docstruct.addMetadata(md);
    }

    /**
     * create doi and save it in the docstruct.
     * 
     * @param prefs
     * @param iIndex
     * @param anchor
     * @param anchor
     * 
     * @return Returns the doi.
     * @throws DoiException
     * @throws JDOMException
     * @throws UGHException
     */
    public String addDoi(DocStruct docstruct, String strId, Prefs prefs, int iIndex, DocStruct logical, DocStruct anchor, DigitalDocument document)
            throws HandleException, IOException, JDOMException, DoiException, UGHException {

        //Make a doi
        String name = config.getString("name");
        String prefix = config.getString("prefix");
        String separator = config.getString("separator", "-");
        String strPostfix = "";
        if (prefix != null && !prefix.isEmpty()) {
            strPostfix = prefix + separator;
        }
        if (name != null && !name.isEmpty()) {
            strPostfix += name + separator;
        }

        String strHandle = handler.makeURLHandleForObject(strId, strPostfix, docstruct, iIndex, logical, anchor,  document);
        setHandle(docstruct, strHandle);

        return strHandle;

    }

    /**
     * Return the CatalogIDDigital for this document. If it has none, return the id for the parent
     * 
     * @param logical2
     */
    public String getId(DocStruct logical, DocStruct parent) {
        List<Metadata> lstMetadata = logical.getAllMetadata();
        if (lstMetadata != null) {
            for (Metadata metadata : lstMetadata) {
                if (metadata.getType().getName().equals("CatalogIDDigital")) {
                    return metadata.getValue();
                }
            }
        }
        //otherwise:
        lstMetadata = parent.getAllMetadata();
        if (lstMetadata != null) {
            for (Metadata metadata : lstMetadata) {
                if (metadata.getType().getName().equals("CatalogIDDigital")) {
                    return metadata.getValue();
                }
            }
        }

        return null;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.NONE;
    }

    @Override
    public String getPagePath() {
        return null;
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String cancel() {
        return "/uii" + returnPath;
    }

    @Override
    public String finish() {
        return "/uii" + returnPath;
    }

    @Override
    public int getInterfaceVersion() {
        return 0;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public boolean execute() {
        PluginReturnValue ret = run();
        return ret != PluginReturnValue.ERROR;
    }

}
