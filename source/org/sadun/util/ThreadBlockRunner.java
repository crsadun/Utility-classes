package org.sadun.util;

import java.util.Random;

/**
 * Contains a thread to run, plus related information.
 */
class ThreadDescriptor implements Cloneable {
	
	private Thread thread;
	private boolean synchronizeWithPrevious;
	
	ThreadDescriptor(Thread t, boolean synchronizeWithPrevious) {
		this.thread=t;
		this.synchronizeWithPrevious=synchronizeWithPrevious;
	}
	
	ThreadDescriptor(Thread t) {
		this(t, false);
	}
	
	public Object clone() {
		return new ThreadDescriptor(thread,synchronizeWithPrevious);
	}
	
	/**
	 * Returns the synchronizeWithPrevious.
	 * @return boolean
	 */
	public boolean isSynchronizeWithPrevious() {
		return synchronizeWithPrevious;
	}

	/**
	 * Sets the synchronizeWithPrevious.
	 * @param synchronizeWithPrevious The synchronizeWithPrevious to set
	 */
	public void setSynchronizeWithPrevious(boolean synchronizeWithPrevious) {
		this.synchronizeWithPrevious = synchronizeWithPrevious;
	}

	/**
	 * Returns the thread.
	 * @return Thread
	 */
	public Thread getThread() {
		return thread;
	}
	
	public static ThreadDescriptor[] toDefaultDescriptorArray(Thread [] threads) {
		ThreadDescriptor[] td = new ThreadDescriptor[threads.length];
		for(int i=0;i<threads.length;i++) {
			td[i]=new ThreadDescriptor(threads[i]);			
		}
		return td;
	}
	
	/**
	 * Sets the thread.
	 * @param thread The thread to set
	 */
	public void setThread(Thread thread) {
		this.thread = thread;
	}

}

/**
 * Runs a block of threads within an array, with a maximum number of concurrent threads,
 * and returning only when all the threads in the block have finished.
 * <p>
 * Optionally, each thread (but the first in each block) can be synchronized with the previous one, that is,
 * it waits to start for the previous one is terminated.
 * <p>
 * To just run a set of thread concurrently and wait for all of them to be terminated, simply use the 
 * {@link #ThreadBlockRunner(Thread[])} constructor and {@link #runAll()} as in the following
 * snippet:
 * <pre>
 *  ThreadBlockRunner tbr = new ThreadBlocRunner( threads ); <font color="green">// threads is an array of thread objects</font>
 *  tbr.runAll();
 * </pre> 
 * 
 * @author cris sadun
 * @version 2.1
 */
public class ThreadBlockRunner {
	
	private long sleepTime=1000L;
	private ThreadDescriptor []threadDescriptors;
	private int maxThreads;

	/**
	 * Create a runner for the given array of threads, which will run a maximum of maxThreads
	 * threads concurrently.
	 * 
	 * @param threads the array of threads to run
	 * @param maxThreads the maximum number of concurrent arrays to run
	 */	
	public ThreadBlockRunner(Thread []threads, int maxThreads) {
		this.threadDescriptors=ThreadDescriptor.toDefaultDescriptorArray(threads);
		this.maxThreads=maxThreads;
		if (maxThreads < 1) throw new IllegalArgumentException("Invalid maxThreads value:"+maxThreads);
	}
	
	/**
	 * Create a runner for the given array of threads. The maximum number of threads used is set equal to the
	 * size of the array, so all will have a chance to run concurrently when {@link #runAll()} or {@link #runBlock(int, int)}
	 * are invoked.
	 * 
	 * @param threads the array of threads to run
	 */
	public ThreadBlockRunner(Thread []threads) {
	    this(threads, threads.length);
	    
	}
	
	private int findThreadIndex(Thread t) {
		for(int i=0;i<threadDescriptors.length;i++) {
			if (threadDescriptors[i].getThread() == t) return i;
		}
		throw new IllegalArgumentException("Programming error: thread "+t.getName()+" is not controlled by the this ThreadBlockRunner object");
	}
	
	/**
	 * Set a certain thread in the threads passed at construction to be synchronized with the previous one. 
	 * <p> If at execution time the thread results the first in a block, the synchronization will be ignored.
	 * @param thread one of the threads passed at {@link #ThreadBlockRunner(java.lang.Thread[], int) construction}.
	 * @param value if <b>true</b>, the thread will wait until the previous one has terminated before starting.
	 */
	public void setSynchronizedWithPrevious(Thread thread, boolean value) {
		setSynchronizedWithPrevious(findThreadIndex(thread), value);
		
	}
	
