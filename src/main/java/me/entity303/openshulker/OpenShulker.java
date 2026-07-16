package me.entity303.openshulker;

import me.entity303.openshulker.commands.OpenShulkerCommand;
import me.entity303.openshulker.hooks.ChestSortHook;
import me.entity303.openshulker.listener.ShulkerDupeListener;
import me.entity303.openshulker.listener.ShulkerOpenCloseListener;
import me.entity303.openshulker.listener.ShulkerReadOnlyListener;
import me.entity303.openshulker.util.ShulkerActions;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class OpenShulker extends JavaPlugin implements Listener {
    public boolean _allowInventoryOpen = true;
    public boolean _allowContainerOpen = true;
    public boolean _allowEnderChestOpen = true;
    public boolean _allowHandOpen = true;
    public boolean _hookChestSort = true;
    public boolean _debugChestSort = false;
    public boolean _hookWorldGuard = true;
    private ShulkerActions _shulkerActions;

    @Override
    public void onDisable() {
        if (this._shulkerActions == null) return;

        for (Player all : Bukkit.getOnlinePlayers()) {
            if (!this._shulkerActions.HasOpenShulkerBox(all)) continue;

            ItemStack shulkerBox = this._shulkerActions.SearchShulkerBox(all);

            if (shulkerBox == null) continue;

            this._shulkerActions.SaveShulkerBox(shulkerBox, all.getOpenInventory().getTopInventory(), all);

            all.closeInventory();
        }
    }

    @Override
    public void onEnable() {
        this.InitializeConfig();

        this._shulkerActions = new ShulkerActions(this);

        Bukkit.getPluginManager().registerEvents(new ShulkerOpenCloseListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ShulkerDupeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ShulkerReadOnlyListener(this), this);

        OpenShulkerCommand openShulkerCommand = new OpenShulkerCommand(this);

        PluginCommand command = this.getCommand("openshulker");

        command.setExecutor(openShulkerCommand);
        command.setTabCompleter(openShulkerCommand);

        this.LogHookStatus();
    }

    public void InitializeConfig() {
        this.saveDefaultConfig();

        this.reloadConfig();

        this.AddConfigDefaults();

        String openSound = this.getConfig().getString("OpenSound");

        try {
            Sound.valueOf(openSound);
        } catch (Throwable ignored) {
            if (openSound == null) {
                Bukkit.getLogger().severe("You did not specify OpenSound, using default");
                this.getConfig().set("OpenSound", "BLOCK_SHULKER_BOX_OPEN");
                this.saveConfig();
                this.reloadConfig();
            } else Bukkit.getLogger()
                         .severe("There is no sound called '" + openSound +
                                 "', for a list of sounds, visit https://www.spigotmc.org/wiki/cc-sounds-list/");
        }

        String closeSound = this.getConfig().getString("CloseSound");

        try {
            Sound.valueOf(closeSound);
        } catch (Throwable ignored) {
            if (closeSound == null) {
                Bukkit.getLogger().severe("You did not specify CloseSound, using default");
                this.getConfig().set("CloseSound", "BLOCK_SHULKER_BOX_CLOSE");
                this.saveConfig();
                this.reloadConfig();
            } else Bukkit.getLogger()
                         .severe("There is no sound called '" + closeSound +
                                 "', for a list of sounds, visit https://www.spigotmc.org/wiki/cc-sounds-list/");
        }

        this._allowInventoryOpen = this.getConfig().getBoolean("OpenMethods.AllowInventoryOpen");
        this._allowContainerOpen = this.getConfig().getBoolean("OpenMethods.AllowContainerOpen");
        this._allowEnderChestOpen = this.getConfig().getBoolean("OpenMethods.AllowEnderChestOpen");
        this._allowHandOpen = this.getConfig().getBoolean("OpenMethods.AllowHandOpen");
        this._hookChestSort = this.getConfig().getBoolean("Hooks.ChestSort", true);
        this._debugChestSort = this.getConfig().getBoolean("Hooks.ChestSortDebug", false);
        this._hookWorldGuard = this.getConfig().getBoolean("Hooks.WorldGuard", true);
    }

    private void AddConfigDefaults() {
        boolean changed = false;

        changed |= this.AddConfigDefault("OpenMethods.AllowInventoryOpen", true);
        changed |= this.AddConfigDefault("OpenMethods.AllowContainerOpen", true);
        changed |= this.AddConfigDefault("OpenMethods.AllowEnderChestOpen", true);
        changed |= this.AddConfigDefault("OpenMethods.AllowHandOpen", true);
        changed |= this.AddConfigDefault("WaitSecondsBeforeOpen", 0);
        changed |= this.AddConfigDefault("OpenSound", "BLOCK_SHULKER_BOX_OPEN");
        changed |= this.AddConfigDefault("CloseSound", "BLOCK_SHULKER_BOX_CLOSE");
        changed |= this.AddConfigDefault("Hooks.ChestSort", true);
        changed |= this.AddConfigDefault("Hooks.ChestSortDebug", false);
        changed |= this.AddConfigDefault("Hooks.WorldGuard", true);
        changed |= this.AddConfigDefault("Messages.Prefix", "&8[&2OpenShulker&8] &7");
        changed |= this.AddConfigDefault("Messages.CannotBreakContainer", "&cYou cannot break this container, since there's an opened shulker in it");
        changed |= this.AddConfigDefault("Messages.OpenShulkerCommand.Syntax", "&cSyntax: &4/<LABEL> <Reload|Hooks>");
        changed |= this.AddConfigDefault("Messages.OpenShulkerCommand.Reloaded", "The plugin was reloaded!");

        if (!changed) return;

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();
    }

    private boolean AddConfigDefault(String path, Object value) {
        boolean missing = !this.getConfig().isSet(path);
        this.getConfig().addDefault(path, value);
        return missing;
    }

    private void LogHookStatus() {
        this.getLogger().info("ChestSort hook: " + this.FormatHookStatus(this._hookChestSort, ChestSortHook.IsLoaded()));
        this.getLogger().info("WorldGuard hook: " +
                              this.FormatHookStatus(this._hookWorldGuard, Bukkit.getPluginManager().getPlugin("WorldGuard") != null));

        if (this._hookChestSort && this._debugChestSort && ChestSortHook.IsLoaded() && !ChestSortHook.IsApiAvailable()) {
            this.getLogger().warning("ChestSort is loaded, but OpenShulker could not find a supported ChestSortAPI#setUnsortable method.");
        }
    }

    private String FormatHookStatus(boolean enabledInConfig, boolean pluginLoaded) {
        if (!enabledInConfig) return "disabled in config";
        if (!pluginLoaded) return "enabled in config, plugin not loaded";
        return "enabled and plugin loaded";
    }

    public ShulkerActions GetShulkerActions() {
        return this._shulkerActions;
    }
}
