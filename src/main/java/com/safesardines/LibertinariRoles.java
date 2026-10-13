package com.safesardines;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class LibertinariRoles {
    public static Role LIBERTINARI;

    public static void register() {
        LIBERTINARI = WatheRoles.registerRole(new Role(Identifier.of("wathedeluxe", "libertinari"), 0x8B1A1A, false, true, Role.MoodType.FAKE, -1, true));
    }

    public static boolean isLibertinari(PlayerEntity player) {
        return LIBERTINARI != null && GameWorldComponent.KEY.get(player.getWorld()).isRole(player, LIBERTINARI);
    }

    public static boolean isShopHidden(PlayerEntity player, ShopEntry entry) {
        if (!isLibertinari(player)) {
            return false;
        }
        return entry.stack().isOf(WatheItems.POISON_VIAL) || entry.stack().isOf(WatheItems.SCORPION);
    }

    public static List<ShopEntry> discount(PlayerEntity player, List<ShopEntry> entries) {
        if (!isLibertinari(player)) {
            return entries;
        }
        float discount = WathedeluxeConfig.HANDLER.instance().libertinariDiscount;
        List<ShopEntry> adjusted = new ArrayList<>(entries.size());
        for (ShopEntry entry : entries) {
            if (entry.stack().isOf(WatheItems.GRENADE)) {
                adjusted.add(new ShopEntry(entry.stack(), Math.round(entry.price() * discount), entry.type()));
            } else {
                adjusted.add(entry);
            }
        }
        return adjusted;
    }
}
