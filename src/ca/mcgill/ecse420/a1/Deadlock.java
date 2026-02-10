package ca.mcgill.ecse420.a1;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Deadlock {
    private static final Lock lockA = new ReentrantLock();
    private static final Lock lockB = new ReentrantLock();

    public static void main(String[] args) {
        //thread 1 obtains lock A THEN B
        Thread t1 = new Thread(() -> {
            System.out.println("thread one attempting to acquire lock a");
            lockA.lock();
            System.out.println("thread one acquired lock a");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("SHOULD DEADLOCK HERE (thread 1 trying to acquire lock b");
            lockB.lock();
            System.out.println("got lock b");

            lockB.unlock();
            lockA.unlock();
        });
        // thread 2 obtains lock in reverse order (B then A)
        Thread t2 = new Thread(() -> {
            System.out.println("thread two attempting to acquire lock b first");
            lockB.lock();
            System.out.println("lock b acquired by thread2");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("trying to get locka  (thread 2), deadlock");
            lockA.lock(); //deadlock
            System.out.println("got lock A");

            lockA.unlock();
            lockB.unlock();
        });

        t1.start();
        t2.start();
    }

}
