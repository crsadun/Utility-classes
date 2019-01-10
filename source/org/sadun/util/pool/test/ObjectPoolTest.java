package org.sadun.util.pool.test;

import java.util.Random;

import org.sadun.util.pool.ObjectPool;

public class ObjectPoolTest {

    ObjectPool pool;
    Random random = new Random();
    Thread []threads = new Thread[2];

    class TestThread extends Thread {

        private int tn;

        TestThread(int tn) { super(""+tn); this.tn=tn; }

        public void run() {
            while(true) {
                try {
                    System.out.println(this+" acquiring object");
                    System.out.flush();
                    Object obj = pool.acquire();
                    System.out.println(this+" - object "+obj+" acquired");
                    int sleepTime=random.nextInt(100);
                    System.out.println(this+" sleeping for "+sleepTime+"ms");
                    Thread.sleep(sleepTime);
                    
                    if (random.nextBoolean()) {
                    	System.out.println(this+" renewing object "+obj);
                    	obj=pool.renew(obj);
                    }
                    
                    System.out.println(this+" releasing object "+obj);
                    pool.release(obj);
                    System.out.println(this+" - object "+obj+" released");
                    System.out.println(pool);
                    sleepTime=random.nextInt(100);
                    System.out.println(this+" sleeping for "+sleepTime+"ms");
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                }
            }
        }

    }

    public ObjectPoolTest() throws Exception {
        //pool=ExtendedObjectPool.newPool(1, "java.lang.String", new Object[] { new String("foo") });
        pool=ObjectPool.newPool(1, "java.lang.Object", new Object[] { });
        for(int i=0;i<threads.length;i++) {
            threads[i]=new TestThread(i);
        }
    }

    public void run() {
        for(int i=0;i<threads.length;i++) {
            threads[i].start();
        }
    }

    public static void main(String args[]) throws Exception {
        ObjectPoolTest test = new ObjectPoolTest();
        test.run();
    }

}

