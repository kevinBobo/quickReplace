package com.bobo;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

/**
 * @author bobo
 * @Description:
 * @date 2018-11-30 11:37
 */
public class ReplaceRunnable implements Runnable {

    private RandomAccessFile rdf;
    private RandomAccessFile rdtempf;
    private long startIndex;
    private long endIndex;
    private String[] replaces;
    private CountDownLatch countDownLatch;
    private String threadName;
    private long progress;

    public ReplaceRunnable(RandomAccessFile rdf, RandomAccessFile rdtempf, long startIndex, long endIndex, String[] replaces, CountDownLatch countDownLatch, String threadName) {
        this.rdf = rdf;
        this.rdtempf = rdtempf;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.replaces = replaces;
        this.countDownLatch = countDownLatch;
        this.threadName = threadName;
    }

    @Override
    public void run() {
        System.out.println("---------线程:" + threadName + "开始执行-----------------");
        System.out.println("线程:" + threadName + "  参数 startIndex：" + startIndex + "  endIndex" + endIndex);
        try {
            if (startIndex != 0) {
                rdf.seek(startIndex);
                rdf.readLine();
                startIndex = rdf.getFilePointer();
            }
            rdf.seek(startIndex);
            rdtempf.seek(startIndex);
            int replaceCount = 0;
            while (true) {
                String line = rdf.readLine();
                progress = rdf.getFilePointer()-startIndex;
                if (line != null) {
                    for (String replace : replaces) {
                        String preLine = line;
                        line = line.replaceAll(replace, "");
                        if (line.length() != preLine.length()) {
                            replaceCount++;
                        }
                    }
                    if (StringUtils.isNotBlank(line)) {
                        rdtempf.write(line.getBytes());
                        rdtempf.write("\r\n".getBytes());
                    }
                }
                if (rdf.getFilePointer() >= endIndex) {
                    ReplaceCount.replaceCount.getAndAdd(replaceCount);
                    return;
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            countDownLatch.countDown();
        }
    }

    public long getProgress(){
        return this.progress;
    }

}
