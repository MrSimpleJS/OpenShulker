package me.entity303.openshulker.listener;

import me.entity303.openshulker.OpenShulker;
import me.entity303.openshulker.hooks.WorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShulkerOpenCloseListener implements Listener {
    private final OpenShulker _openShulker;

    public ShulkerOpenCloseListener(OpenShulker openShulker) {
        this._openShulker = openShulker;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnShulkerOpen(PlayerInteractEvent event) {
        if (!this._openShulker._allowHandOpen) return;

        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        if (!event.getPlayer().isSneaking()) return;

        //Don't open shulkerbox when interact with hopper
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.HOPPER) return;

        if (this._openShulker.GetShulkerActions().AttemptToOpenShulkerBox(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void OnShulkerOpenAlternative(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!player.hasPermission("openshulker.use")) return;

        if (event.getClickedInventory() == null) return;

        int clickedSlot = event.getSlot();

        ItemStack clickedItemStack = event.getClickedInventory().getItem(clickedSlot);

        if (clickedItemStack == null) return;

        if (clickedItemStack.getType() == Material.AIR) return;

        if (this.HandleShulkerItemInput(event, player, clickedItemStack, clickedSlot)) return;

        if (!clickedItemStack.getType().name().contains(Material.SHULKER_BOX.name())) return;

        if (!event.isRightClick()) return;

        if (!event.isShiftClick()) return;

        if (event.getClickedInventory() == event.getWhoClicked().getInventory()) {
            if (!this._openShulker._allowInventoryOpen) return;

            if (event.getView().getTopInventory().getType() == InventoryType.SHULKER_BOX) {
                if (this._openShulker.GetShulkerActions().HasOpenShulkerBox(player)) {
                    ItemStack shulkerBox = this._openShulker.GetShulkerActions().SearchShulkerBox(player);

                    this._openShulker.GetShulkerActions().SaveShulkerBox(shulkerBox, event.getView().getTopInventory(), player);
                }

                //Close inventory to prevent overriding open shulker contents
                event.getWhoClicked().closeInventory();
            }

            boolean open = this._openShulker.GetShulkerActions().AttemptToOpenShulkerBox(player, clickedItemStack);

            if (!open) return;
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) {
            if (!this._openShulker._allowEnderChestOpen) return;
            if (!this.IsOwnerOfEnderChest(player, event.getClickedInventory(), clickedItemStack, clickedSlot)) return;

            boolean open = this._openShulker.GetShulkerActions().AttemptToOpenShulkerBox(player, clickedItemStack, true);

            if (!open) return;
            event.setCancelled(true);
            return;
        }

        if (!this._openShulker._allowContainerOpen) return;

        Location location = event.getClickedInventory().getLocation();

        if (location == null) return;

        if (!this.CanUseInventoryLocation(player, location)) return;

        boolean open = this._openShulker.GetShulkerActions().AttemptToOpenShulkerBox(player, clickedItemStack, location);

        if (!open) return;
        event.setCancelled(true);
    }

    private boolean HandleShulkerItemInput(InventoryClickEvent event, Player player, ItemStack clickedItemStack, int clickedSlot) {
        if (!event.isRightClick()) return false;

        ItemStack cursorItemStack = event.getCursor();

        if (cursorItemStack == null) return false;

        if (cursorItemStack.getType() == Material.AIR) return false;

        Location location = event.getClickedInventory().getLocation();

        if (!this.CanUseInventoryLocation(player, location)) return false;

        if (this._openShulker.GetShulkerActions().CanInputIntoShulkerBox(player, clickedItemStack, cursorItemStack)) {
            ItemStack leftover = this._openShulker.GetShulkerActions().InputItemIntoShulkerBox(clickedItemStack, cursorItemStack);

            event.getClickedInventory().setItem(clickedSlot, clickedItemStack);
            event.setCursor(leftover);
            event.setCancelled(true);
            return true;
        }

        if (this._openShulker.GetShulkerActions().CanInputIntoShulkerBox(player, cursorItemStack, clickedItemStack)) {
            ItemStack leftover = this._openShulker.GetShulkerActions().InputItemIntoShulkerBox(cursorItemStack, clickedItemStack);

            event.setCursor(cursorItemStack);
            event.getClickedInventory().setItem(clickedSlot, leftover);
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    private boolean CanUseInventoryLocation(Player player, Location location) {
        if (!this._openShulker._hookWorldGuard) return true;

        if (location == null) return true;

        try {
            return WorldGuardHook.CanBuild(player, location);
        } catch (Throwable ignored) {
            return true;
        }
    }

    private boolean IsOwnerOfEnderChest(Player player, Inventory clickedInventory, ItemStack clickedItemStack, int clickedSlot) {
        if (!player.getEnderChest().equals(clickedInventory)) return false;

        ItemStack potentiallyClickedItem = player.getEnderChest().getItem(clickedSlot);

        if (potentiallyClickedItem == null) return false;

        return potentiallyClickedItem == clickedItemStack || potentiallyClickedItem.equals(clickedItemStack);
    }

    @EventHandler(ignoreCancelled = true)
    public void OnShulkerItemDrop(PlayerDropItemEvent event) {
        if (!this._openShulker.GetShulkerActions().HasOpenShulkerBox(event.getPlayer())) return;

        ItemStack shulkerBox = event.getItemDrop().getItemStack();

        if (!this._openShulker.GetShulkerActions().IsOpenShulker(shulkerBox, event.getPlayer())) return;

        boolean enderChest = this._openShulker.GetShulkerActions().HasOpenShulkerInEnderChest(event.getPlayer());

        Container container = this._openShulker.GetShulkerActions().GetShulkerHoldingContainer(event.getPlayer());

        this._openShulker.GetShulkerActions().SaveShulkerBox(shulkerBox, event.getPlayer().getOpenInventory().getTopInventory(), event.getPlayer());

        this.ReopenInventory(enderChest, container, event.getPlayer());
    }

    private void ReopenInventory(boolean enderChest, Container container, HumanEntity player) {
        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(this._openShulker, () -> {
            if (container != null) {
                if (container.getWorld() != player.getWorld()) return;

                if (container.getLocation().distance(player.getLocation()) > 4) return;

                player.openInventory(container.getInventory());
                return;
            }

            if (!enderChest) return;

            player.openInventory(player.getEnderChest());
        }, 1L);
    }

    @EventHandler
    public void OnShulkerInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        if (event.getInventory().getType() != InventoryType.SHULKER_BOX) return;

        Player player = (Player) event.getPlayer();

        if (!this._openShulker.GetShulkerActions().HasOpenShulkerBox(player)) return;

        ItemStack itemStack = this._openShulker.GetShulkerActions().SearchShulkerBox(player);

        if (itemStack == null) return;

        boolean enderChest = this._openShulker.GetShulkerActions().HasOpenShulkerInEnderChest(player);

        Container container = this._openShulker.GetShulkerActions().GetShulkerHoldingContainer(player);

        this._openShulker.GetShulkerActions().SaveShulkerBox(itemStack, event.getInventory(), player);

        this.ReopenInventory(enderChest, container, event.getPlayer());
    }
}
