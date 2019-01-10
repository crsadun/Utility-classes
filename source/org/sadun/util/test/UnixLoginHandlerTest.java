package org.sadun.util.test;

import java.io.IOException;

import org.sadun.util.OperationTimedoutException;
import org.sadun.util.UnixLoginHandler;
import org.sadun.util.UnixLoginHandler.LoginIncorrectException;

import junit.framework.TestCase;

/**
 * 
 * @author Cristiano Sadun
 */
public class UnixLoginHandlerTest extends TestCase {
    
    UnixLoginHandler lh;
    String hostname="192.168.83.13";
    int port=23;
    String correctUser="webprov";
    String correctPassword="webprov";
    
    
    protected void setUp() throws Exception {
        lh=new UnixLoginHandler(hostname,port);
    }

    public void testCorrectLogin() throws IOException, OperationTimedoutException, LoginIncorrectException {
        lh.doLogin(correctUser,correctPassword);
        lh.doLogout();
    }
    
    public void testIncorrectLogin() throws IOException, OperationTimedoutException {
        try {
            lh.doLogin(correctUser,correctPassword+"*¤#*");
            lh.doLogout();
            fail("Logged in correctly with incorrect password!");
        } catch (LoginIncorrectException e) {
            // Ok
        }
    }
    
    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        lh.disconnect();
    }

}
