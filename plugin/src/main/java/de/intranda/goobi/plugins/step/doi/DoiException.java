package de.intranda.goobi.plugins.step.doi;

public class DoiException extends Exception {

    public DoiException(String string) {
        super(string);
    }

    public DoiException(Exception e) {
        super(e);
    }

}
