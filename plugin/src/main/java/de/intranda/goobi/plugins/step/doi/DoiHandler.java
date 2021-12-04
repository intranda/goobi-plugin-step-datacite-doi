package de.intranda.goobi.plugins.step.doi;

import java.io.IOException;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jdom2.JDOMException;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.datacite.mds.doi.GetDOI;
import de.intranda.goobi.plugins.step.datacite.mds.doi.PostDOI;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPResponse;
import de.intranda.goobi.plugins.step.datacite.mds.metadata.PostMetadata;
import lombok.extern.log4j.Log4j;
import net.handle.hdllib.HandleException;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Prefs;
import ugh.exceptions.UGHException;

/**
 * Creates requests for the Doi Service, querying dois and creating new dois.
 */
@Log4j
public class DoiHandler {

    private String base;
    private String prefix;
    private String separator;

    private MakeDOI makeDOI;
    private AccessObject ao;
    private String typeForDOI;

    /**
     * Constructor.
     * 
     * @param config
     * @param prefs
     * @throws HandleException
     * @throws IOException
     * @throws JDOMException
     */
    public DoiHandler(SubnodeConfiguration config, Prefs prefs) throws IOException, JDOMException {
        this.base = config.getString("base");
        this.prefix = config.getString("url");
        this.separator = config.getString("separator");
        this.typeForDOI = config.getString("typeForDOI", "");

        this.ao = new AccessObject(config.getString("username"), config.getString("password"));
        ao.setServiceAddress(config.getString("serviceAddress", "https://mds.datacite.org/"));

        this.makeDOI = new MakeDOI(config);
        makeDOI.setPrefs(prefs);
    }

    /**
     * Given an object with specified ID and postfix, make a doi "base/postfix_id" with URL given in getURLForHandle. Returns the new doi.
     * 
     * @param iIndex
     * @param anchor 
     * @param anchor2 
     * 
     * @throws IOException
     * @throws JDOMException
     * @throws DoiException
     * @throws UGHException
     * 
     */
    public String makeURLHandleForObject(String strObjectId, String strPostfix, DocStruct docstruct, int iIndex, DocStruct logical, DocStruct anchor, DigitalDocument document)
            throws JDOMException, IOException, DoiException, UGHException {
        String strNewHandle = registerNewHandle(base + "/" + strPostfix + strObjectId, docstruct, iIndex, logical,anchor,  document);
        return strNewHandle;
    }

    /**
     * Make a new doi with specified URL. Returns the new handle.
     * 
     * @param strNewHandle
     * @param docstruct
     * @param iIndex
     * @param anchor 
     * @return
     * @throws DoiException
     * @throws IOException
     * @throws JDOMException
     * @throws UGHException
     */
    public String registerNewHandle(String strNewHandle, DocStruct docstruct, int iIndex, DocStruct logical, DocStruct anchor, DigitalDocument document)
            throws DoiException, JDOMException, IOException, UGHException {

//        //if the index is incremented, then we are giving more than one article a doi.
//        //Only the first one may be updated
//        if (iIndex == 0 && isDoiRegistered(strNewHandle)) {
//            updateURLHandleForObject(strNewHandle, prefix + strNewHandle, docstruct);
//            return strNewHandle;
//        }

        //create a unique suffix
        int iCount = iIndex;
        String strTestHandle = strNewHandle + separator + iCount;

        //if the DOi is at Doc level, not Article, then do not include counter a priori:
        if (typeForDOI.isEmpty()) {
            strTestHandle = strNewHandle;
        }

        while (isDoiRegistered(strTestHandle)) {
            iCount++;
            strTestHandle = strNewHandle + separator + iCount;
            if (iCount > 10000) {
                throw new DoiException("Registry query always returning true: " + strNewHandle);
            }
        }

        //test doi ok:
        strNewHandle = strTestHandle;

        //first we must register the metadata:

        //create the xml from the original file
        BasicDoi basicDOI = makeDOI.getBasicDoi(docstruct,logical, anchor,  document);
        String strMetadata = makeDOI.getXMLStructure(strNewHandle, basicDOI);

        //register the metadata
        addDOI(strMetadata, strNewHandle);

        //Then register the url 
        registerURL(strNewHandle, prefix + strNewHandle);

        return strNewHandle;
    }

    /**
     * Returns true if the doi has already been registered, false otherwise.
     * 
     */
    public boolean isDoiRegistered(String doi) throws DoiException {
        GetDOI getDoi = new GetDOI(ao);
        return getDoi.ifExists(doi) != null;
    }

    /**
     * Create a DOI (with basic information) for the docstruct, and update the corresponding doi with the DOI info.
     */
    public Boolean addDOI(String metadata, String doi) throws JDOMException, IOException, DoiException {
        if (StringUtils.isEmpty(doi)) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        log.debug("Update DOI: " + doi + ". Generating DOI.");
        try {

            PostMetadata postData = new PostMetadata(ao);
            HTTPResponse response = postData.forDoi(doi, metadata);

            if (response.getResponseCode() != HTTPResponse.CREATED) {
                return false;
            }
        } catch (DoiException e) {
            log.error("Tried to update DOI " + doi + " but failed", e);
            throw e;
        }

        return true;
    }

    /**
     * Update an existing DOI (with basic information) for the docstruct
     * @param anchor 
     */
    public void updateData(DocStruct docstruct, String handle, DocStruct logical, DocStruct anchor, DigitalDocument document) throws JDOMException, IOException, DoiException {
        if (StringUtils.isEmpty(handle)) {
            throw new IllegalArgumentException("The DOI handle cannot be empty");
        }
        log.debug("Try to update DOI: " + handle);

        try {
        	String metadataXml = "";
            try {
                BasicDoi basicDOI = makeDOI.getBasicDoi(docstruct, logical, anchor,  document);
                metadataXml = makeDOI.getXMLStructure(handle, basicDOI);
            } catch (Exception e) {
                throw new DoiException(e);
            }

            PostMetadata postData = new PostMetadata(ao);
            HTTPResponse response = postData.forUpdatingDoi(handle, metadataXml);
            if (response.getResponseCode() != HTTPResponse.CREATED) {
                throw new DoiException("Tried to update DOI " + handle + " but failed: " + response.toString());
            }

            //now update the url:
            registerURL(handle, prefix + handle);
        } catch (DoiException e) {
            log.error("Tried to update DOI " + handle + " but failed", e);
            throw e;
        }
    }

    /**
     * Give the DOI with specified doi the url.
     * 
     * @param url
     */
    public void registerURL(String handle, String url) throws DoiException {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        log.debug("Try to register DOI: " + handle + " for URL: " + url);
        PostDOI postDoi = new PostDOI(ao);
        HTTPResponse response = postDoi.newURL(handle, url);
        if (response.getResponseCode() != HTTPResponse.CREATED) {
            throw new DoiException("Tried to register DOI: " + handle + " for URL: " + url + ". Registration failed: " + response.toString());
        }
    }

}
