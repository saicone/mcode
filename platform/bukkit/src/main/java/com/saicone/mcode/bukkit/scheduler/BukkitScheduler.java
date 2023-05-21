package com.saicone.mcode.bukkit.scheduler;

import com.saicone.mcode.scheduler.Scheduler;
import com.saicone.mcode.scheduler.TaskTimer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class BukkitScheduler implements Scheduler<Integer> {

    private final Plugin plugin;

    public BukkitScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public long ticks(long duration, @NotNull TimeUnit unit) {
        return unit.toMillis(duration) * 20000 / 1000;
    }

    @Override
    public Integer sync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable).getTaskId();
    }

    @Override
    public Integer syncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks(delay, unit)).getTaskId();
    }

    @Override
    public Integer syncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, ticks(delay, unit), ticks(period, unit)).getTaskId();
    }

    @Override
    public Integer async(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable).getTaskId();
    }

    @Override
    public Integer asyncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticks(delay, unit)).getTaskId();
    }

    @Override
    public Integer asyncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, ticks(delay, unit), ticks(period, unit)).getTaskId();
    }

    @Override
    public void stop(Integer id) {
        Bukkit.getScheduler().cancelTask(id);
    }

    @Override
    public BukkitTimer timer(@NotNull String id) {
        return new BukkitTimer(id);
    }

    public class BukkitTimer extends TaskTimer<Integer> {

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
        protected Integer run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
            if (period > 0) {
                setBukkitRunnable(runnable);
                return async
                        ? bukkitRunnable.runTaskTimerAsynchronously(plugin, ticks(delay, unit), ticks(period, unit)).getTaskId()
                        : bukkitRunnable.runTaskTimer(plugin, ticks(delay, unit), ticks(period, unit)).getTaskId();
            }
            if (delay > 0) {
                setBukkitRunnable(runnable);
                return async
                        ? bukkitRunnable.runTaskLaterAsynchronously(plugin, ticks(delay, unit)).getTaskId()
                        : bukkitRunnable.runTaskLater(plugin, ticks(delay, unit)).getTaskId();
            }
            return async ? async(runnable) : sync(runnable);
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
        protected void stop(Integer id) {
            BukkitScheduler.this.stop(id);
        }
    }
}
