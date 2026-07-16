package me.entity303.openshulker.util;

import me.entity303.openshulker.OpenShulker;
import me.entity303.openshulker.hooks.ChestSortHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class ShulkerActions {
    private final NamespacedKey _openShulkerKey;
    private final NamespacedKey _openShulkerLocationKey;
    private final OpenShulker _openShulker;

    public ShulkerActions(OpenShulker openShulker) {
        this._openShulker = openShulker;
        this._openShulkerKey = new NamespacedKey(this._openShulker, "openshulker");
        this._openShulkerLocationKey = new NamespacedKey(this._openShulker, "openshulkerlocation");
    }

    public void SaveShulkerBox(ItemStack shulkerBoxStack, Inventory inventory, Player player) {
        BlockStateMeta blockStateMeta = (BlockStateMeta) shulkerBoxStack.getItemMeta();

        ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

        shulkerBox.getInventory().setContents(inventory.getContents());

        blockStateMeta.setBlockState(shulkerBox);

        PersistentDataContainer container = blockStateMeta.getPersistentDataContainer();

        container.remove(this._openShulkerKey);

        shulkerBoxStack.setItemMeta(blockStateMeta);

        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, null);

        container = player.getPersistentDataContainer();

        container.remove(this._openShulkerKey);

        container.remove(this._openShulkerLocationKey);

        try {
            player.playSound(player.getLocation(), Sound.valueOf(this._openShulker.getConfig().getString("CloseSound")), 1F, 1F);
        } catch (Throwable ignored) {
            //Ignore the exception, it's probably just a message about not being able to find the correct sound
            //We have an info message in OpenShulker#onEnable for this
        }
    }

    public boolean HasOpenShulkerBox(Player player) {
        ItemStack itemStack = this.SearchShulkerBox(player);

        PersistentDataContainer container = player.getPersistentDataContainer();

        if (!container.has(this._openShulkerKey, PersistentDataType.STRING)) {
            if (itemStack != null) {
                this._openShulker.getLogger()
                                 .warning("Recovered stale open shulker item state for " + player.getName() + " (" +
                                          player.getUniqueId() + ").");
                this.ClearOpenShulkerState(itemStack, player);
            }
            return false;
        }

        if (itemStack == null) {
            this._openShulker.getLogger()
                             .warning("Recovered stale open shulker player state for " + player.getName() + " (" +
                                      player.getUniqueId() + ").");
            this.ClearPlayerOpenShulkerState(player);
            return false;
        }

        return true;
    }

    public ItemStack SearchShulkerBox(Player player) {
        PersistentDataContainer dataContainer = player.getPersistentDataContainer();

        if (this.HasOpenShulkerInEnderChest(player)) return this.SearchShulkerBox(player.getEnderChest(), player);

        if (dataContainer.has(this._openShulkerLocationKey, PersistentDataType.STRING)) {
            Container container = this.GetShulkerHoldingContainer(player);

            if (container == null) return null;

            return this.SearchShulkerBox(container.getInventory(), player);
        }

        return this.SearchShulkerBox(player.getInventory());
    }

    public boolean HasOpenShulkerInEnderChest(Player player) {
        PersistentDataContainer dataContainer = player.getPersistentDataContainer();

        return dataContainer.has(this._openShulkerLocationKey, PersistentDataType.BYTE);
    }

    public ItemStack SearchShulkerBox(Inventory inventory, Player player) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) continue;

            if (itemStack.getType() == Material.AIR) continue;

            if (!this.IsOpenShulker(itemStack, player)) continue;

            return itemStack;
        }

        return null;
    }

    public Container GetShulkerHoldingContainer(Player player) {
        PersistentDataContainer dataContainer = player.getPersistentDataContainer();

        if (dataContainer.has(this._openShulkerLocationKey, PersistentDataType.BYTE)) return null;

        if (!dataContainer.has(this._openShulkerLocationKey, PersistentDataType.STRING)) return null;

        String locationString = dataContainer.get(this._openShulkerLocationKey, PersistentDataType.STRING);
        if (locationString == null) return null;

        String[] locationStringArray = locationString.split(";");
        if (locationStringArray.length != 4) return null;

        double xCoordinate;
        double yCoordinate;
        double zCoordinate;

        try {
            xCoordinate = Double.parseDouble(locationStringArray[0]);
            yCoordinate = Double.parseDouble(locationStringArray[1]);
            zCoordinate = Double.parseDouble(locationStringArray[2]);
        } catch (NumberFormatException ignored) {
            return null;
        }

        World world = Bukkit.getWorld(locationStringArray[3]);
        if (world == null) return null;

        Location location = new Location(world, xCoordinate, yCoordinate, zCoordinate);

        Block block = location.getBlock();

        if (!(block.getState() instanceof Container)) return null;

        Container container = (Container) block.getState();

        return container;
    }

    public ItemStack SearchShulkerBox(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) continue;

            if (itemStack.getType() == Material.AIR) continue;

            if (!this.IsOpenShulker(itemStack)) continue;

            return itemStack;
        }

        return null;
    }

    public boolean IsOpenShulker(ItemStack itemStack, Player player) {
        if (itemStack == null) return false;

        ItemMeta meta = itemStack.getItemMeta();

        if (!(itemStack.getItemMeta() instanceof BlockStateMeta)) return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(this._openShulkerKey, PersistentDataType.STRING)) return false;

        if (player == null) return true;

        String uniqueId = container.get(this._openShulkerKey, PersistentDataType.STRING);

        if (uniqueId == null) return false;

        if (!uniqueId.equalsIgnoreCase(player.getUniqueId().toString())) return false;

        return true;
    }

    public boolean IsOpenShulker(ItemStack itemStack) {
        return this.IsOpenShulker(itemStack, null);
    }

    public boolean IsShulkerBox(ItemStack itemStack) {
        if (itemStack == null) return false;

        if (itemStack.getType() == Material.AIR) return false;

        return itemStack.getType().name().contains(Material.SHULKER_BOX.name());
    }

    public boolean CanInputIntoShulkerBox(Player player, ItemStack shulkerBoxStack, ItemStack itemStack) {
        if (!player.hasPermission("openshulker.write")) return false;

        if (!this.IsShulkerBox(shulkerBoxStack)) return false;

        if (itemStack == null) return false;

        if (itemStack.getType() == Material.AIR) return false;

        if (this.IsShulkerBox(itemStack)) return false;

        if (shulkerBoxStack.getAmount() != 1) return false;

        if (this.IsOpenShulker(shulkerBoxStack)) return false;

        if (!(shulkerBoxStack.getItemMeta() instanceof BlockStateMeta)) return false;

        BlockStateMeta blockStateMeta = (BlockStateMeta) shulkerBoxStack.getItemMeta();

        return blockStateMeta.getBlockState() instanceof ShulkerBox;
    }

    public ItemStack InputItemIntoShulkerBox(ItemStack shulkerBoxStack, ItemStack itemStack) {
        BlockStateMeta blockStateMeta = (BlockStateMeta) shulkerBoxStack.getItemMeta();
        ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
        Inventory inventory = shulkerBox.getInventory();

        HashMap<Integer, ItemStack> leftovers = inventory.addItem(itemStack.clone());

        blockStateMeta.setBlockState(shulkerBox);
        shulkerBoxStack.setItemMeta(blockStateMeta);

        if (leftovers.isEmpty()) return null;

        for (Map.Entry<Integer, ItemStack> entry : leftovers.entrySet()) return entry.getValue();

        return null;
    }

    public boolean AttemptToOpenShulkerBox(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        return this.AttemptToOpenShulkerBox(player, itemStack);
    }

    public boolean AttemptToOpenShulkerBox(Player player, ItemStack itemStack) {
        if (!player.hasPermission("openshulker.use")) return false;

        if (itemStack == null) return false;

        if (itemStack.getAmount() <= 0) return false;

        if (itemStack.getAmount() > 1) return false;

        if (!itemStack.getType().name().contains(Material.SHULKER_BOX.name())) return false;

        if (!(itemStack.getItemMeta() instanceof BlockStateMeta)) return false;

        BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();

        if (!(blockStateMeta.getBlockState() instanceof ShulkerBox)) return false;

        ShulkerBox shulker = (ShulkerBox) blockStateMeta.getBlockState();

        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(this._openShulkerKey, PersistentDataType.STRING)) return false;

        container.set(this._openShulkerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        itemStack.setItemMeta(meta);

        container = player.getPersistentDataContainer();

        container.set(this._openShulkerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        String inventoryTitle = meta.hasDisplayName() ? meta.getDisplayName() : null;
        Inventory inventory = inventoryTitle == null ? Bukkit.createInventory(null, InventoryType.SHULKER_BOX) : Bukkit.createInventory(null, InventoryType.SHULKER_BOX, inventoryTitle);

        if (this._openShulker._hookChestSort) ChestSortHook.SetUnsortable(inventory, this._openShulker._debugChestSort);

        SchedulerUtil.RunEntityLater(this._openShulker, player, () -> {
            if (!player.isOnline()) {
                this.ClearOpenShulkerState(itemStack, player);
                return;
            }

            ItemStack currentOpenShulker = this.SearchShulkerBox(player);

            if (currentOpenShulker == null || !this.IsOpenShulker(currentOpenShulker, player)) {
                this.ClearOpenShulkerState(itemStack, player);
                return;
            }

            if (currentOpenShulker != itemStack && !currentOpenShulker.equals(itemStack)) {
                this.ClearOpenShulkerState(itemStack, player);
                return;
            }

            inventory.setContents(shulker.getInventory().getContents());

            player.openInventory(inventory);
            if (this._openShulker._hookChestSort) {
                ChestSortHook.SetUnsortable(player.getOpenInventory().getTopInventory(), this._openShulker._debugChestSort);
            }
        }, this._openShulker.getConfig().getLong("WaitSecondsBeforeOpen", 0) * 20);

        try {
            player.playSound(player.getLocation(), Sound.valueOf(this._openShulker.getConfig().getString("OpenSound")), 1F, 1F);
        } catch (Throwable ignored) {
            //Ignore the exception, it's probably just a message about not being able to find the correct sound
            //We have an info message in OpenShulker#onEnable for this
        }
        return true;
    }

    private void ClearOpenShulkerState(ItemStack itemStack, Player player) {
        if (itemStack != null && itemStack.getItemMeta() instanceof BlockStateMeta) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().remove(this._openShulkerKey);
            itemStack.setItemMeta(meta);
        }

        this.ClearPlayerOpenShulkerState(player);
    }

    private void ClearPlayerOpenShulkerState(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        container.remove(this._openShulkerKey);
        container.remove(this._openShulkerLocationKey);
    }

    public boolean AttemptToOpenShulkerBox(Player player, ItemStack itemStack, Location chest) {
        if (chest == null || chest.getWorld() == null) return false;

        Block block = chest.getBlock();

        if (!(block.getState() instanceof Container)) return false;

        boolean open = this.AttemptToOpenShulkerBox(player, itemStack);

        if (!open) return false;

        PersistentDataContainer container = player.getPersistentDataContainer();

        container.set(this._openShulkerLocationKey, PersistentDataType.STRING,
                      chest.getX() + ";" + chest.getY() + ";" + chest.getZ() + ";" + chest.getWorld().getName());
        return true;
    }

    public boolean AttemptToOpenShulkerBox(Player player, ItemStack itemStack, boolean enderChest) {
        boolean open = this.AttemptToOpenShulkerBox(player, itemStack);

        if (!open) return false;

        if (!enderChest) return true;

        PersistentDataContainer container = player.getPersistentDataContainer();

        container.set(this._openShulkerLocationKey, PersistentDataType.BYTE, (byte) 1);
        return true;
    }
}
