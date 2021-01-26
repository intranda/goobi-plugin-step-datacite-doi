package de.intranda.goobi.plugins.step.doi;

import java.io.IOException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jdom2.JDOMException;

import de.intranda.goobi.plugins.step.datacite.mds.doi.GetDOI;
import de.intranda.goobi.plugins.step.datacite.mds.doi.PostDOI;
import de.intranda.goobi.plugins.step.datacite.mds.metadata.PostMetadata;
import de.intranda.goobi.plugins.step.doi.BasicDoi;
import de.intranda.goobi.plugins.step.doi.MakeDOI;
import de.intranda.goobi.plugins.step.http.HTTPResponse;
import lombok.extern.log4j.Log4j;
import net.handle.hdllib.HandleException;
import ugh.dl.DocStruct;

/**
 * Creates requests for the Handle Service, querying handles and creating new handles.
 */
@Log4j
public class DoiHandler {

    // Non-Static fields
    private String base;
    private String prefix;
    private String separator;

    private String USERNAME;
    private String PASSWORD;

    private MakeDOI makeDOI;

    /**
     * Constructor.
     * 
     * @param config
     * @throws HandleException
     * @throws IOException
     * @throws JDOMException
     */
    public DoiHandler(SubnodeConfiguration config) throws IOException, JDOMException {
        this.base = config.getString("base");
        this.prefix = config.getString("url");
        this.separator = config.getString("separator");
        this.PASSWORD = config.getString("PASSWORD");
        this.USERNAME = config.getString("USERNAME");

        this.makeDOI = new MakeDOI(config);
    }

    /**
     * Given an object with specified ID and postfix, make a handle "base/postfix_id" with URL given in getURLForHandle. Returns the new Handle.
     * 
     * @throws IOException
     * @throws JDOMException
     * @throws DoiException
     * 
     */
    public String makeURLHandleForObject(String strObjectId, String strPostfix, DocStruct docstruct) throws JDOMException, IOException, DoiException {

        String strNewHandle = registerNewHandle(base + "/" + strPostfix + strObjectId, prefix, separator, docstruct);

        return strNewHandle;
    }

    /**
     * Make a new handle with specified URL. Returns the new handle.
     * 
     * @param strNewHandle
     * @param url
     * @param separator
     * @param boMintNewSuffix
     * @param boMakeDOI
     * @param basicDOI
     * @return
     * @throws DoiException
     * @throws IOException
     * @throws JDOMException
     * @throws HandleException
     */
    public String registerNewHandle(String strNewHandle, String url, String separator, DocStruct docstruct)
            throws DoiException, JDOMException, IOException {

        if (isDoiRegistered(strNewHandle)) {
            updateURLHandleForObject(strNewHandle, url, docstruct);
        }

        //create a unique suffix?
        int iCount = 0;
        String strTestHandle = strNewHandle;
        while (isDoiRegistered(strTestHandle)) {
            strTestHandle = strNewHandle + separator + iCount;
            iCount++;
            if (iCount > 10000) {
                throw new DoiException("Registry query always returning true: " + strNewHandle);
            }
        }

        //test handle ok:
        strNewHandle = strTestHandle;

        //first we must register the metadata:

        //create the xml from the original file
        BasicDoi basicDOI = makeDOI.getBasicDoi(docstruct);
        String strMetadata = makeDOI.getXMLStructure(strNewHandle, basicDOI);

        //then register it
        addDOI(strMetadata, strNewHandle);

        //Then register the url 
        registerURL(strNewHandle, url);

        return strNewHandle;
    }

    /**
     * Given an object with specified handle, update the URL (and if required DOI)
     * 
     */
    public void updateURLHandleForObject(String handle, String url, DocStruct docstruct) throws DoiException {

        String strMetadata = "";

        try {
            BasicDoi basicDOI = makeDOI.getBasicDoi(docstruct);
            strMetadata = makeDOI.getXMLStructure(handle, basicDOI);
        } catch (Exception e) {
            throw new DoiException(e);
        }

        //update the URL:
        PostDOI postDoi = new PostDOI(USERNAME, PASSWORD);
        postDoi.newURL(handle, url);

        //Update the metadata:
        PostMetadata postMeta = new PostMetadata(USERNAME, PASSWORD);
        postMeta.forDoi(handle, strMetadata);
    }

    
    /**
     * Returns true if the handle has already been registered, false otherwise.
     * 
     */
    public boolean isDoiRegistered(String handle) throws DoiException {

        GetDOI getDoi = new GetDOI(USERNAME, PASSWORD);

        return getDoi.ifExists(handle) != null;
    }

    /**
     * Create a DOI (with basic information) for the docstruct, and update the corresponding handle with the DOI info.
     */
    public Boolean addDOI(String metadata, String handle) throws JDOMException, IOException, DoiException {

        if (StringUtils.isEmpty(handle)) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        log.debug("Update Handle: " + handle + ". Generating DOI.");
        try {

            PostMetadata postData = new PostMetadata(USERNAME, PASSWORD);
            HTTPResponse response = postData.forDoi(handle, metadata);

            if (response.getResponseCode() != HTTPResponse.CREATED) {
                return false;
            }
        } catch (DoiException e) {
            log.error("Tried to update handle " + handle + " but failed", e);
            throw e;
        }

        return true;
    }

    /**
     * Give the DOI with specified handle the url.
     * 
     * @param url
     */
    public Boolean registerURL(String handle, String url) throws DoiException {

        if (StringUtils.isEmpty(handle)) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        log.debug("register Handle: " + handle);

        PostDOI postDoi = new PostDOI(USERNAME, PASSWORD);
        HTTPResponse response = postDoi.newURL(handle, url);

        if (response.getResponseCode() != HTTPResponse.CREATED) {
            throw new DoiException("\"Tried to register handle \" + handle + \" but failed: \" + response.getResponseCode()");
        }

        return true;
    }

    //    private HandleValue[] getHandleValuesFromDOI(BasicDoi basicDOI) throws DoiException {
    //        ArrayList<HandleValue> values = new ArrayList<HandleValue>();
    //        for (Pair<String, List<String>> pair : basicDOI.getValues()) {
    //            int index = getIndex(pair.getLeft());
    //            for (String strValue : pair.getRight()) {
    //                values.add(new HandleValue(index, pair.getLeft(), strValue));
    //            }
    //        }
    //
    //        int timestamp = (int) (System.currentTimeMillis() / 1000);
    //        for (HandleValue handleValue : values) {
    //            handleValue.setTimestamp(timestamp);
    //        }
    //        return values.toArray(new HandleValue[0]);
    //    }

    //    /**
    //     * Get the index in the handle for the specified field type.
    //     * 
    //     * @param strType
    //     * @return
    //     * @throws HandleException
    //     */
    //    private int getIndex(String strType) throws DoiException {
    //        switch (strType) {
    //            case "TITLE":
    //                return TITLE_INDEX;
    //            case "AUTHORS":
    //                return AUTHORS_INDEX;
    //            case "PUBLISHER":
    //                return PUBLISHER_INDEX;
    //            case "PUBDATE":
    //                return PUBDATE_INDEX;
    //            case "INST":
    //                return INST_INDEX;
    //            default:
    //                throw new DoiException("Mapping file error");
    //        }
    //    }
//
//    /**
//     * Setter
//     * 
//     * @param strMappingFile
//     */
//    public void setDOIMappingFile(String strMappingFile) {
//        this.strDOIMappingFile = strMappingFile;
//    }

}
