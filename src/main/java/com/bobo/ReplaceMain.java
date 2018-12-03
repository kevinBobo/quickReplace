package com.bobo;

import cn.hutool.core.lang.UUID;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author bobo
 * @Description:
 * @date 2018-11-30 10:57
 */
public class ReplaceMain {

    public static void main(String[] args) {

        System.out.println("=============开始执行替换================");

        if (args.length < 3) {
            System.out.println("-------请输入完整的参数 如: replace.jar path threadNum-----");
            System.exit(0);
        }

        String filePath = args[0];
        int threadNum = Integer.valueOf(args[1]);
        String replaceStr = args[2];
        if (StringUtils.isBlank(replaceStr)) {
            System.out.println("----------替换目录不能为空------------");
            System.exit(0);
        }

        String[] replaces = replaceStr.split(",");
        for (String s : replaces) {
            System.out.println("需要替换的正则:" + s);
        }


        if (StringUtils.isBlank(filePath)) {
            System.out.println("----------文件地址不能为空,程序停止------------");
            System.exit(0);
        }
        String endprfix = "";
        int index = filePath.lastIndexOf(".");
        if (index != -1) {
            endprfix = filePath.substring(filePath.lastIndexOf("."));
        }
        String tempPath = filePath.substring(0, filePath.lastIndexOf("/") + 1) + UUID.randomUUID().toString().replaceAll("-", "") + endprfix;
        File tempFile = new File(tempPath);
        try {
            System.out.println("=============开始创建临时文件================");
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("-----------创建临时文件失败---------------");
            System.exit(1);
        }
        CountDownLatch countDownLatch = new CountDownLatch(threadNum+1);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        long startTime  = System.currentTimeMillis();
        ConsoleProgressBar progressBar;
        List<ReplaceRunnable> runnables = new ArrayList<>();
        try {
            final long size = new RandomAccessFile(filePath, "rw").length();
            progressBar = new ConsoleProgressBar(size);
            long avg = size / threadNum;
            System.out.println("=============开始过滤数据================");

            for (int i = 0; i < threadNum; i++) {
                long startIndex = i * avg;
                long endIndex = (i + 1) * avg;
                ReplaceRunnable replaceRunnable = new ReplaceRunnable(new RandomAccessFile(filePath, "r"), new RandomAccessFile(tempPath, "rw"), startIndex, endIndex, replaces, countDownLatch, String.valueOf(i + 1));
                runnables.add(replaceRunnable);
                executor.execute(replaceRunnable);
            }
            executor.execute(new ProgressBarRunable(progressBar,runnables,countDownLatch));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("错误的文件路径，无法检测到文件，程序停止");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("错误的文件路径，无法检测到文件，程序停止:{}");
            System.exit(1);
        }

        try {
            countDownLatch.await();
            executor.shutdown();
            System.out.println("======执行完毕！总共替换了:" + ReplaceCount.replaceCount.get() + "个数据=========");
            System.out.println("======总共用时："+((double)System.currentTimeMillis()-startTime)/1000+"秒===============");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
