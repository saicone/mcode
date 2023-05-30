package com.saicone.mcode.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CacheSet<E> implements Set<E> {

    private final long cacheTime;
    private final long checkPeriod;
    private Consumer<E> removalConsumer;

    private final Map<E, Long> cache = new HashMap<>();
    private long last = -1;
    private boolean onRun = false;
    private Thread thread = null;

    public CacheSet() {
        this(10000);
    }

    public CacheSet(long cacheTime) {
        this(cacheTime, cacheTime / 2);
    }

    public CacheSet(long cacheTime, long checkPeriod) {
        this(cacheTime, checkPeriod, TimeUnit.MILLISECONDS, null);
    }

    public CacheSet(long cacheTime, long checkPeriod, @NotNull TimeUnit timeUnit) {
        this(cacheTime, checkPeriod, timeUnit, null);
    }

    public CacheSet(long cacheTime, long checkPeriod, @Nullable Consumer<E> removalConsumer) {
        this(cacheTime, checkPeriod, TimeUnit.MILLISECONDS, removalConsumer);
    }

    public CacheSet(long cacheTime, long checkPeriod, @NotNull TimeUnit timeUnit, @Nullable Consumer<E> removalConsumer) {
        this.cacheTime = timeUnit.toMillis(cacheTime);
        this.checkPeriod = timeUnit.toMillis(checkPeriod);
        this.removalConsumer = removalConsumer;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public long getCheckPeriod() {
        return checkPeriod;
    }

    @Nullable
    public Consumer<E> getRemovalConsumer() {
        return removalConsumer;
    }

    @NotNull
    public Map<E, Long> getCache() {
        return cache;
    }

    public long getLast() {
        return last;
    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    public boolean isOnRun() {
        return onRun;
    }

    public long getUntilTime(@NotNull E e) {
        final Long cachedTime = cache.get(e);
        if (cachedTime == null) {
            return 0;
        }
        final long time = System.currentTimeMillis();
        if (cachedTime < time) {
            return 0;
        }
        return cachedTime - time;
    }

    @NotNull
    @Contract("_ -> this")
    public CacheSet<E> setRemovalConsumer(@Nullable Consumer<E> removalConsumer) {
        this.removalConsumer = removalConsumer;
        return this;
    }

    @Nullable
    public Long add(@NotNull E e, long time) {
        final long current = System.currentTimeMillis() + time;
        final Long mapped = cache.put(e, current);
        if (!isRunning()) {
            // Update last after check if not running
            last = Math.max(current, last);
            // Run check after update last
            start();
        } else {
            // Update if current is more than last
            last = Math.max(current, last);
        }
        return mapped;
    }

    public boolean containsOrAdd(@NotNull E e) {
        if (cache.containsKey(e)) {
            return true;
        } else {
            add(e);
            return false;
        }
    }

    public boolean containsOrAdd(@NotNull E e, long time) {
        if (cache.containsKey(e)) {
            return true;
        } else {
            add(e, time);
            return false;
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    try {
                        run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                    onRun = false;

                    try {
                        Thread.sleep(checkPeriod);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        onRun = false;
    }

    public void stopAndClear() {
        stop();
        clear();
    }

    public void run() {
        if (onRun) {
            return;
        }

        onRun = true;

        final long time = System.currentTimeMillis();
        if (removalConsumer != null) {
            cache.entrySet().removeIf(entry -> {
                if (entry.getValue() < time) {
                    removalConsumer.accept(entry.getKey());
                    return true;
                }
                return false;
            });
        } else {
            cache.entrySet().removeIf(entry -> entry.getValue() < time);
        }
        if (cache.isEmpty() && (last < time || last < 0)) {
            stopAndClear();
        }
    }

    // Vanilla set methods

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return cache.containsKey(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return cache.keySet().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return cache.keySet().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return cache.keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return add(e, cacheTime) != null;
    }

    @Override
    public boolean remove(Object o) {
        final Long time = cache.remove(o);
        return time != null;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!cache.containsKey(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean result = false;
        for (E e : c) {
            if (result) {
                add(e);
            } else {
                result = add(e);
            }
        }
        return result;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return cache.entrySet().removeIf(entry -> !c.contains(entry.getKey()));
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean result = false;
        for (Object o : c) {
            if (result) {
                remove(o);
            } else {
                result = remove(o);
            }
        }
        return result;
    }

    @Override
    public void clear() {
        last = -1;
        cache.clear();
        stop();
    }
}
