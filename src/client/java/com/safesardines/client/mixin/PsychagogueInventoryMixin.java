package com.safesardines.client.mixin;

import com.safesardines.PsychagogueRoles;
import com.safesardines.client.PsychagoguePlayerWidget;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class PsychagogueInventoryMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    public PsychagogueInventoryMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void psychagogue$panel(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        if (!PsychagogueRoles.isPsychagogue(client.player)) {
            return;
        }
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null) {
            return;
        }
        List<PlayerListEntry> players = new ArrayList<>();
        for (PlayerListEntry entry : handler.getPlayerList()) {
            if (entry.getProfile().getId().equals(client.player.getUuid())) {
                continue;
            }
            if (entry.getGameMode() == GameMode.SPECTATOR || entry.getGameMode() == GameMode.CREATIVE) {
                continue;
            }
            players.add(entry);
        }
        int headGap = 32;
        int rowGap = 44;
        int perRow = 20;
        int headY = (this.height - 32) / 2 + 68;
        for (int i = 0; i < players.size(); i += perRow) {
            List<PlayerListEntry> row = players.subList(i, Math.min(i + perRow, players.size()));
            int headX = this.width / 2 - row.size() * headGap / 2 + 9;
            int rowY = headY + (i / perRow) * rowGap;
            for (PlayerListEntry entry : row) {
                this.addDrawableChild(new PsychagoguePlayerWidget(headX, rowY, entry));
                headX += headGap;
            }
        }
    }
}
