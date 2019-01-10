/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog;

import java.util.Random;

import com.deltax.util.TimeInterval;

/**
 *
 * @author Cristiano Sadun
 * @version 1.0
 */
public class Test {

	public static void main(String[] args) {
		
		final Random random = new Random();
		
		WatchDog wd = new WatchDog("test", 1000) {
			
			public Throwable doCheck(Object obj) throws WatchDogException {
				try {
					long sleepTime=(1+random.nextInt(100))*50;
					System.out.println("Sleeping for "+new TimeInterval(sleepTime));
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					throw new WatchDogException("Interrupted");
				}
				switch(random.nextInt(3)) {
					case 0: return null;
					case 1: return new Throwable("A simulated problem has occurred");
					default: return new WatchDogException("A simulated check impossibility has occurred");
				}
			}
		};
		
		WatchDog.Listener listener = new WatchDog.Listener() {
			/* (non-Javadoc)
			 * @see org.sadun.util.watchdog.ManagedWatchDog.Listener#checkFailed(java.lang.Object, java.lang.Throwable)
			 */
			public void checkFailed(Object obj, Throwable e) {
				System.out.println("Check failed (param="+obj+")");
				e.printStackTrace(System.out);
				
				// Block for some time
				if (random.nextBoolean()) {
					long blockTime=10000+random.nextInt(10)*500;
					System.out.println("LISTENER BLOCKED FOR "+new TimeInterval(blockTime)+"!");
					try {
						Thread.sleep(blockTime);
						System.out.println("LISTENER UNBLOCKED");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						System.out.println("LISTENER UNBLOCKED BY INTERRUPTION");
					}
				}
				
			}
			/* (non-Javadoc)
			 * @see org.sadun.util.watchdog.ManagedWatchDog.Listener#checkOk(java.lang.Object)
			 */
			public void checkOk(Object obj) {
				System.out.println("Check ok (param="+obj+")");
			}
			
			/* (non-Javadoc)
			 * @see org.sadun.util.watchdog.ManagedWatchDog.Listener#checkImpossible(java.lang.Object, org.sadun.util.watchdog.WatchDogException)
			 */
			public void checkImpossible(Object obj, WatchDogException e) {
				System.err.println("Check impossible (param="+obj+")");
				e.printStackTrace(System.err);
			}
		};
		
		wd.addListener(listener);
		wd.setDaemon(false);
		wd.start();
	}
}
