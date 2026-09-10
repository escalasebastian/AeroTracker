package com.aerotracker.exception;

/**
 * Raised when someone tries to track a route that nobody is monitoring yet while the platform
 * is already watching as many distinct routes as its price provider quota allows.
 * <p>
 * Joining a route that is already being tracked never triggers this: those subscribers share
 * the existing price lookups and cost nothing extra.
 */
public class RouteLimitExceededException extends RuntimeException {

    private final long limit;

    public RouteLimitExceededException(long limit) {
        super("Tracking limit reached: the platform is already monitoring " + limit + " distinct routes");
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }
}
