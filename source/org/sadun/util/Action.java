package org.sadun.util;

/**
 * A general interface for actions with parameters. 
 *
 * @author Cristiano Sadun
 */
public interface Action {
    
    public void execute(Object param) throws ActionException;

}
