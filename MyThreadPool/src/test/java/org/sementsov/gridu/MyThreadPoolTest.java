package org.sementsov.gridu;

import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class MyThreadPoolTest {

    private static final long DEFAULT_DELAY = 100;

    private MyThreadPool myThreadPool;

    @Test
    public void shouldSubmitAndRunSimpleTask() throws InterruptedException {
        //given
        Deque<Runnable> queue = new LinkedList<>();
        myThreadPool = new MyThreadPool(2, queue);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final String msg = "Hi";
        Runnable myRunnable = getSimpleRunnable(outputStream, msg);

        //when
        myThreadPool.submit(myRunnable);
        Thread.sleep(DEFAULT_DELAY);

        //then
        assertTrue("Messages are not equal", outputStream.toString().contains(msg));
    }

    @Test
    public void shouldSubmitAndRunSimpleTaskWithDelay() throws InterruptedException {
        //given
        Deque<Runnable> queue = new LinkedList<>();
        myThreadPool = new MyThreadPool(2, queue);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final String msg = "Hi";
        Runnable myRunnable = getSimpleRunnable(outputStream, msg);
        final long delay = 1000;

        //when
        myThreadPool.submit(myRunnable, delay);
        Thread.sleep(DEFAULT_DELAY);

        //then
        assertNotEquals("Messages are equal. Wrong behaviour", msg, outputStream.toString());
        Thread.sleep(delay);

        assertTrue("Messages are not equal. Wrong behaviour", outputStream.toString().contains(msg));
    }

    //check there are no lost tasks and all threads work
    @Test
    public void shouldExecuteSeveralTasksViaSeveralThreads() throws InterruptedException {
        //given
        Deque<Runnable> queue = new LinkedList<>();
        final int threadCount = 2;
        myThreadPool = new MyThreadPool(threadCount, queue);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final String msg = "Hi";

        final int tasksCount = 100;
        List<Runnable> runnableList = new ArrayList<>(tasksCount);
        for (int i = 0; i < tasksCount; i++) {
            runnableList.add(getRunnableWithThreadDescription(outputStream, String.valueOf(i)));
        }

        //when
        runnableList.forEach(runnable -> myThreadPool.submit(runnable));
        Thread.sleep(DEFAULT_DELAY);

//        System.out.println(outputStream.toString());

        //then
        for (int i = 0; i < runnableList.size(); i++) {
            assertTrue("Output stream does not contain runnable's result", outputStream.toString().contains(addNewLineChar(String.valueOf(i))));
        }

        for (int i = 0; i < threadCount; i++) {
            assertTrue("Thread #" + i + " did not work", outputStream.toString().contains("#" + i));
        }
    }

    @Test
    public void shouldExecuteNotDelayedTaskBefore() throws InterruptedException {
        //given
        Deque<Runnable> queue = new LinkedList<>();
        myThreadPool = new MyThreadPool(2, queue);
        final OutputStream outputStream = new ByteArrayOutputStream();
        Runnable myRunnable1 = getSimpleRunnable(outputStream, "1");
        Runnable myRunnable2 = getSimpleRunnable(outputStream, "2");
        final long delay1 = 1000;

        //when
        myThreadPool.submit(myRunnable1, delay1);
        myThreadPool.submit(myRunnable2);
        Thread.sleep(DEFAULT_DELAY);

        //then
        assertTrue("Output does not contain 2", outputStream.toString().contains("2"));
        assertFalse("Output contains 1", outputStream.toString().contains("1"));
        Thread.sleep(delay1);

        assertTrue("Output does not contain 1", outputStream.toString().contains("1"));
    }

    private Runnable getSimpleRunnable(final OutputStream outputStream, final String msg) {
        return () -> {
            try {
                outputStream.write(addNewLineChar(msg).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private Runnable getRunnableWithThreadDescription(final OutputStream outputStream, final String msg) {
        return () -> {
            try {
                outputStream.write((Thread.currentThread().getName() + ". Msg = " + addNewLineChar(msg)).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    @After
    public void shutdownThreadPool() {
        if (myThreadPool != null) {
            myThreadPool.shutdown();
            myThreadPool = null;
        }
    }

    private String addNewLineChar(String input) {
        return input + '\n';
    }

//    private Runnable createRunnableTestTask(PrintStream printStream) {
//        Runnable runnable = new Runnable() {
//            public void run() {
//                printStream.println(factorial());
//            }
//        }
//        return null;
//    }
}