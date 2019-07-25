package org.sementsov.gridu;

import java.util.Deque;
import java.util.PriorityQueue;

public class MyThreadPool {

    private final int poolSize;
    private final LongLiveThread[] threads;
    private final Deque<Runnable> queue;

    //delayed
    private final ZookeeperThread zookeeper;
    private final PriorityQueue<RunnableWithDelay> delayedQueue;

    public MyThreadPool(int poolSize, Deque<Runnable> queue) {
        this.poolSize = poolSize;
        this.queue = queue;
        threads = new LongLiveThread[poolSize];

        for (int i = 0; i < poolSize; i++) {
            threads[i] = new LongLiveThread();
            threads[i].setName("LongLiveThread #" + i);
            threads[i].start();
        }

        delayedQueue = new PriorityQueue<>((task1, task2) -> (int) (task1.getTimeToRun() - task2.getTimeToRun()));
        zookeeper = new ZookeeperThread();
        zookeeper.start();
    }

    public void submit(Runnable task) {
        submit(task, false);
    }

    private void submit(Runnable task, boolean addFirst) {
        if (task == null) {
            throw new IllegalArgumentException("The task is null");
        }

        synchronized (queue) {
            if (addFirst) {
                queue.addFirst(task);
            } else {
                queue.addLast(task);
            }
            queue.notify();
        }
    }

    public void submit(Runnable task, long delay) {
        if (delay <= 0) {
            submit(task);
            return;
        }

        synchronized (delayedQueue) {
            delayedQueue.add(new RunnableWithDelay(task, delay));
            delayedQueue.notify();
        }
    }

    private class ZookeeperThread extends Thread {
        public void run() {
            while (true) {
                synchronized (delayedQueue) {
                    while (delayedQueue.isEmpty()) {
                        try {
                            delayedQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    final RunnableWithDelay runnableWithDelay = delayedQueue.peek();
                    if (runnableWithDelay.timeToRun < System.currentTimeMillis()) {
                        delayedQueue.poll();

                        submit(runnableWithDelay.task, true);
                    }

                    while(!delayedQueue.isEmpty()) {
                        final RunnableWithDelay nextRunnable = delayedQueue.peek();
                        final long delay = nextRunnable.timeToRun - System.currentTimeMillis();
                        if (delay <= 0) {
                            delayedQueue.poll();

                            submit(nextRunnable.task, true);
                        } else {
                            try {
                                delayedQueue.wait(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private class RunnableWithDelay implements Runnable {

        private final Runnable task;
        private long timeToRun;

        RunnableWithDelay(Runnable task, long delay) {
            this.task = task;
            this.timeToRun = System.currentTimeMillis() + delay;
        }

        public long getTimeToRun() {
            return timeToRun;
        }

        @Override
        public void run() {
            task.run();
        }
    }

    private class LongLiveThread extends Thread {
        public void run() {
            Runnable task;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            System.out.println("An error occurred while queue is waiting: " + e.getMessage());
                        }
                    }
                    task = queue.poll();
                }

                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.out.println("Thread pool is interrupted due to an issue: " + e.getMessage());
                }
            }
        }
    }

    public void shutdown() {
        System.out.println("Shutting down thread pool");
        for (int i = 0; i < poolSize; i++) {
            threads[i] = null;
        }
    }

}
