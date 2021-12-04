package de.intranda.goobi.plugins.step.doi;

/**
 * Exception for DOI
 */
public class DoiException extends Exception {

    private static final long serialVersionUID = 1933450438049700473L;

    public DoiException(String string) {
        super(string);
    }

    public DoiException(Exception e) {
        super(e);
    }

}
