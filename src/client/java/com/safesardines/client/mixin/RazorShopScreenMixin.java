package com.safesardines.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.safesardines.RazorRoles;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public class RazorShopScreenMixin {
    @WrapOperation(method = "init()V", at = @At(value = "FIELD", target = "Ldev/doctor4t/wathe/game/GameConstants;SHOP_ENTRIES:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private List<ShopEntry> wathedeluxe$filterShop(Operation<List<ShopEntry>> original) {
        List<ShopEntry> entries = original.call();
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return entries;
        }
        return entries.stream().filter(entry -> !RazorRoles.isShopHidden(player, entry)).toList();
    }
}
