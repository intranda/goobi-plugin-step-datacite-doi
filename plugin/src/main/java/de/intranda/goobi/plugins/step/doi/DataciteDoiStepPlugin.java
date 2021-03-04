package de.intranda.goobi.plugins.step.doi;

import java.io.IOException;

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
     * Carry out the plugin: - get the current digital document - for each physical and logical element of the document, create and register a handle
     * - write the handles into the MetsMods file for the document
     * 
     */
    @Override
    public PluginReturnValue run() {
        boolean successfull = true;
        String strDOI = "";
        Boolean boUpdate = false;

        try {
            //read the metatdata
            Process process = step.getProzess();
            Prefs prefs = process.getRegelsatz().getPreferences();
            String strUrn = config.getString("handleMetadata", "_urn");
            urn = prefs.getMetadataTypeByName(strUrn);

            Fileformat fileformat = process.readMetadataFile();
            DigitalDocument digitalDocument = fileformat.getDigitalDocument();
            DocStruct logical = digitalDocument.getLogicalDocStruct();
            if (logical.getType().isAnchor()) {
                logical = logical.getAllChildren().get(0);
            }

            String strId = getId(logical);

            //add handle
            boUpdate = getHandle(logical) != null;

            strDOI = addDoi(logical, strId, prefs);

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

        if (boUpdate) {
            Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "DOI updated: " + strDOI);
        } else {
            Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "DOI created: " + strDOI);
        }
        return PluginReturnValue.FINISH;
    }

    /**
     * If the element already has a handle, return it, otherwise return null.
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
     * Add handle (eg "_urn") metadata to the docstruct.
     */
    private void setHandle(DocStruct docstruct, String strHandle) throws MetadataTypeNotAllowedException {
        Metadata md = new Metadata(urn);
        md.setValue(strHandle);
        docstruct.addMetadata(md);
    }

    /**
     * check if Metadata handle exists if not, create handle and save it under "_urn" in the docstruct.
     * 
     * @param prefs
     * 
     * @return Returns the handle.
     * @throws DoiException
     * @throws JDOMException
     * @throws UGHException
     */
    public String addDoi(DocStruct docstruct, String strId, Prefs prefs)
            throws HandleException, IOException, JDOMException, DoiException, UGHException {

        DoiHandler handler = new DoiHandler(config, prefs);

        //already has a handle?
        String strHandle = getHandle(docstruct);
        if (strHandle == null) {
            //if not, make one.
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

            strHandle = handler.makeURLHandleForObject(strId, strPostfix, docstruct);
            setHandle(docstruct, strHandle);
        } else {

            //already has handle? Then delete the old data, and make new DOI:
            handler.updateData(docstruct, strHandle);
        }

        return strHandle;

    }

    /**
     * Return the CatalogIDDigital for this document
     */
    public String getId(DocStruct logical) {
        List<Metadata> lstMetadata = logical.getAllMetadata();
        if (lstMetadata != null) {
            for (Metadata metadata : lstMetadata) {
                if (metadata.getType().getName().equals("CatalogIDDigital")) {
                    return metadata.getValue();
                }
            }
        }
        //otherwise:
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