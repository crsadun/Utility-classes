package org.sadun.util.sql;

import java.sql.SQLException;

/**
 * 
 *
 * @author Cristiano Sadun
 */
public interface ResultSetPrinter {

    public void printCaptions() throws SQLException;
    public void printNextRow() throws SQLException;
    public void printAll() throws SQLException;
}
