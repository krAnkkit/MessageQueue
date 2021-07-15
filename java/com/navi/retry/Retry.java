package com.navi.retry;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Retry {

    private Retry() {
    }

    public interface Config {

        /**
         * This is expected to be a blocking call. The function waits and return true only when it is ready to execute.
         * For retries with fixed or exponential backoff, this method must return true after elapsing the backoff time.
         * @param attempt
         * @param err
         * @return true if it is okay to execute
         */
        boolean proceed(int attempt, Exception err);

    }

    public static class None implements Config {

        @Override
        public boolean proceed(final int attempt, Exception err) {
            return false;
        }
    }

    public static class ExponentialBackoff implements Config {

        private int maxRetries;
        private int baseDelay;

        public ExponentialBackoff(int max, int delay) {
            maxRetries = max;
            baseDelay = delay;
        }

        @Override
        public boolean proceed(int attempt, Exception err) {
            int waitTime = new Double(Math.pow(baseDelay, attempt)).intValue(); // in seconds
            try {
                if(attempt<=maxRetries) {
                    Thread.sleep(waitTime * 1000L);
                    return true;
                } else {
                    return false;
                }
            } catch (InterruptedException e) {
                Logger.getGlobal().log(Level.WARNING, "Interrupted while waiting on backoff", e);
                return false;
            }
        }
    }

}
