package eu.sdi4apps.openapi.exceptions;

/**
 *
 * @author runarbe
 */
public class UnsupportedAction extends Exception {

    public UnsupportedAction(String actionParameter) {
        super("The specified action parameter is not supported: " + actionParameter);
    }

}
