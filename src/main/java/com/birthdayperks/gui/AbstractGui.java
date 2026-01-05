package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractGui {

    protected final PlayerBirthdayPerks plugin;
    protected final Player player;
    protected final Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers;

    public AbstractGui(PlayerBirthdayPerks plugin, Player player, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, rows * 9, ColorUtil.colorize(title));
        this.clickHandlers = new HashMap<>();
    }

    public abstract void initialize();

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Consumer<InventoryClickEvent> handler = clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(event);
        }
    }

    public void onClose(Player player) {
        // 可被子类重写
    }

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickHandler) {
        inventory.setItem(slot, item);
        if (clickHandler != null) {
            clickHandlers.put(slot, clickHandler);
        }
    }

    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(name));
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ColorUtil.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }

    protected void fillBorder(Material material) {
        ItemStack filler = createItem(material, " ");
        int rows = inventory.getSize() / 9;
        
        for (int i = 0; i < 9; i++) {
            setItem(i, filler);
            setItem((rows - 1) * 9 + i, filler);
        }
        
        for (int i = 1; i < rows - 1; i++) {
            setItem(i * 9, filler);
            setItem(i * 9 + 8, filler);
        }
    }

    protected void fillEmpty(Material material) {
        ItemStack filler = createItem(material, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                setItem(i, filler);
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }
}
