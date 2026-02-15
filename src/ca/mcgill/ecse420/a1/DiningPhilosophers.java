package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {

    public static void main(String[] args) {

        int numberOfPhilosophers = 5;
        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        Lock[] chopsticks = new Lock[numberOfPhilosophers];

        //initialize chopstick locks
        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new ReentrantLock();
        }
        // initialize philosophers
        for (int i = 0; i < numberOfPhilosophers; i++) {
            Lock leftChopstick = chopsticks[i];
            Lock rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];
            philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick);
        }
        // creating philosopher threads.
        ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);
        for (Philosopher philosopher : philosophers) {
            executor.execute(philosopher);
        }
    }

    public static class Philosopher implements Runnable {
        private final int idx;
        private final Lock leftChopstick;
        private final Lock rightChopstick;

        Philosopher(int idx, Lock leftChopstick, Lock rightChopstick) {
            this.idx = idx;
            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // each philosopher always tries to get the left chopstick first
                    System.out.println("philosopher" + this.idx + " is trying to picking up left chopstick");
                    this.leftChopstick.lock();
                    System.out.println("philosopher" + this.idx + " acquired left chopstick");
                    Thread.sleep(1000);

                    System.out.println("philosopher" + this.idx + " is trying to picking up right chopstick");
                    this.rightChopstick.lock();
                    System.out.println("philosopher" + this.idx + " acquired right chopstick, eating");

                    Thread.sleep(1000); // eating

                    System.out.println("Done eating, put down right chopstick by philosopher: " + this.idx);
                    this.rightChopstick.unlock();
                    System.out.println("Done eating, put down left chopstick by philosopher: " + this.idx);
                    this.leftChopstick.unlock();

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

}
