package org.sadun.util;

/**
 * An exception raised by the {@link org.sadun.util.Condition} interface. 
 *
 * @author Cristiano Sadun
 */
public class ConditionException extends Exception {

    /**
     * 
     */
    public ConditionException() {
        super();
    }

    /**
     * @param message
     */
    public ConditionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ConditionException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ConditionException(String message, Throwable cause) {
        super(message, cause);
    }

}
