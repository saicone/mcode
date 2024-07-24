package com.saicone.mcode.util.cache;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface LoadingCache<K, V> extends Cache<K, V> {

    V get(K key);

    @NotNull
    Map<K, V> getAll(Iterable<? extends K> keys);

    @NotNull
    CompletableFuture<V> refresh(K key);

    @NotNull
    CompletableFuture<Map<K, V>> refreshAll(Iterable<? extends K> keys);
}
