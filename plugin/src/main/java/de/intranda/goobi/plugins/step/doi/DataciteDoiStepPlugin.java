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
import ugh.exceptions.UGHException;

/**
 * @author steffen
 *
 */
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
    private MetadataType doiMetadataType;
    @Getter
    @Setter
    private SubnodeConfiguration config;
    private DoiHandler handler;
    @Getter
    @Setter
    private String[] typesForDOI;

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
     * Carry out the plugin: 
     * 	- get the current digital document 
     * 	- for the defined logical elements of the document, create and register a doi
     * 	- write the dois into the MetsMods file
     */
    @Override
    public PluginReturnValue run() {
        boolean successfull = true;
        
        try {
            //read the configuration and metatdata from the METS file
            Process process = step.getProzess();
            Prefs prefs = process.getRegelsatz().getPreferences();
            String doiMetadataTypeName = config.getString("doiMetadata", "DOI");
            doiMetadataType = prefs.getMetadataTypeByName(doiMetadataTypeName);
            typesForDOI = config.getStringArray("typeForDOI");

            Fileformat fileformat = process.readMetadataFile();
            DigitalDocument digitalDocument = fileformat.getDigitalDocument();
            DocStruct logical = digitalDocument.getLogicalDocStruct();
            
            // in case it is an anchor file use the first child as logical top
            DocStruct anchor = null;
            if (logical.getType().isAnchor()) {
                anchor = logical;
                logical = logical.getAllChildren().get(0);
            }

            // create a list of all the structure elements that shall receive a DOI
            // if no structure elements are configured then the top structure element is used
            ArrayList<DocStruct> structElements = new ArrayList<>();
            getStructureElementsToRegister(structElements, logical);

            // if no structure element was found we cannot create DOIs at all
            if (structElements.isEmpty()) {
                Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "Error while trying to register DOIs. No structure elements could be found to register.");
                successfull = false;
            }
            
            // do some preparation
            int i = 0;
            this.handler = new DoiHandler(config, prefs);

            // now run through the list of all docstructs to register
            for (DocStruct struct : structElements) {
                // Get the id of the structure element, or if it has none, the id of the parent:
                String strId = getDocStructId(struct, logical);

                // Register the DOI, by first checking if there is one already
                String existingDoi = getExistingDoi(struct);
                if (existingDoi == null) {
                	// if there is no DOI yet create it new
                	String strDOI = registerNewDoi(struct, strId, prefs, i, logical, anchor,  digitalDocument);
                    Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "A new DOI was created successfully: " + strDOI);
                    i++;
                } else {
                	//otherwise just update the existing DOI
                    handler.updateData(struct, existingDoi, logical, anchor,  digitalDocument);
                    Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, "The DOI was updated successfully:: " + existingDoi);
                }
            }

            //finally save the METS file again
            process.writeMetadataFile(fileformat);

        } catch (Exception e) {
            log.error("An error happend during the registration of DOIs", e);
            Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.ERROR, "An error happend during the registration of DOIs: " + e.getMessage());
            successfull = false;
        }

        log.info("DataCite Doi step plugin executed");
        if (!successfull) {
            return PluginReturnValue.ERROR;
        }

        return PluginReturnValue.FINISH;
    }

    /**
     * run through all children of the logical structure to collect the structure elements that shall get registered
     * 
     * @param structElements
     * @param logical
     */
    public void getStructureElementsToRegister(ArrayList<DocStruct> structList, DocStruct struct) {

        // check configured types to check if it is empty
        Boolean typesConfigured = false;
        if (typesForDOI != null && typesForDOI.length > 0) {
            for (String strType : typesForDOI) {
                if (strType != null && StringUtils.isNotBlank(strType)) {
                	// at least one type is configured and not empty
                	typesConfigured = true;
                    break;
                }
            }
        }

        // no special types are configured, so take just the top struct and return
        if (!typesConfigured) {
        	structList.add(struct);
            return;
        }

        // special types were configured, so collect them all
        for (String strType : typesForDOI) {
            log.debug("DOIs shall be registered for special type: " + strType);
            if (struct.getType().getName().contentEquals(strType)) {
            	structList.add(struct);
                break;
            }
        }

        // if current element has children then check these recursively too
        if (struct.getAllChildren() != null) {
            for (DocStruct child : struct.getAllChildren()) {
            	getStructureElementsToRegister(structList, child);
            }
        }
    }

    /**
     * If the element already has a DOI, return it, otherwise return null.
     * 
     * @param docstruct
     * @return
     */
    private String getExistingDoi(DocStruct docstruct) {
        List<? extends Metadata> lstURN = docstruct.getAllMetadataByType(doiMetadataType);
        if (lstURN.size() == 1) {
            return lstURN.get(0).getValue();
        }
        //otherwise
        return null;
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
    public String registerNewDoi(DocStruct docstruct, String strId, Prefs prefs, int iIndex, DocStruct logical, DocStruct anchor, DigitalDocument document)
            throws HandleException, IOException, JDOMException, DoiException, UGHException {

        // prepare the DOI name
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

        // register the DOI
        String doi = handler.makeURLHandleForObject(strId, strPostfix, docstruct, iIndex, logical, anchor,  document);
        
        // Write DOI metadata into the docstruct.
        Metadata md = new Metadata(doiMetadataType);
        md.setValue(doi);
        docstruct.addMetadata(md);
        
        // return the registered DOI
        return doi;
    }

    /**
     * Return the CatalogIDDigital for this document. If it has none, return the id for the parent
     * 
     * @param logical2
     */
    public String getDocStructId(DocStruct struct, DocStruct parent) {
        List<Metadata> lstMetadata = struct.getAllMetadata();
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
