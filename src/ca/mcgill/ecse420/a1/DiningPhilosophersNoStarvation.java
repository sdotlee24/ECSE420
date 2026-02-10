package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophersNoStarvation {

    public static void main(String[] args) {

        int numberOfPhilosophers = 5;
        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        Lock[] chopsticks = new Lock[numberOfPhilosophers];

        //initialize chopstick locks
        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new ReentrantLock(true); //fair policy avoids starvation
        }
        // initialize philosophers with new rule (last philosopher picks up reverse order chopsticks)
        for (int i = 0; i < numberOfPhilosophers; i++) {
            Lock leftChopstick = chopsticks[i];
            Lock rightChopstick = chopsticks[(i+1) % numberOfPhilosophers];
            // last philosopher picks up chopstick in reverse order to avoid deadlock
            if (i == numberOfPhilosophers - 1) {
                philosophers[i] = new Philosopher(i, rightChopstick, leftChopstick);
            } else {
                philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick);
            }
        }
        // creating philosopher threads.
        ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);
        for (Philosopher philosopher : philosophers) {
            executor.execute(philosopher);
        }
    }

    public static class Philosopher implements Runnable {
        private final int idx;
        //we use first, secondChopstick instead of the left/right convention used before, since not all philosophers pick up the left one first
        private final Lock firstChopstick;
        private final Lock secondChopstick;
        private int mealsAte = 0; //init to 0 "mealsAte" is used to to check if starvation still occurs

        Philosopher(int idx, Lock firstChopstick, Lock secondChopstick) {
            this.idx = idx;
            this.firstChopstick = firstChopstick;
            this.secondChopstick = secondChopstick;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // each philosopher always tries to get the left chopstick first
                    System.out.println("philosopher" + this.idx + " is trying to picking up first chopstick");
                    this.firstChopstick.lock();
                    System.out.println("philosopher" + this.idx + " acquired first chopstick");
                    Thread.sleep(1000);

                    System.out.println("philosopher" + this.idx + " is trying to picking up second chopstick");
                    this.secondChopstick.lock();
                    System.out.println("philosopher" + this.idx + " acquired second chopstick, eating");

                    this.mealsAte += 1; //increment the # of meals this philosopher ate. even-ish numbers for all philosophers indicate no starvation.
                    Thread.sleep(1000); // eating

                    System.out.println("Done eating, put down second chopstick by philosopher: " + this.idx);
                    this.secondChopstick.unlock();
                    System.out.println("Done eating, put down first chopstick by philosopher: " + this.idx);
                    this.firstChopstick.unlock();

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

}
