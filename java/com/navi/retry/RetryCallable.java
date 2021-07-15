package com.navi.retry;

import com.navi.utils.Utils;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            Utils.logger.log(Level.WARNING, "Error executing callable", e);
            err = e;
            while (config.proceed(++attempt, err)) {
                try {
                    return core.call();
                } catch(Exception ex) {
                    Utils.logger.log(Level.WARNING, String.format("Error in retry %d", attempt), ex);
                    err = ex;
                }
            }
            // Here we throw the last exception
            throw err;
        }
    }
}
