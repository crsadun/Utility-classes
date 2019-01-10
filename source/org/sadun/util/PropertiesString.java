/*
 * Created on Jun 7, 2004
 */
package org.sadun.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author Cristiano Sadun
 */
public class PropertiesString {

	private QuoteAwareStringTokenizer st;
	private Properties p = new Properties();
	
	public PropertiesString(String propertyList) {
		QuoteAwareStringTokenizer st=new QuoteAwareStringTokenizer(propertyList,";","\"\"");
		while(st.hasMoreTokens()) {
			String s=st.nextToken();
			int i=s.indexOf('=');
			if (i==-1) throw new IllegalArgumentException("Invalid property definition "+s);
			p.put(s.substring(0,i),s.substring(i+1));
		}
	}
	
	public Properties getProperties() { return p; }

	public static String [] getArgs(Properties p) {
		return getArgs("-", p);
	}
	
	public static String [] getArgs(String optionPrefix, Properties p) {
			List l=new ArrayList();
			for(Iterator i=p.keySet().iterator();i.hasNext();) {
				String optionName=(String)i.next();
				String optionValues=p.getProperty(optionName);
				l.add(optionPrefix+optionName);
				QuoteAwareStringTokenizer st = new QuoteAwareStringTokenizer(optionValues);
				while(st.hasMoreTokens()) {
					l.add(st.nextToken());
				}
			}
			String [] result = new String[l.size()];
			l.toArray(result);
			return result;
	}
	
}
