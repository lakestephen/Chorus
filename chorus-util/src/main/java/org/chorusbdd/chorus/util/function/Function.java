package org.chorusbdd.chorus.util.function;

/**
 * Created by nick on 09/01/15.
 */
public interface Function<T, R> {

    public R apply(T argument);

}