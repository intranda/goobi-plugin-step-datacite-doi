package de.intranda.goobi.plugins.step.datacite.mds;

import lombok.Data;

/*
 * Class holding the data for access to the DataCite DOI registration
 */
@Data
public class AccessObject {

    private String serviceAddress = "https://mds.datacite.org/";
    private String username = "[username]";
    private String password = "[password]";
    
    /*
     * ctor
     */
    public AccessObject(String username, String pw) {
        this.username = username;
        this.password = pw;
    }

}
