package com.maxdemarzi;

import org.roaringbitmap.IntConsumer;

@FunctionalInterface
public interface ThrowingConsumer extends IntConsumer {

    @Override
    default void accept(int elem) {
        try {
            acceptThrows(elem);
        } catch (final Exception e) {
            /* Do whatever here ... */
            System.out.println("handling an exception...");
            throw new RuntimeException(e);
        }
    }
    void acceptThrows(int elem) throws Exception;
}
