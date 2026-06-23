package me.entity303.openshulker.hooks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Method;

public final class ChestSortHook {
    private ChestSortHook() {
    }

    public static void SetUnsortable(Inventory inventory) {
        if (Bukkit.getPluginManager().getPlugin("ChestSort") == null) return;

        try {
            Class<?> chestSortApi = Class.forName("de.jeff_media.chestsort.api.ChestSortAPI");
            Method setUnsortable = chestSortApi.getMethod("setUnsortable", Inventory.class);
            setUnsortable.invoke(null, inventory);
        } catch (Throwable ignored) {
            // ChestSort is optional and older builds may not expose the API.
        }
    }
}
