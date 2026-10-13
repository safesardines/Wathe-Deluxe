package com.safesardines.client.mixin;

import cat.rezelyn.watheextended.client.screen.ConfigScreen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.safesardines.client.DeluxeCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ConfigScreen.class)
public class DeluxeConfigScreenMixin {
    @WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Ldev/isxander/yacl3/api/YetAnotherConfigLib$Builder;build()Ldev/isxander/yacl3/api/YetAnotherConfigLib;"))
    private static YetAnotherConfigLib wathedeluxe$deluxeCategory(YetAnotherConfigLib.Builder builder, Operation<YetAnotherConfigLib> original) {
        builder.category(DeluxeCategory.build());
        return original.call(builder);
    }
}
