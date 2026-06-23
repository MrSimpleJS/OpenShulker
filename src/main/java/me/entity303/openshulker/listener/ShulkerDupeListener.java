package me.entity303.openshulker.listener;

import me.entity303.openshulker.OpenShulker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShulkerDupeListener implements Listener {
    private final OpenShulker _openShulker;

    public ShulkerDupeListener(OpenShulker openShulker) {
        this._openShulker = openShulker;
    }

    @EventHandler(ignoreCancelled = true)
    public void OnBlockPlace(BlockPlaceEvent event) {
        this.HandleEvent(event, event.getPlayer());
    }

    private boolean HandleEvent(Cancellable cancellableEvent, Player player) {
        if (player.getOpenInventory().getType() != InventoryType.SHULKER_BOX) return false;

        if (!this._openShulker.GetShulkerActions().HasOpenShulkerBox(player)) return false;

        cancellableEvent.setCancelled(true);

        player.openInventory(player.getOpenInventory());
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void OnBlockBreak(BlockBreakEvent event) {
        this.HandleEvent(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void OnInteractAtEntity(PlayerInteractAtEntityEvent event) {
        this.HandleEvent(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void OnInteractEntity(PlayerInteractEntityEvent event) {
        this.HandleEvent(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void OnCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (!this.HandleEvent(event, (Player) event.getWhoClicked())) return;

        event.setResult(Event.Result.DENY);
    }

    @EventHandler(ignoreCancelled = true)
    public void OnContainerBreak(BlockBreakEvent event) {
        this.HandleContainerBreak(event, event.getPlayer());
    }

    private void HandleContainerBreak(BlockEvent event, CommandSender player) {
        if (!(event instanceof Cancellable)) return;

        Cancellable cancellable = (Cancellable) event;

        if (!(event.getBlock().getState() instanceof Container)) return;

        Container container = (Container) event.getBlock().getState();

        ItemStack shulkerBox = this._openShulker.GetShulkerActions().SearchShulkerBox(container.getInventory());

        if (shulkerBox == null) return;

        cancellable.setCancelled(true);

        String prefix = ChatColor.translateAlternateColorCodes('&', this._openShulker.getConfig().getString("Messages.Prefix"));

        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this._openShulker.getConfig().getString("Messages.CannotBreakContainer")));
    }

    @EventHandler(ignoreCancelled = true)
    public void OnContainerBreak(BlockDropItemEvent event) {
        this.HandleContainerBreak(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void OnContainerBreak(BlockExplodeEvent event) {
        this.RemoveContainersWithOpenShulkers(event.blockList());
    }

    private void RemoveContainersWithOpenShulkers(List<Block> blockList) {
        for (Block block : new ArrayList<>(blockList)) {
            if (!(block.getState() instanceof Container)) continue;

            Container container = (Container) block.getState();

            ItemStack shulkerBox = this._openShulker.GetShulkerActions().SearchShulkerBox(container.getInventory());

            if (shulkerBox == null) continue;

            blockList.remove(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void OnContainerBreak(EntityExplodeEvent event) {
        this.RemoveContainersWithOpenShulkers(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void OnDupedShulkerPlace(PlayerInteractEvent event) {
        ItemStack itemStack = event.getHand() != null ? event.getPlayer().getInventory().getItem(event.getHand()) : event.getItem();

        if (itemStack == null) return;

        if (!this._openShulker.GetShulkerActions().IsOpenShulker(itemStack)) return;

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        int slot = event.getSlot();

        if (event.getClickedInventory() == null) return;

        ItemStack clickedItemStack = event.getClickedInventory().getItem(slot);

        if (clickedItemStack == null) return;

        if (clickedItemStack.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();

        if (!this._openShulker.GetShulkerActions().HasOpenShulkerBox(player)) return;

        //Make operator can modify shulkerbox's NBT to reset it, which can solve some strange bugs caused by PersistentDataContainer
        //Like player has closed shulkerbox but the dataContainer still contains player's UUID so that player can't interact with the shulkerbox
        if (!this._openShulker.GetShulkerActions().IsOpenShulker(clickedItemStack) || player.isOp())
            return;

        if (event.isRightClick() && event.isShiftClick()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void OnInventoryClick2(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        int slot = event.getSlot();

        if (event.getClickedInventory() == null) return;

        ItemStack clickedItemStack = event.getClickedInventory().getItem(slot);

        if (clickedItemStack == null) return;

        if (clickedItemStack.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();

        if (this._openShulker.GetShulkerActions().HasOpenShulkerBox(player)) return;

        if (!this._openShulker.GetShulkerActions().IsOpenShulker(clickedItemStack)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void OnItemMove(InventoryMoveItemEvent event) {
        if (!this._openShulker.GetShulkerActions().IsOpenShulker(event.getItem())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void OnBlockDispense(BlockDispenseEvent event) {
        if (!this._openShulker.GetShulkerActions().IsOpenShulker(event.getItem())) return;

        event.setCancelled(true);
    }
}
