package com.saicone.mcode.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class CacheSet<E> implements Set<E> {

    protected final long expireTime;
    protected final Consumer<E> removalListener;

    @NotNull
    public static <T> CacheSet<T> of(long expireTime, @NotNull TimeUnit unit) {
        return of(expireTime, unit, null);
    }

    @NotNull
    public static <T> CacheSet<T> of(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<T> removalListener) {
        try {
            Class.forName("com.github.benmanes.caffeine.cache.Cache");
            return caffeine(expireTime, unit, removalListener);
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("com.google.common.cache.Cache");
            return guava(expireTime, unit, removalListener);
        } catch (ClassNotFoundException ignored) { }
        return thread(expireTime, unit, removalListener);
    }

    @NotNull
    public static <T> CacheSet.ThreadCacheSet<T> thread(long expireTime, @NotNull TimeUnit unit) {
        return thread(expireTime, unit, null);
    }

    @NotNull
    public static <T> CacheSet.ThreadCacheSet<T> thread(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<T> removalListener) {
        return new ThreadCacheSet<>(expireTime, unit, removalListener);
    }

    @NotNull
    public static <T> CacheSet.CaffeineCacheSet<T> caffeine(long expireTime, @NotNull TimeUnit unit) {
        return caffeine(expireTime, unit, null);
    }

    @NotNull
    public static <T> CacheSet.CaffeineCacheSet<T> caffeine(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<T> removalListener) {
        return new CaffeineCacheSet<>(expireTime, unit, removalListener);
    }

    @NotNull
    public static <T> CacheSet.GuavaCacheSet<T> guava(long expireTime, @NotNull TimeUnit unit) {
        return guava(expireTime, unit, null);
    }

    @NotNull
    public static <T> CacheSet.GuavaCacheSet<T> guava(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<T> removalListener) {
        return new GuavaCacheSet<>(expireTime, unit, removalListener);
    }

    public CacheSet(long expireTime, @NotNull TimeUnit unit) {
        this(expireTime, unit, null);
    }

    public CacheSet(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<E> removalListener) {
        this.expireTime = unit.toMillis(expireTime);
        this.removalListener = removalListener;
    }

    public long getExpireTime() {
        return expireTime;
    }

    @Nullable
    public Consumer<E> getRemovalListener() {
        return removalListener;
    }

    public long getUntilTime(@NotNull E e) {
        final Long cachedTime = get(e);
        if (cachedTime == null) {
            return 0;
        }
        final long time = System.currentTimeMillis();
        if (cachedTime < time) {
            return 0;
        }
        return cachedTime - time;
    }

    public boolean containsOrAdd(@NotNull E e) {
        if (contains(e)) {
            return true;
        } else {
            put(e);
            return false;
        }
    }

    @NotNull
    public abstract Map<E, Long> asMap();

    @Nullable
    public abstract Long get(@NotNull E e);

    @Nullable
    public abstract Long put(@NotNull E e);

    // Vanilla set methods

    @Override
    public int size() {
        return asMap().size();
    }

    @Override
    public boolean isEmpty() {
        return asMap().isEmpty();
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return asMap().keySet().iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return asMap().keySet().toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return asMap().keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return put(e) != null;
    }

    @Override
    public boolean remove(Object o) {
        final Long time = asMap().remove(o);
        return time != null;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
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
        return asMap().entrySet().removeIf(entry -> !c.contains(entry.getKey()));
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

    public static class ThreadCacheSet<E> extends CacheSet<E> {

        private final long checkPeriod;
        private final Map<E, Long> cache = new HashMap<>();
        private long last = -1;
        private boolean onRun = false;
        private Thread thread = null;

        public ThreadCacheSet(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<E> removalListener) {
            super(expireTime, unit, removalListener);
            this.checkPeriod = unit.toMillis(expireTime / 2);
        }

        public long getCheckPeriod() {
            return checkPeriod;
        }

        @NotNull
        public Map<E, Long> getCache() {
            return cache;
        }

        public long getLast() {
            return last;
        }

        public boolean isRunning() {
            return thread != null && thread.isAlive();
        }

        public boolean isOnRun() {
            return onRun;
        }

        @Override
        public @NotNull Map<E, Long> asMap() {
            return cache;
        }

        @Override
        public @Nullable Long get(@NotNull E e) {
            return cache.get(e);
        }

        @Override
        public @Nullable Long put(@NotNull E e) {
            return put(e, expireTime);
        }

        @Nullable
        public Long put(@NotNull E e, long time) {
            final long millis = System.currentTimeMillis() + time;
            final Long mapped = cache.put(e, millis);
            if (!isRunning()) {
                // Update last after check if not running
                last = Math.max(millis, last);
                // Run check after update last
                start();
            } else {
                // Update if current is more than last
                last = Math.max(millis, last);
            }
            return mapped;
        }

        @Override
        public boolean contains(Object o) {
            return cache.containsKey(o);
        }

        public boolean containsOrAdd(@NotNull E e, long time) {
            if (contains(e)) {
                return true;
            } else {
                put(e, time);
                return false;
            }
        }

        @Override
        public void clear() {
            last = -1;
            cache.clear();
            stop();
        }

        @SuppressWarnings("all")
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
            if (removalListener != null) {
                cache.entrySet().removeIf(entry -> {
                    if (entry.getValue() < time) {
                        removalListener.accept(entry.getKey());
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
    }

    public static class CaffeineCacheSet<E> extends CacheSet<E> {

        private final Cache<E, Long> cache;

        @SuppressWarnings("unchecked")
        public CaffeineCacheSet(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<E> removalListener) {
            super(expireTime, unit, removalListener);
            final Caffeine<Object, Object> builder = Caffeine.newBuilder().expireAfterWrite(expireTime, unit);
            if (removalListener != null) {
                builder.removalListener((e, millis, cause) -> removalListener.accept((E) e));
            }
            this.cache = builder.build();
        }

        @NotNull
        public Cache<E, Long> getCache() {
            return cache;
        }

        @Override
        public @NotNull Map<E, Long> asMap() {
            return cache.asMap();
        }

        @Override
        public @Nullable Long get(@NotNull E e) {
            return cache.getIfPresent(e);
        }

        @Override
        public @Nullable Long put(@NotNull E e) {
            final Long result = cache.getIfPresent(e);
            cache.put(e, System.currentTimeMillis() + expireTime);
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object o) {
            try {
                final Long result = cache.getIfPresent((E) o);
                cache.invalidate((E) o);
                return result != null;
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            try {
                return cache.getIfPresent((E) o) != null;
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public void clear() {
            cache.cleanUp();
        }
    }

    public static class GuavaCacheSet<E> extends CacheSet<E> {

        private final com.google.common.cache.Cache<E, Long> cache;

        @SuppressWarnings("unchecked")
        public GuavaCacheSet(long expireTime, @NotNull TimeUnit unit, @Nullable Consumer<E> removalListener) {
            super(expireTime, unit, removalListener);
            final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().expireAfterWrite(expireTime, unit);
            if (removalListener != null) {
                builder.removalListener(notification -> removalListener.accept((E) notification.getKey()));
            }
            this.cache = builder.build();
        }

        @NotNull
        public com.google.common.cache.Cache<E, Long> getCache() {
            return cache;
        }

        @Override
        public @NotNull Map<E, Long> asMap() {
            return cache.asMap();
        }

        @Override
        public @Nullable Long get(@NotNull E e) {
            return cache.getIfPresent(e);
        }

        @Override
        public @Nullable Long put(@NotNull E e) {
            final Long result = cache.getIfPresent(e);
            cache.put(e, System.currentTimeMillis() + expireTime);
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object o) {
            try {
                final Long result = cache.getIfPresent((E) o);
                cache.invalidate((E) o);
                return result != null;
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            try {
                return cache.getIfPresent((E) o) != null;
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public void clear() {
            cache.cleanUp();
        }
    }
}
