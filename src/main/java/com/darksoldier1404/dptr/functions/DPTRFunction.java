package com.darksoldier1404.dptr.functions;

import com.darksoldier1404.dppc.api.essentials.MoneyAPI;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static com.darksoldier1404.dptr.ToolRepair.plugin;

public class DPTRFunction {
    public static void init() {
        plugin.setEnableExpCost(plugin.getConfig().getBoolean("Settings.EnableExpCost"));
        plugin.setExpCostPerDurability(plugin.getConfig().getInt("Settings.RepairExpCostPerDurability"));
        plugin.setEnableMoneyCost(plugin.getConfig().getBoolean("Settings.EnableMoneyCost"));
        plugin.setMoneyCostPerDurability(plugin.getConfig().getInt("Settings.RepairMoneyCostPerDurability"));
    }

    public static void openToolRepairGUI(Player p) {
        DInventory inv = new DInventory(plugin.getLang().get("inventory_title"), 54, plugin);
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta im = pane.getItemMeta();
        im.setDisplayName(plugin.getLang().get("item_pane_display_name"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        pane.setItemMeta(im);
        NBT.setStringTag(pane, "dppc_clickcancel", "true");
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, pane);
            }
        }
        ItemStack doRepair = new ItemStack(Material.ANVIL);
        im = doRepair.getItemMeta();
        im.setDisplayName(plugin.getLang().get("item_repair_display_name"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        doRepair.setItemMeta(im);
        NBT.setStringTag(doRepair, "dptr_repair", "true");
        inv.setItem(49, doRepair);
        refreshCost(inv);
        inv.openInventory(p);
    }

    public static List<ItemStack> getRepairItems(DInventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || NBT.hasTagKey(item, "dppc_clickcancel") || NBT.hasTagKey(item, "dptr_repair")) continue;
            items.add(item);
        }
        return items;
    }

    public static ItemStack[] getRepairItemsArray(DInventory inv) {
        List<ItemStack> items = getRepairItems(inv);
        return items.toArray(new ItemStack[0]);
    }

    public static int getTotalDurabilityToRepair(DInventory inv) {
        int totalDurabilityToRepair = 0;
        for (ItemStack repairItem : getRepairItems(inv)) {
            if (repairItem == null || !(repairItem.getItemMeta() instanceof Damageable)) continue;
            Damageable damageable = (Damageable) repairItem.getItemMeta();
            int damage = damageable.getDamage();
            totalDurabilityToRepair += damage;
        }
        return totalDurabilityToRepair;
    }

    public static void refreshCost(DInventory inv) {
        ItemStack item = inv.getItem(49);
        int totalDurabilityToRepair = getTotalDurabilityToRepair(inv);
        int totalMoneyCost = 0;
        int totalExpCost = 0;
        if (plugin.isEnableMoneyCost()) {
            totalMoneyCost = totalDurabilityToRepair * plugin.getMoneyCostPerDurability();
        }
        if (plugin.isEnableExpCost()) {
            totalExpCost = totalDurabilityToRepair * plugin.getExpCostPerDurability();
        }
        ItemMeta im = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getLang().get("item_repair_lore_space"));
        if (plugin.isEnableMoneyCost()) {
            lore.add(plugin.getLang().getWithArgs("item_repair_lore_money_cost", String.valueOf(totalMoneyCost)));
        }
        if (plugin.isEnableExpCost()) {
            lore.add(plugin.getLang().getWithArgs("item_repair_lore_exp_cost", String.valueOf(totalExpCost)));
        }
        im.setLore(lore);
        item.setItemMeta(im);
        inv.setItem(49, item);
    }

    public static void repairTools(Player p, DInventory inv) {
        int totalDurabilityToRepair = getTotalDurabilityToRepair(inv);
        int totalMoneyCost = plugin.isEnableMoneyCost() ? totalDurabilityToRepair * plugin.getMoneyCostPerDurability() : 0;
        int totalExpCost = plugin.isEnableExpCost() ? totalDurabilityToRepair * plugin.getExpCostPerDurability() : 0;

        if (plugin.isEnableMoneyCost() && !MoneyAPI.hasEnoughMoney(p, totalMoneyCost)) {
            double currentMoney = MoneyAPI.getMoney(p).doubleValue();
            p.sendMessage(plugin.getPrefix() + plugin.getLang().getWithArgs("message_not_enough_money", String.valueOf(totalMoneyCost), String.valueOf(currentMoney)));
            return;
        }
        if (plugin.isEnableExpCost() && p.getTotalExperience() < totalExpCost) {
            int currentExp = p.getTotalExperience();
            p.sendMessage(plugin.getPrefix() + plugin.getLang().getWithArgs("message_not_enough_exp", String.valueOf(totalExpCost), String.valueOf(currentExp)));
            return;
        }
        repairAndTakeRequirements(p, inv);
        refreshCost(inv);
    }

    public static boolean hasEnoughMoney(Player p, DInventory inv) {
        if (!plugin.isEnableMoneyCost()) return true;
        int totalDurabilityToRepair = getTotalDurabilityToRepair(inv);
        int totalMoneyCost = totalDurabilityToRepair * plugin.getMoneyCostPerDurability();
        return MoneyAPI.hasEnoughMoney(p, totalMoneyCost);
    }

    public static boolean hasEnoughExp(Player p, DInventory inv) {
        if (!plugin.isEnableExpCost()) return true;
        int totalDurabilityToRepair = getTotalDurabilityToRepair(inv);
        int totalExpCost = totalDurabilityToRepair * plugin.getExpCostPerDurability();
        return p.getTotalExperience() >= totalExpCost;
    }

    public static void repairAndTakeRequirements(Player p, DInventory inv) {
        int totalDurabilityToRepair = getTotalDurabilityToRepair(inv);
        int totalMoneyCost = totalDurabilityToRepair * plugin.getMoneyCostPerDurability();
        MoneyAPI.takeMoney(p, totalMoneyCost);
        int totalExpCost = totalDurabilityToRepair * plugin.getExpCostPerDurability();
        p.giveExp(-totalExpCost);
        for (ItemStack repairItem : getRepairItems(inv)) {
            if (repairItem == null) continue;
            if (!(repairItem.getItemMeta() instanceof Damageable)) continue;
            ItemMeta im = repairItem.getItemMeta();
            Damageable damageable = (Damageable) im;
            damageable.setDamage(0);
            repairItem.setItemMeta(im);
        }
        p.sendMessage(plugin.getPrefix() + plugin.getLang().get("message_repair_success"));
    }
}
