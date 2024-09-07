package com.saicone.mcode.folia.scheduler;

import com.saicone.mcode.scheduler.Scheduler;
import com.saicone.mcode.scheduler.TaskTimer;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements Scheduler<ScheduledTask> {

    private final Plugin plugin;

    public FoliaScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public long ticks(long duration, @NotNull TimeUnit unit) {
        return (long) (unit.toMillis(duration) * 0.02);
    }

    @Override
    public ScheduledTask run(@NotNull Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().run(plugin, task -> runnable.run());
    }

    @Override
    public ScheduledTask later(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), ticks(delay, unit));
    }

    @Override
    public ScheduledTask timer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), ticks(delay, unit), ticks(period, unit));
    }

    @Override
    public ScheduledTask runAsync(@NotNull Runnable runnable) {
        return Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
    }

    @Override
    public ScheduledTask laterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runDelayed(plugin, task -> runnable.run(), delay, unit);
    }

    @Override
    public ScheduledTask timerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay, period, unit);
    }

    @Override
    public ScheduledTask runBy(@NotNull Object provider, @NotNull Runnable runnable) {
        if (provider instanceof Location location) {
            return Bukkit.getRegionScheduler().run(plugin, location, task -> runnable.run());
        } else if (provider instanceof Entity entity) {
            return entity.getScheduler().run(plugin, task -> runnable.run(), null);
        } else {
            return Scheduler.super.runBy(provider, runnable);
        }
    }

    @Override
    public ScheduledTask laterBy(@NotNull Object provider, @NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        if (provider instanceof Location location) {
            return Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> runnable.run(), ticks(delay, unit));
        } else if (provider instanceof Entity entity) {
            return entity.getScheduler().runDelayed(plugin, task -> runnable.run(), null, ticks(delay, unit));
        } else {
            return Scheduler.super.laterBy(provider, runnable, delay, unit);
        }
    }

    @Override
    public ScheduledTask timerBy(@NotNull Object provider, @NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        if (provider instanceof Location location) {
            return Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, task -> runnable.run(), ticks(delay, unit), ticks(period, unit));
        } else if (provider instanceof Entity entity) {
            return entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null, ticks(delay, unit), ticks(period, unit));
        } else {
            return Scheduler.super.timerBy(provider, runnable, delay, period, unit);
        }
    }

    @Override
    public void stop(ScheduledTask task) {
        task.cancel();
    }

    @Override
    public TaskTimer<ScheduledTask> timer(@NotNull String id) {
        return new TaskTimer<>(id) {
            @Override
            protected ScheduledTask run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
                if (period > 0) {
                    return async ? timerAsync(runnable, delay, period, unit) : timer(runnable, delay, period, unit);
                }
                if (delay > 0) {
                    return async ? laterAsync(runnable, delay, unit) : later(runnable, delay, unit);
                }
                return async ? runAsync(runnable) : FoliaScheduler.this.run(runnable);
            }

            @Override
            protected ScheduledTask run(@NotNull Object provider, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
                if (period > 0) {
                    return timerBy(provider, runnable, delay, period, unit);
                }
                if (delay > 0) {
                    return laterBy(provider, runnable, delay, unit);
                }
                return runBy(provider, runnable);
            }

            @Override
            protected void stop(ScheduledTask task) {
                task.cancel();
            }
        };
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }
}
