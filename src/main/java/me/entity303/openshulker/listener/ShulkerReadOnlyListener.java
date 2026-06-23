package me.entity303.openshulker.listener;

import me.entity303.openshulker.OpenShulker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class ShulkerReadOnlyListener implements Listener {
    private final OpenShulker _openShulker;

    public ShulkerReadOnlyListener(OpenShulker openShulker) {
        this._openShulker = openShulker;
    }

    @EventHandler
    public void OnShulkerInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getView().getTopInventory().getType() != InventoryType.SHULKER_BOX) return;

        Player player = (Player) event.getWhoClicked();

        if (!this._openShulker.GetShulkerActions().HasOpenShulkerBox(player)) return;

        if (player.hasPermission("openshulker.write")) return;

        event.setCancelled(true);
    }
}