	/**
	 * Return whether or not a thread is synchronized with the previous one (see 
	 * {@link #setSynchronizedWithPrevious(java.lang.Thread, boolean) setSynchronizeWithPrevious()}).
	 * @param thread one of the threads passed at {@link #ThreadBlockRunner(java.lang.Thread[], int) construction}.
	 * @return whether or not the thread is synchronized with the previous one.
	 */
	public boolean isSynchronizedWithPrevious(Thread thread) {
		return isSynchronizedWithPrevious(findThreadIndex(thread));
	}
	
	/**
	 * Set a the i-th thread (realtive to the thread array passed at construction) to be synchronized with the 
	 * previous one. 
	 * <p> If at execution time the thread results the first in a block, the synchronization will be ignored.
	 * @param i a valid index in the thread array passed at {@link #ThreadBlockRunner(java.lang.Thread[], int) construction}.
	 * @param value if <b>true</b>, the thread will wait until the previous one has terminated before starting.
	 */
	public void setSynchronizedWithPrevious(int i, boolean value) {
		if (i<0 || i>=threadDescriptors.length) throw new IllegalArgumentException("Programming error: invalid thread index");
		threadDescriptors[i].setSynchronizeWithPrevious(value);
	}

	/**
	 * Return whether or not a thread is synchronized with the previous one (see 
	 * {@link #setSynchronizedWithPrevious(java.lang.Thread, boolean) setSynchronizeWithPrevious()}).
	 * @param i a valid index in the thread array passed at {@link #ThreadBlockRunner(java.lang.Thread[], int) construction}.
	 * @return whether or not the thread in the i-th position is synchronized with the previous one.
	 */
	public boolean isSynchronizedWithPrevious(int i) {
		if (i<0 || i>=threadDescriptors.length) throw new IllegalArgumentException("Programming error: invalid thread index");
		return threadDescriptors[i].isSynchronizeWithPrevious();
	}
	
	/**
	 * Run all threads from the array passed at construction.
	 * <p>
	 * If any thread's "synchronizeWithPrevious" flag has been set, the thread waits for 
	 * the previous thread to terminate before starting (the first thread flag state is ignored).
	 * <p>
	 * Note that the maximum number of threads running <i>concurrently</i> is the one set at construction.
	 *
	 */
	public void runAll() {
	    runBlock(0, threadDescriptors.length);
	}
	
