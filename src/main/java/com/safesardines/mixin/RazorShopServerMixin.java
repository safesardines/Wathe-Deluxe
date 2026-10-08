package com.safesardines.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.safesardines.RazorRoles;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PlayerShopComponent.class)
public class RazorShopServerMixin {
    @Shadow
    private PlayerEntity player;

    @WrapOperation(method = "tryBuy(I)V", at = @At(value = "FIELD", target = "Ldev/doctor4t/wathe/game/GameConstants;SHOP_ENTRIES:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private List<ShopEntry> wathedeluxe$filterShop(Operation<List<ShopEntry>> original) {
        List<ShopEntry> entries = original.call();
        return entries.stream().filter(entry -> !RazorRoles.isShopHidden(this.player, entry)).toList();
    }
}
