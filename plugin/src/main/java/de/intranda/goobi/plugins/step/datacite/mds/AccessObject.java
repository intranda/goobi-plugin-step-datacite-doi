package de.intranda.goobi.plugins.step.datacite.mds;

/*
 * Class holding the data for access to the DataCite DOI registration
 */
public class AccessObject {

    //Address for the datacite requests
    public String SERVICE_ADDRESS = "https://mds.datacite.org/doi/";
    
    //User
    public String USERNAME = "[username]";
    
    //password
    public String PASSWORD = "[password]";
    
    /*
     * ctor
     */
    public AccessObject(String username, String pw) {
        this.USERNAME = username;
        this.PASSWORD = pw;
    }

}
