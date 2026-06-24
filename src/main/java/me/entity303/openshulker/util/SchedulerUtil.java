package me.entity303.openshulker.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class SchedulerUtil {
    private SchedulerUtil() {
    }

    public static void RunEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            if (TryRunEntity(plugin, entity, task)) return;

            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }

        if (TryRunEntityLater(plugin, entity, task, delayTicks)) return;

        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    private static boolean TryRunEntity(Plugin plugin, Entity entity, Runnable task) {
        try {
            Object scheduler = GetEntityScheduler(entity);
            Method run = scheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class);

            Consumer<Object> scheduledTask = ignored -> task.run();
            Runnable retired = () -> {
            };

            run.invoke(scheduler, plugin, scheduledTask, retired);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Failed to schedule entity task, falling back to Bukkit scheduler: " + throwable.getMessage());
            return false;
        }
    }

    private static boolean TryRunEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        try {
            Object scheduler = GetEntityScheduler(entity);
            Method runDelayed = scheduler.getClass()
                                         .getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);

            Consumer<Object> scheduledTask = ignored -> task.run();
            Runnable retired = () -> {
            };

            runDelayed.invoke(scheduler, plugin, scheduledTask, retired, delayTicks);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Failed to schedule entity task, falling back to Bukkit scheduler: " + throwable.getMessage());
            return false;
        }
    }

    private static Object GetEntityScheduler(Entity entity) throws Exception {
        Method getScheduler = entity.getClass().getMethod("getScheduler");
        return getScheduler.invoke(entity);
    }
}
