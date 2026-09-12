package com.aerotracker.exception;

/**
 * Raised when someone tries to create a new alert while already holding as many active alerts as
 * one person may keep.
 * <p>
 * The distinct routes the platform can monitor are shared by everyone, so without this cap a single
 * user could take every slot. Updating the target price of an alert the user already has never
 * triggers it.
 */
public class UserRouteLimitExceededException extends RuntimeException {

    private final long limit;

    public UserRouteLimitExceededException(long limit) {
        super("Alert limit reached: the user already has " + limit + " active alerts");
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }
}
