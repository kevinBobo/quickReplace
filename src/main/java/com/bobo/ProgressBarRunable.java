package com.bobo;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author bobo
 * @Description:
 * @date 2018-11-30 21:05
 */
public class ProgressBarRunable implements Runnable {

    private ConsoleProgressBar consoleProgressBar;

    private List<ReplaceRunnable> runnables;

    private CountDownLatch countDownLatch;


    public ProgressBarRunable(ConsoleProgressBar consoleProgressBar, List<ReplaceRunnable> runnables, CountDownLatch countDownLatch) {
        this.consoleProgressBar = consoleProgressBar;
        this.runnables = runnables;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        while (true){
            long sum = runnables.stream().mapToLong(ReplaceRunnable::getProgress).sum();
            consoleProgressBar.show(sum);
            if (consoleProgressBar.isComplete()){
                break;
            }else {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        countDownLatch.countDown();
    }
}
