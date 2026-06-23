package me.entity303.openshulker.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class WorldGuardHook {
    private WorldGuardHook() {
    }

    public static boolean CanBuild(Player player, Location location) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return true;
        if (location == null || location.getWorld() == null) return true;

        try {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(location.getWorld()))) {
                return true;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BUILD);
        } catch (Throwable ignored) {
            return true;
        }
    }
}
