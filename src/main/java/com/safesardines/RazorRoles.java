package com.safesardines;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RazorRoles {
    public static Role RAZOR;
    public static EntityType<ThrownKnifeEntity> THROWN_KNIFE;

    public static void register() {
        RAZOR = WatheRoles.registerRole(new Role(Identifier.of("wathedeluxe", "razor"), 0x555555, false, true, Role.MoodType.FAKE, -1, true));
        // i am a 100x JAVA KING developer. i am a 100x JAVA KING developer. i a
        THROWN_KNIFE = Registry.register(Registries.ENTITY_TYPE, Identifier.of("wathedeluxe", "thrown_knife"), EntityType.Builder.<ThrownKnifeEntity>create(ThrownKnifeEntity::new, SpawnGroup.MISC).dimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10).build("thrown_knife"));
    }

    // sync
    public static boolean isShopHidden(PlayerEntity player, ShopEntry entry) {
        if (RAZOR == null || !GameWorldComponent.KEY.get(player.getWorld()).isRole(player, RAZOR)) {
            return false;
        }
        return entry.stack().isOf(WatheItems.GRENADE)
                || entry.stack().isOf(WatheItems.POISON_VIAL)
                || entry.stack().isOf(WatheItems.SCORPION);
    }
}
