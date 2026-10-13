package com.safesardines.client.mixin;

import cat.rezelyn.watheextended.client.screen.guidebook.GuidebookEntryItems;
import cat.rezelyn.watheextended.client.screen.guidebook.GuidebookEntryItems.RoleItem;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.safesardines.DeluxeShop;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(GuidebookEntryItems.class)
public class DeluxeGuidebookMixin {
    private static final Set<String> RAZOR_HIDDEN = Set.of("revolver", "grenade", "poison_vial", "scorpion");
    private static final Set<String> LIBERTINARI_HIDDEN = Set.of("poison_vial", "scorpion");

    @ModifyReturnValue(method = "getItemsForRole", at = @At("RETURN"))
    private static List<RoleItem> wathedeluxe$filterShop(List<RoleItem> original, String role, boolean flag) {
        if (role == null) {
            return original;
        }
        boolean razor = role.equals("wathedeluxe:razor") || role.equals("razor");
        boolean libertinari = role.equals("wathedeluxe:libertinari") || role.equals("libertinari");
        if (!razor && !libertinari) {
            return original;
        }
        List<RoleItem> result = new ArrayList<>(original.size());
        for (RoleItem item : original) {
            String icon = item.icon();
            if (razor && RAZOR_HIDDEN.contains(icon)) {
                continue;
            }
            if (libertinari && LIBERTINARI_HIDDEN.contains(icon)) {
                continue;
            }
            if (libertinari && icon.equals("grenade")) {
                result.add(new RoleItem(icon, item.name(), libertinariPrice(item.price())));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    private static int libertinariPrice(int base) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameTimeComponent time = client.world == null ? null : GameTimeComponent.KEY.get(client.world);
        return DeluxeShop.libertinariGrenadePrice(base, time);
    }
}
