/*
 * Created on Nov 25, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog.mbean.test;

import java.net.MalformedURLException;

import org.sadun.util.watchdog.WatchDog;
import org.sadun.util.watchdog.WatchDogException;
import org.sadun.util.watchdog.listeners.MailAlarmListener;
import org.sadun.util.watchdog.mbean.ManagedWatchDog;
import org.sadun.util.watchdog.mbean.ManagedWatchDogMBean;

/**
 *
 * @author Cristiano Sadun
 * @version 1.0
 */
public class TestWatchDog extends ManagedWatchDog {
	
	static class TestListener implements WatchDog.Listener {
		
		/* (non-Javadoc)
		 * @see org.sadun.util.watchdog.WatchDog.Listener#checkFailed(java.lang.Object, java.lang.Throwable)
		 */
		public void checkFailed(Object obj, Throwable e) {
		}

		/* (non-Javadoc)
		 * @see org.sadun.util.watchdog.WatchDog.Listener#checkImpossible(java.lang.Object, org.sadun.util.watchdog.WatchDogException)
		 */
		public void checkImpossible(Object obj, WatchDogException e) {
			
		}

		/* (non-Javadoc)
		 * @see org.sadun.util.watchdog.WatchDog.Listener#checkOk(java.lang.Object)
		 */
		public void checkOk(Object obj) {
			System.out.println("Raising exception on purpose");
			throw new RuntimeException();
		}

}
	
	public TestWatchDog() {
		super("test watchdog", 100, true, new ManagedWatchDogMBean.CheckAction() {
			public String isReady(boolean running) {
				return null;
			}
			
			public Throwable doCheck(Object obj) throws WatchDogException {
				System.out.println("Checking");
				return new WatchDogException("Failure to communicate");
			}
		});
		watchDog.setDaemon(false);
	}
	
	public static void main(String args[]) throws MalformedURLException {
		TestWatchDog wd = new TestWatchDog();
		//wd.addListener(new TestListener());
		wd.addListener(new MailAlarmListener("cristiano.sadun@tietoenator.com","localhost", "file:///c|/test.xml"));
		wd.setStartBySleeping(false);
		wd.startup();
		
	}



}
