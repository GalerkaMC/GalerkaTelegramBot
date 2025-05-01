package org.justiks.telegram.routers;

/**
 * Router pattern, all routers should implement it
 * @param <T>
 */
public interface Router<T> {
    void route(T t);
}
