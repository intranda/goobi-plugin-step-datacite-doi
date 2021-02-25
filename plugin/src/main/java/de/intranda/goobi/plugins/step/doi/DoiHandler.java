package de.intranda.goobi.plugins.step.doi;

import java.io.IOException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jdom2.JDOMException;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.datacite.mds.doi.GetDOI;
import de.intranda.goobi.plugins.step.datacite.mds.doi.PostDOI;
import de.intranda.goobi.plugins.step.datacite.mds.metadata.GetMetadata;
import de.intranda.goobi.plugins.step.datacite.mds.metadata.PostMetadata;
import de.intranda.goobi.plugins.step.doi.BasicDoi;
import de.intranda.goobi.plugins.step.doi.MakeDOI;
import de.intranda.goobi.plugins.step.http.HTTPResponse;
import lombok.extern.log4j.Log4j;
import net.handle.hdllib.HandleException;
import ugh.dl.DocStruct;
import ugh.dl.Prefs;

/**
 * Creates requests for the Handle Service, querying handles and creating new handles.
 */
@Log4j
public class DoiHandler {

    // Non-Static fields
    private String base;
    private String prefix;
    private String separator;

    private MakeDOI makeDOI;
    private AccessObject ao;

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

        this.ao = new AccessObject(config.getString("USERNAME"), config.getString("PASSWORD"));
        ao.SERVICE_ADDRESS = config.getString("SERVICE_ADDRESS", "https://mds.datacite.org/");

        this.makeDOI = new MakeDOI(config);
        makeDOI.setPrefs(prefs);
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

        String strNewHandle = registerNewHandle(base + "/" + strPostfix + strObjectId, docstruct);

        return strNewHandle;
    }

    /**
     * Make a new handle with specified URL. Returns the new handle.
     * 
     * @param strNewHandle
     * @param urlTarget
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
    public String registerNewHandle(String strNewHandle, DocStruct docstruct) throws DoiException, JDOMException, IOException {

        if (isDoiRegistered(strNewHandle)) {
            updateURLHandleForObject(strNewHandle, prefix + strNewHandle, docstruct);
            return strNewHandle;
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

        //register the metadata
        addDOI(strMetadata, strNewHandle);

        //Then register the url 
        registerURL(strNewHandle, prefix + strNewHandle);

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

        //Update the metadata:
        PostMetadata postMeta = new PostMetadata(ao);
        postMeta.forUpdatingDoi(handle, strMetadata);

        //update the URL:
        PostDOI postDoi = new PostDOI(ao);
        postDoi.newURL(handle, url);
    }

    /**
     * Returns true if the handle has already been registered, false otherwise.
     * 
     */
    public boolean isDoiRegistered(String handle) throws DoiException {

        GetDOI getDoi = new GetDOI(ao);

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

            PostMetadata postData = new PostMetadata(ao);
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
     * Create a DOI (with basic information) for the docstruct, and update the corresponding handle with the DOI info.
     */
    public Boolean updateData(DocStruct docstruct, String handle) throws JDOMException, IOException, DoiException {

        if (StringUtils.isEmpty(handle)) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        log.debug("Update Handle: " + handle + ". Generating DOI.");

        String strMetadata = "";
        try {

            try {
                BasicDoi basicDOI = makeDOI.getBasicDoi(docstruct);
                strMetadata = makeDOI.getXMLStructure(handle, basicDOI);
            } catch (Exception e) {
                throw new DoiException(e);
            }

            PostMetadata postData = new PostMetadata(ao);
            HTTPResponse response = postData.forUpdatingDoi(handle, strMetadata);

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

        PostDOI postDoi = new PostDOI(ao);
        HTTPResponse response = postDoi.newURL(handle, url);

        if (response.getResponseCode() != HTTPResponse.CREATED) {
            throw new DoiException("\"Tried to register handle \" + handle + \" but failed: \" + response.getResponseCode()");
        }

        return true;
    }

    /**
     * Static entry point for testing
     * 
     * @param args
     * @throws DoiException
     * @throws IOException
     * @throws ParseException
     * @throws ConfigurationException
     * @throws JDOMException
     */
    public static void main(String[] args) throws DoiException {
        System.out.println("Start DOI");

        AccessObject ao = new AccessObject("YZVP.GOOBI", "eo9iaH5u");
        ao.SERVICE_ADDRESS = "https://mds.test.datacite.org/";

        String strHandle = "10.80831/go-goobi-37876-1";
        String metadata =
                "<resource xmlns=\"http://datacite.org/schema/kernel-4\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd\">\r\n"
                        + "    <identifier identifierType=\"DOI\">10.80831/go-goobi-37876</identifier>\r\n" + "    <creators>\r\n"
                        + "        <creator>\r\n" + "            <creatorName>intranda</creatorName>\r\n" + "        </creator>\r\n"
                        + "    </creators>\r\n" + "    <title>¬Das preußische Rentengut</TITLE>\r\n" + "    <author>Aal, Arthur</AUTHORS>\r\n"
                        + "    <publisher>intranda</PUBLISHER>\r\n" + "    <publicationYear>1901</publicationYear>\r\n" + "</resource>";

        PostMetadata postMD = new PostMetadata(ao);
        HTTPResponse response = postMD.forDoi(strHandle, metadata);
        System.out.println(response.toString());

        String strNewURL = "https://viewer.goobi.io/idresolver?handle=" + strHandle;

        PostDOI postDoi = new PostDOI(ao);
        HTTPResponse response2 = postDoi.newURL(strHandle, strNewURL);
        System.out.println(response2.toString());

        GetMetadata getMD = new GetMetadata(ao);
        HTTPResponse response3 = getMD.forDoi(strHandle);
        System.out.println(response3.toString());
    }
}
