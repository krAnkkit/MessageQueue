package com.navi.retry;

import java.util.concurrent.Callable;

public class RetryCallable<T> implements Callable<T> {
    private Callable<T> core;
    private Retry.Config config;

    private int attempt;
    private Exception err;

    public RetryCallable(Callable<T> that, Retry.Config config) {
        core = that;
        this.config = config;
        attempt = 0;
    }

    @Override
    public T call() throws Exception {
        try{
            return core.call();
        } catch(Exception e) {
            err = e;
            while (config.proceed(++attempt, err)) {
                try {
                    return core.call();
                } catch(Exception ex) {
                    err = ex;
                }
            }
            // Here we throw the last exception
            throw err;
        }
    }
}
