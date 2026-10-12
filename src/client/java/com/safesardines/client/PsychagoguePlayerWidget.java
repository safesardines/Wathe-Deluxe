package com.safesardines.client;

import com.safesardines.PsychagogueDataPayload;
import com.safesardines.PsychagogueQueuePayload;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

import java.util.UUID;

public class PsychagoguePlayerWidget extends ButtonWidget {
    private static final Identifier SLOT = Identifier.of("wathedeluxe", "gui/psychagogue_slot");
    private final UUID uuid;

    public PsychagoguePlayerWidget(int x, int y, PlayerListEntry entry) {
        super(x, y, 16, 44, Text.empty(), button -> cycle(entry.getProfile().getId()), DEFAULT_NARRATION_SUPPLIER);
        this.uuid = entry.getProfile().getId();
    }

    private static void cycle(UUID uuid) {
        int[] order = {PlayerMoodComponent.Task.EAT.ordinal(), PlayerMoodComponent.Task.DRINK.ordinal(), PlayerMoodComponent.Task.SLEEP.ordinal(), PlayerMoodComponent.Task.OUTSIDE.ordinal(), -1};
        int current = PsychagogueClientData.next(uuid);
        int index = 0;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == current) {
                index = i;
            }
        }
        int next = order[(index + 1) % order.length];
        PsychagogueClientData.setNext(uuid, next);
        ClientPlayNetworking.send(new PsychagogueQueuePayload(uuid, next));
    }

    private static int colorFor(PsychagogueDataPayload.Entry data) {
        Role.MoodType type = Role.MoodType.values()[data.moodType()];
        if (type == Role.MoodType.FAKE) {
            return MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F) | 0xFF000000;
        }
        if (type == Role.MoodType.REAL) {
            return MathHelper.hsvToRgb(MathHelper.clamp(data.mood(), 0.0F, 1.0F) / 3.0F, 1.0F, 1.0F) | 0xFF000000;
        }
        return 0xFFFFFFFF;
    }

    private static Text taskText(int ordinal, boolean killer) {
        Text name = Text.translatable("psychagogue.task." + PlayerMoodComponent.Task.values()[ordinal].name().toLowerCase());
        return killer ? Text.literal("'").append(name).append("'") : name;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        int x = this.getX();
        int y = this.getY();
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        PlayerListEntry entry = handler == null ? null : handler.getPlayerListEntry(this.uuid);
        if (entry == null || entry.getGameMode() == GameMode.SPECTATOR || entry.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        context.drawGuiTexture(SLOT, x - 7, y - 7, 30, 30);
        PlayerSkinDrawer.draw(context, entry.getSkinTextures(), x, y, 16);
        if (this.isHovered()) {
            context.drawBorder(x, y, 16, 16, 0xFFFFFFFF);
        }
        PsychagogueDataPayload.Entry data = PsychagogueClientData.get(this.uuid);
        if (data == null) {
            return;
        }
        boolean killer = Role.MoodType.values()[data.moodType()] == Role.MoodType.FAKE;
        Text current = data.task() < 0 ? Text.literal("-") : taskText(data.task(), killer);
        Text next = data.next() < 0 ? Text.translatable("psychagogue.any") : taskText(data.next(), killer);
        Text nextLine = killer ? Text.translatable("psychagogue.ask", next) : Text.translatable("psychagogue.next", next);
        float scale = 0.5F + 1.0F / textRenderer.fontHeight;
        context.getMatrices().push();
        context.getMatrices().translate(x + 8.0F, y + 21.0F, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawTextWithShadow(textRenderer, current, -textRenderer.getWidth(current) / 2, 0, colorFor(data));
        context.getMatrices().pop();
        boolean hovered = this.isHovered();
        float nextScale = hovered ? scale : 0.5F;
        int nextColor = hovered ? 0xC8A8D8 : (killer ? 0xC9A0FF : 0xAAAAAA);
        context.getMatrices().push();
        context.getMatrices().translate(x + 8.0F, y + 21.0F + (textRenderer.fontHeight + 2.0F) * scale, 0.0F);
        context.getMatrices().scale(nextScale, nextScale, 1.0F);
        context.drawTextWithShadow(textRenderer, nextLine, -textRenderer.getWidth(nextLine) / 2, 0, nextColor);
        context.getMatrices().pop();
    }
}
