/*
 * Created on Dec 5, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog.mbean.action;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.sadun.util.watchdog.WatchDogException;
import org.sadun.util.watchdog.mbean.ManagedWatchDogMBean.CheckAction;

/**
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class ConnectURLAction implements CheckAction {

	public URL url;
	
	public String isReady(boolean running) {
		if (url==null) return "url' property not set";
		return null;
	}

	public Throwable doCheck(Object obj) throws WatchDogException {
		URLConnection uc;
		try {
			uc = url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(false);
			
			uc.connect();
			InputStream is = uc.getInputStream();
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		} catch (IOException e) {
			return e;
		} 
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		if (url==null) return null;
		return url.toString();
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("The given URL is not well formed: "+e.getMessage());
		}
	}

}
