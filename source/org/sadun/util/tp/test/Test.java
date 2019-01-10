package org.sadun.util.tp.test;

import java.util.Random;

import org.sadun.util.tp.ThreadPool;

class TestThread {

    protected String name;
    protected static Random rnd = new Random();
    private static int nextNumber=0;

    TestThread(String name) {
        if (name==null) name="test-thread";
        this.name=name+"-"+(++nextNumber);
    }

    TestThread() {
        this(null);
    }

    public String toString() { return name; }
}


class TestThread1 extends TestThread implements Runnable {

    TestThread1() {
        super("type1");
    }

    public void run() {
        boolean exit=false;
        do {
            System.out.println("{"+Thread.currentThread()+"} "+this+" sleeping for 1sec.");
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                System.out.println("{"+Thread.currentThread()+"} "+this+" interrupted");
            }
            System.out.println("{"+Thread.currentThread()+"} "+this+" awakened");
            int n=rnd.nextInt(3);
            switch(n) {
                case 0:
                     throw new RuntimeException(this+" failed!");
                case 1:
                    // Continue
                    System.out.println("{"+Thread.currentThread()+"} "+this+" continuing processing");
                    break;
                case 2:
                    System.out.println("{"+Thread.currentThread()+"} "+this+" exiting");
                    exit=true;
                    break;
            }
        } while(!exit);
        System.out.println("{"+Thread.currentThread()+"} "+this+" terminated");
    }
}


/**
 * A program to test thread pools
 */
public class Test {

    public static void main(String args[]) throws Exception {

        ThreadPool tp = new ThreadPool(1, false);
        do {
            if (tp.getQueueSize() < 5) {
                System.out.println("{"+Thread.currentThread()+"} "+"Creating new test thread"+" ("+tp.getQueueSize()+" in queue)");
                tp.start(new TestThread1());
            } else System.out.println("{"+Thread.currentThread()+"} "+"Too many threads, waiting for queue to get smaller");
            Thread.sleep(500);
        } while(true);
    }

}