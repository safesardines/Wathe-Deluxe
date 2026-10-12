package com.safesardines;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class PsychagogueRoles {
    public static Role PSYCHAGOGUE;

    public static void register() {
        PSYCHAGOGUE = WatheRoles.registerRole(new Role(Identifier.of("wathedeluxe", "psychagogue"), 0x8A2BE2, false, true, Role.MoodType.FAKE, -1, true));
    }

    public static boolean isPsychagogue(PlayerEntity player) {
        return PSYCHAGOGUE != null && GameWorldComponent.KEY.get(player.getWorld()).isRole(player, PSYCHAGOGUE);
    }
}
