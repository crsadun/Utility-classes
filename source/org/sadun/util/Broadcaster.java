package org.sadun.util;

/**
 * A general interface for mechanisms that can broadcast messages
 * to a given context (for example, across multiple threads, processes,
 * hosts, networks, etc).
 * 
 * @author Cristiano Sadun
 */
public interface Broadcaster {
    
    /**
     * 
     * @author Cristiano Sadun
     */
    public static class BroadcastingException extends Exception {

        /**
         * 
         */
        public BroadcastingException() {
            super();
            // TODO Auto-generated constructor stub
        }

        /**
         * @param message
         */
        public BroadcastingException(String message) {
            super(message);
            // TODO Auto-generated constructor stub
        }

        /**
         * @param cause
         */
        public BroadcastingException(Throwable cause) {
            super(cause);
            // TODO Auto-generated constructor stub
        }

        /**
         * @param message
         * @param cause
         */
        public BroadcastingException(String message, Throwable cause) {
            super(message, cause);
            // TODO Auto-generated constructor stub
        }

    }
    
    /**
     * Broadcast the given object.
     * 
     * @param obj the object to broadcast
     * @throws BroadcastingException if the broadcast fails
     */
    public void broadCast(Object obj) throws BroadcastingException;
    
    /**
     * Dispose of any resource used by this object
     * 
     */
    public void dispose() throws BroadcastingException;

}
