package com.huawei.test.myraspweb.detector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegexDetector {

    private static ExecutorService pool = Executors.newFixedThreadPool(20);

    class RegexDetectRunnable implements Runnable {

        String regex;

        Future<?> theFuture;

        long starttime;
        long endtime = -1;

        public long getStarttime() {
            return starttime;
        }

        public void setStarttime(long starttime) {
            this.starttime = starttime;
        }

        public Future<?> getTheFuture() {
            return theFuture;
        }

        public void setTheFuture(Future<?> theFuture) {
            this.theFuture = theFuture;
        }

        public RegexDetectRunnable(String regex) {
            this.regex = regex;
        }

        @Override
        public void run() {

        }

    }

    public void submitRunnable(String regex)
    {
        RegexDetectRunnable regexDetectRunnable = new RegexDetectRunnable(regex);
        Future<?> submit = pool.submit(regexDetectRunnable);
        regexDetectRunnable.setTheFuture(submit);
        regexDetectRunnable.setStarttime(System.currentTimeMillis());
    }

}
