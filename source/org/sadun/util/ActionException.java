package org.sadun.util;

/**
 * An exception raised by the {@link org.sadun.util.Action} interface.
 *
 * @author Cristiano Sadun
 */
public class ActionException extends Exception {

    /**
     * 
     */
    public ActionException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public ActionException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public ActionException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public ActionException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
