package org.justiks.telegram.handlers;


/**
 * Router handler interface
 * All handlers should implement it
 */
@FunctionalInterface
public interface Handler<T>{
    void handle(T t);
}
