package com.saicone.mcode.bukkit.scheduler;

import com.saicone.mcode.scheduler.Scheduler;
import com.saicone.mcode.scheduler.TaskTimer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class BukkitScheduler implements Scheduler<BukkitTask> {

    private final Plugin plugin;

    public BukkitScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public long ticks(long duration, @NotNull TimeUnit unit) {
        return unit.toMillis(duration) * 20000 / 1000;
    }

    @Override
    public BukkitTask run(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public BukkitTask later(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks(delay, unit));
    }

    @Override
    public BukkitTask timer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, ticks(delay, unit), ticks(period, unit));
    }

    @Override
    public BukkitTask runAsync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public BukkitTask laterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticks(delay, unit));
    }

    @Override
    public BukkitTask timerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, ticks(delay, unit), ticks(period, unit));
    }

    @Override
    public void stop(BukkitTask task) {
        task.cancel();
    }

    @Override
    public BukkitTimer timer(@NotNull String id) {
        return new BukkitTimer(id);
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    public class BukkitTimer extends TaskTimer<BukkitTask> {

        private BukkitRunnable bukkitRunnable;

        public BukkitTimer(@NotNull String id) {
            super(id);
        }

        @Nullable
        public BukkitRunnable getBukkitRunnable() {
            return bukkitRunnable;
        }

        public void setBukkitRunnable(@Nullable BukkitRunnable bukkitRunnable) {
            this.bukkitRunnable = bukkitRunnable;
        }

        public void setBukkitRunnable(@NotNull Runnable runnable) {
            this.bukkitRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            };
        }

        @Override
        public boolean isRunning() {
            return super.isRunning() || !bukkitRunnable.isCancelled();
        }

        @Override
        protected BukkitTask run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
            if (period > 0) {
                setBukkitRunnable(runnable);
                return async
                        ? bukkitRunnable.runTaskTimerAsynchronously(plugin, ticks(delay, unit), ticks(period, unit))
                        : bukkitRunnable.runTaskTimer(plugin, ticks(delay, unit), ticks(period, unit));
            }
            if (delay > 0) {
                setBukkitRunnable(runnable);
                return async
                        ? bukkitRunnable.runTaskLaterAsynchronously(plugin, ticks(delay, unit))
                        : bukkitRunnable.runTaskLater(plugin, ticks(delay, unit));
            }
            return async ? runAsync(runnable) : BukkitScheduler.this.run(runnable);
        }

        @Override
        public boolean stop() {
            boolean result = super.stop();
            if (bukkitRunnable != null) {
                if (!result) {
                    result = !bukkitRunnable.isCancelled();
                }
                bukkitRunnable.cancel();
                bukkitRunnable = null;
            }
            return result;
        }

        @Override
        protected void stop(BukkitTask task) {
            BukkitScheduler.this.stop(task);
        }
    }
}
