/*
 * Created on Sep 10, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;


/**
 * A generator of IDs unique even across different machines.
 * 
 * @author Cristiano Sadun
 */
public class UniqueIdGenerator {
	
	private Object lock = new Object();
	private static UniqueIdGenerator instance = new UniqueIdGenerator();
	private long counter;
	private Random random=new Random();
	
	private String hostName;

	protected UniqueIdGenerator() {
		try {
			hostName=InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new Error("Cannot initialize the unique id generator");
		}
	}

	/**
	 * Return the JVM-unique instance of the generator.
	 * @return the JVM-unique instance of the generator.
	 */
	public static UniqueIdGenerator getInstance() {
		return instance;
	}
	
	public String getNextId(String hint) {
		if (hint==null) hint="__default";
		synchronized(lock) {
			 String id = random.nextInt(100000)+hostName+"_"+hint+"_"+counter;
			 counter++;
			 return id;
		}
	}
	
	public String getNextId() {
		return getNextId("__default");
	}
	
	public static void main(String args[]) {
		while(true) {
			System.out.println(UniqueIdGenerator.getInstance().getNextId());
		}
	}

}
