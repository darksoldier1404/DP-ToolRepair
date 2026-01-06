package com.darksoldier1404.dptr.events;

import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.events.dinventory.DInventoryClickEvent;
import com.darksoldier1404.dppc.events.dinventory.DInventoryCloseEvent;
import com.darksoldier1404.dppc.utils.NBT;
import com.darksoldier1404.dptr.functions.DPTRFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static com.darksoldier1404.dptr.ToolRepair.plugin;

public class DPTREvent implements Listener {
    @EventHandler
    public void onInventoryClick(DInventoryClickEvent e) {
        DInventory inv = e.getDInventory();
        Player p = (Player) e.getWhoClicked();
        if (inv.isValidHandler(plugin)) {
            ItemStack item = e.getCurrentItem();
            if (NBT.hasTagKey(item, "dptr_repair")) {
                e.setCancelled(true);
                DPTRFunction.repairTools(p, inv);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> DPTRFunction.refreshCost(inv), 1L);
        }
    }

    @EventHandler
    public void onInventoryClose(DInventoryCloseEvent e) {
        DInventory inv = e.getDInventory();
        if (inv.isValidHandler(plugin)) {
            Player p = (Player) e.getPlayer();
            ItemStack[] items = DPTRFunction.getRepairItemsArray(inv);
            if (items == null || items.length == 0) return;
            HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(items);
            for (ItemStack item : leftover.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), item);
            }
        }
    }
}
