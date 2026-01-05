package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {

    private final PlayerBirthdayPerks plugin;
    private final Map<UUID, AbstractGui> openGuis;

    public GuiManager(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGui(Player player, AbstractGui gui) {
        openGuis.put(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory());
    }

    public void closeGui(Player player) {
        openGuis.remove(player.getUniqueId());
        player.closeInventory();
    }

    public AbstractGui getOpenGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    public boolean hasOpenGui(Player player) {
        return openGuis.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        AbstractGui gui = openGuis.get(player.getUniqueId());
        if (gui == null) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !clickedInventory.equals(gui.getInventory())) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        gui.handleClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        AbstractGui gui = openGuis.remove(player.getUniqueId());
        if (gui != null) {
            gui.onClose(player);
        }
    }

    public PlayerBirthdayPerks getPlugin() {
        return plugin;
    }
}
