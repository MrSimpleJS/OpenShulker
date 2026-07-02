package me.entity303.openshulker.hooks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ChestSortHook {
    private static final String[] API_CLASSES = {
            "de.jeff_media.chestsort.api.ChestSortAPI",
            "de.jeff_media.chestsort.ChestSortAPI"
    };
    private static boolean _warnedMissingApi = false;

    private ChestSortHook() {
    }

    public static void SetUnsortable(Inventory inventory, boolean debug) {
        if (!IsLoaded()) return;
        if (inventory == null) return;

        for (String apiClass : API_CLASSES) {
            if (TrySetUnsortable(apiClass, inventory)) return;
        }

        if (debug && !_warnedMissingApi) {
            Bukkit.getLogger().warning("[OpenShulker] ChestSort is loaded, but no supported ChestSortAPI#setUnsortable method was found.");
            _warnedMissingApi = true;
        }
    }

    public static boolean IsLoaded() {
        return Bukkit.getPluginManager().getPlugin("ChestSort") != null;
    }

    public static boolean IsApiAvailable() {
        if (!IsLoaded()) return false;

        for (String apiClass : API_CLASSES) {
            try {
                Class<?> chestSortApi = Class.forName(apiClass);
                if (FindUnsortableMethod(chestSortApi) != null) return true;
            } catch (Throwable ignored) {
            }
        }

        return false;
    }

    private static boolean TrySetUnsortable(String apiClass, Inventory inventory) {
        try {
            Class<?> chestSortApi = Class.forName(apiClass);

            try {
                Method setUnsortable = chestSortApi.getMethod("setUnsortable", Inventory.class);
                setUnsortable.invoke(null, inventory);
                return true;
            } catch (NoSuchMethodException ignored) {
                return TryMatchingUnsortableMethod(chestSortApi, inventory);
            }
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean TryMatchingUnsortableMethod(Class<?> chestSortApi, Inventory inventory) {
        Method method = FindUnsortableMethod(chestSortApi);

        if (method == null) return false;

        try {
            method.invoke(null, inventory);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Method FindUnsortableMethod(Class<?> chestSortApi) {
        for (Method method : chestSortApi.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (!method.getName().toLowerCase().contains("unsort")) continue;
            if (method.getParameterTypes().length != 1) continue;
            if (!method.getParameterTypes()[0].isAssignableFrom(Inventory.class)) continue;

            return method;
        }

        return null;
    }
}
