package org.sadun.util;

/**
 * Classes implementing this interface encapsulate boolean conditions.
 *
 * @author Cristiano Sadun
 */
public interface Condition {
    
    /**
     * A {@link Condition} which always holds.
     * 
     * @author Cristiano Sadun
     */
    public static final Condition ALWAYS_TRUE = new Condition() {
        public boolean holds(Object param) throws ConditionException {
            return true;
        }
    };
    
    
    /**
     * A {@link Condition} which never holds.
     * 
     * @author Cristiano Sadun
     */
    public static final Condition ALWAYS_FALSE = new Condition() {
        public boolean holds(Object param) throws ConditionException {
            return false;
        }
    };

    /**
     * Return whether or not the condition holds.
     * 
     * @return whether or not the condition holds.
     * @param param any parameter to the condition
     * @throws ConditionException
     */
    public boolean holds(Object param) throws ConditionException;

}