	/**
	 * Run a block of consecutive threads from the array passed at construction.
	 * <p>
	 * If any thread's "synchronizeWithPrevious" flag has been set, the thread waits for 
	 * the previous thread to terminate before starting (the first thread flag state is ignored).
	 * 
	 * @param blockStart the index of the first thread to run, inclusive
	 * @param blockEnd the index of the last thread to run, exclusive
	 */
	public void runBlock(int blockStart, int blockEnd) {
		if(blockStart<0 || blockEnd>threadDescriptors.length || blockStart>blockEnd) 
			throw new IllegalArgumentException(threadDescriptors.length+" threads in the array. ("+blockStart+","+
			                                    blockEnd+") is an invalid range");
		
		/*
		 ThreadDescriptor [] threadDescriptors2=new ThreadDescriptor[threadDescriptors.length];
 		 System.arraycopy(threadDescriptors,0,threadDescriptors2,0,threadDescriptors.length);
		*/
		ThreadDescriptor [] threadDescriptors2=new ThreadDescriptor[threadDescriptors.length];
		for(int i=0;i<threadDescriptors2.length;i++) {
			threadDescriptors2[i]=(ThreadDescriptor)threadDescriptors[i].clone();
		}
		int count=0;	                                   
		int waitingForPreviousCount=0;
		boolean waitingForPrevious[] = new boolean[threadDescriptors2.length];
		boolean started[] = new boolean[threadDescriptors2.length];
		do {
			for(int i=blockStart;i<blockEnd;i++) {
				if (threadDescriptors2[i].getThread()==null) {
					//System.out.println(threadDescriptors[i].getThread().getName()+" has already terminated, skipping");
					continue;
				}
				
				// If the first entry is supposed to be synchronized, ignore the fact:
			
				if (i>blockStart &&
				    threadDescriptors2[i].isSynchronizeWithPrevious()) {
				    if (waitingForPrevious[i]==false) {
				    	waitingForPrevious[i]=true;
				    	waitingForPreviousCount++;
				    	// Do not start the thread - yet
				    	//System.out.println("Not starting "+threadDescriptors2[i].getThread().getName()+" since it must wait for previous to finish");
				    } else {
				    	if (! started[i]) {
					    	// Start the thread only if the previous is dead
					    	//System.out.print("Checking if "+threadDescriptors[i-1].getThread().getName()+" has finished..");
						    if (started[i-1] && ! threadDescriptors[i-1].getThread().isAlive()) {
						    	// Do not reset waitingForPrevious flag, to avoid restarts
						    	waitingForPreviousCount--;
						    	//System.out.println("Starting "+threadDescriptors2[i].getThread().getName()+" since previous has finished.");
						    	threadDescriptors2[i].getThread().start();
						    	started[i]=true;
						    	count++;
						    } else {
						    	//System.out.println("Not starting "+threadDescriptors2[i].getThread().getName()+" since previous has not finished yet");
						    }
				    	} else  {
				    		//System.out.println("Thread "+threadDescriptors[i].getThread().getName()+" has already started, skipping");
				    	}
				    }	
				    	
				} else {
					if (! started[i]) {
						started[i]=true;
						Thread tr=threadDescriptors2[i].getThread();
						//System.out.println("Starting "+tr.getName());
						tr.start();
						count++;
					}
				}
				
				if (count == maxThreads) {
					count=waitForOneToFinish(threadDescriptors2, started, blockStart, i+1);
					//assert(count >= 0);
					//assert(count <= maxThreads);
				}
			}			                         
			//System.out.println("("+waitingForPreviousCount+" threads waiting for a previous one to finish)");
			
			if (waitingForPreviousCount>0)  {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		} while (waitingForPreviousCount>0);
		
		waitForAllFinished(blockStart, blockEnd);
	}
	
	/**
	 * Wait for at least one thread in the given range to finish
	 * @return the number of alive threads in the given range
	 */
	private int waitForOneToFinish(ThreadDescriptor[] threads, boolean [] started, int start, int end) {
		int count, aliveCount;
		do {
			count=0;
			aliveCount=0;
			for(int i=start;i<end;i++) {
				if (!started[i]) continue;
				if (threads[i].getThread()!=null) {
					if (! threads[i].getThread().isAlive()) {
						threads[i].setThread(null);
						count++; 
					} else { aliveCount++; }
				} 
			}
			if (count==0)
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		} while(count==0);
		return aliveCount;
	}
	
	/**
	 *  Wait for all the threads within the given range in the "threads" member to be finished.
	 */
	private void waitForAllFinished(int start, int end) {
		int count;
		do {
			count=0;
			for(int i=start;i<end;i++) {
				if (threadDescriptors[i].getThread() == null) continue;
				if (threadDescriptors[i].getThread().isAlive()) count++; 
			}
			if (count>0)
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		} while(count>0);
	}
	
	private static Random testRandom = new Random();
	private static Object lock = new Object();

	private static class TestThread extends Thread {
		
		int count;
		
		TestThread(int count) {
			super("thread-"+count);
			this.count=count;
		}

		public void run() {
			long l=20L;
			if (count % 2 == 0) l=1000L;
			synchronized(lock) {
				System.out.println(getName()+" started");
				System.out.flush();
			}
			try {
				sleep(l);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			synchronized(lock) {
				System.out.println(getName()+" finished");
				System.out.println(getName()+" -------------------");
				System.out.flush();
			}
		}
	}
	
	/**
	 * A test method. 
	 */
	public static void main2(String args[]) {
		Thread [] threads = new Thread[3];
		Random random = new Random();
		for(int i=0;i<threads.length;i++) {
			threads[i]=new ThreadBlockRunner.TestThread(i);
		}
		
		//threads[0]=null;
		ThreadBlockRunner tbr = new ThreadBlockRunner(threads, 3);
		for(int i=0;i<threads.length;i++) {
			if (i==1) {
				tbr.setSynchronizedWithPrevious(i, true);
			}
		}
		
		tbr.runBlock(0,3);
		synchronized(lock) {
			System.out.println("********************");
		}
	}
	
	public static void main(String args[]) {
		Thread [] threads = new Thread[10];
		Random random = new Random();
		for(int i=0;i<threads.length;i++) {
			threads[i]=new ThreadBlockRunner.TestThread(i);
		}
		
		//threads[0]=null;
		ThreadBlockRunner tbr = new ThreadBlockRunner(threads, 3);
		for(int i=0;i<threads.length;i++) {
			//if (i>=5 && i<=8) {
			if (i>0 && random.nextBoolean()) {
				System.out.println("thread "+i+" waits for previous one to finish");
				tbr.setSynchronizedWithPrevious(i, true);
			}
		}
		
		
		tbr.runBlock(0,6);
		synchronized(lock) {
			System.out.println("********************");
		}
		tbr.runBlock(6, threads.length);
	}
}
