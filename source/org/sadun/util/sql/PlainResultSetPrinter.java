package org.sadun.util.sql;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.sadun.util.PrintStringWriter;

/**
 * 
 *
 * @author Cristiano Sadun
 */
public class PlainResultSetPrinter implements ResultSetPrinter {

    private ResultSet rs;
    private String [] captions;
    private boolean [] disabled;
    private PrintStream out = System.out;
    
    public PlainResultSetPrinter(ResultSet rs) throws SQLException {
        this.rs=rs;
        ResultSetMetaData rsmd = rs.getMetaData();
        captions=new String[rsmd.getColumnCount()];
        disabled=new boolean[captions.length];
        for(int i=0;i<captions.length;i++) {
            captions[i]=rsmd.getColumnName(i+1);
        }
    }
    
    public void printAll() throws SQLException {
        printCaptions();
        while(rs.next()) {
            printNextRow();
        }
    }
    
    public void printCaptions() {
        PrintStringWriter pw = new PrintStringWriter();
        for(int i=0;i<captions.length;i++) {
            if (!disabled[i]) {
                if (pw.length()>0) pw.print(" ");
                pw.print(captions[i]);
            }
        }
        out.println(pw.toString());
        
        for(int i=0;i<captions.length;i++) {
            if (!disabled[i]) {
                if (pw.length()>0) pw.print(" ");
                
                pw.print(pad(captions[i].length()));
            }
        }
        
    }
    
    /**
     * @param i
     * @return
     */
    private String pad(int len) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++) sb.append("-");
        return sb.toString();
    }

    public void printNextRow() throws SQLException {
        PrintStringWriter pw = new PrintStringWriter();
        for(int i=0;i<captions.length;i++) {
            if (!disabled[i]) {
                if (pw.length()>0) pw.print(" ");
                pw.print(rs.getObject(i+1));
            }
        }
        out.println(pw.toString());
    }
}
