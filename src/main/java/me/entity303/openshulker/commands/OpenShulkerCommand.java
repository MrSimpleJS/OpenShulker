package me.entity303.openshulker.commands;

import me.entity303.openshulker.OpenShulker;
import me.entity303.openshulker.hooks.ChestSortHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenShulkerCommand implements TabExecutor {
    private final OpenShulker _openShulker;

    public OpenShulkerCommand(OpenShulker openShulker) {
        this._openShulker = openShulker;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!commandSender.hasPermission("openshulker.admin")) return true;

        String prefix = ChatColor.translateAlternateColorCodes('&', this._openShulker.getConfig().getString("Messages.Prefix"));

        if (args.length == 0) {
            commandSender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this._openShulker.getConfig()
                                                                                                            .getString(
                                                                                                                    "Messages.OpenShulkerCommand.Syntax")
                                                                                                            .replace("<LABEL>", label)));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            this._openShulker.InitializeConfig();
            commandSender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this._openShulker.getConfig()
                                                                                                            .getString(
                                                                                                                    "Messages.OpenShulkerCommand.Reloaded")
                                                                                                            .replace("<LABEL>", label)));
            return true;
        }

        if (args[0].equalsIgnoreCase("hooks")) {
            commandSender.sendMessage(prefix + "ChestSort hook: " + this.FormatHookStatus(this._openShulker._hookChestSort, ChestSortHook.IsLoaded()));
            commandSender.sendMessage(prefix + "WorldGuard hook: " +
                                      this.FormatHookStatus(this._openShulker._hookWorldGuard,
                                                            Bukkit.getPluginManager().getPlugin("WorldGuard") != null));
            return true;
        }

        commandSender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this._openShulker.getConfig()
                                                                                                        .getString("Messages.OpenShulkerCommand.Syntax")
                                                                                                        .replace("<LABEL>", label)));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            completions.add("hooks");
            return completions;
        }

        return new ArrayList<>();
    }

    private String FormatHookStatus(boolean enabledInConfig, boolean pluginLoaded) {
        if (!enabledInConfig) return ChatColor.RED + "disabled in config";
        if (!pluginLoaded) return ChatColor.YELLOW + "enabled in config, plugin not loaded";
        return ChatColor.GREEN + "enabled";
    }
}
