package com.safesardines.client;

import com.safesardines.WathedeluxeConfig;
import com.safesardines.WathedeluxeConfigPayload;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DeluxeCategory {
    private DeluxeCategory() {
    }

    public static ConfigCategory build() {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("gui.watheextended.config.category.deluxe"))
                .tooltip(Text.translatable("gui.watheextended.config.category.deluxe.tooltip"))
                .option(toggle("destruction", false, () -> WathedeluxeConfig.HANDLER.instance().destruction, value -> WathedeluxeConfig.HANDLER.instance().destruction = value))
                .option(toggle("placement", false, () -> WathedeluxeConfig.HANDLER.instance().placement, value -> WathedeluxeConfig.HANDLER.instance().placement = value))
                .option(toggle("collisionsWhileWalking", false, () -> WathedeluxeConfig.HANDLER.instance().collisionsWhileWalking, value -> WathedeluxeConfig.HANDLER.instance().collisionsWhileWalking = value))
                .option(slider("libertinariDiscount", 0.8F, () -> WathedeluxeConfig.HANDLER.instance().libertinariDiscount, value -> WathedeluxeConfig.HANDLER.instance().libertinariDiscount = value))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("gui.watheextended.config.category.deluxe.label.timed_discounts"))
                        .option(toggle("timeDiscount", true, () -> WathedeluxeConfig.HANDLER.instance().timeDiscount, value -> WathedeluxeConfig.HANDLER.instance().timeDiscount = value))
                        .option(slider("maxDiscount", 100.0F / 350.0F, () -> WathedeluxeConfig.HANDLER.instance().maxDiscount, value -> WathedeluxeConfig.HANDLER.instance().maxDiscount = value))
                        .option(toggle("uncapped", false, () -> WathedeluxeConfig.HANDLER.instance().uncapped, value -> WathedeluxeConfig.HANDLER.instance().uncapped = value))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("gui.watheextended.config.category.deluxe.label.silly"))
                        .option(toggle("breakAnything", false, () -> WathedeluxeConfig.HANDLER.instance().breakAnything, value -> WathedeluxeConfig.HANDLER.instance().breakAnything = value))
                        .option(toggle("itemDrops", false, () -> WathedeluxeConfig.HANDLER.instance().itemDrops, value -> WathedeluxeConfig.HANDLER.instance().itemDrops = value))
                        .option(toggle("placeAnything", false, () -> WathedeluxeConfig.HANDLER.instance().placeAnything, value -> WathedeluxeConfig.HANDLER.instance().placeAnything = value))
                        .build())
                .build();
    }

    private static Option<Boolean> toggle(String field, boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Text.translatable("yacl3.config.wathedeluxe:config." + field))
                .description(description(field, boolValue(def)))
                .binding(def, getter, value -> {
                    setter.accept(value);
                    pushed();
                })
                .controller(option -> BooleanControllerBuilder.create(option).formatValue(DeluxeCategory::boolValue))
                .build();
    }

    private static Option<Float> slider(String field, float def, Supplier<Float> getter, Consumer<Float> setter) {
        return Option.<Float>createBuilder()
                .name(Text.translatable("yacl3.config.wathedeluxe:config." + field))
                .description(description(field, Text.literal("§e" + String.format(Locale.ROOT, "%.2f", def))))
                .binding(def, getter, value -> {
                    setter.accept(value);
                    pushed();
                })
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0.0F, 1.0F).step(0.01F))
                .build();
    }

    private static OptionDescription description(String field, Text defaultValue) {
        Text text = Text.translatable("yacl3.config.wathedeluxe:config." + field + ".desc")
                .append(Text.literal("\n\n§6Default:§r ")).append(defaultValue);
        return OptionDescription.of(text);
    }

    private static Text boolValue(boolean value) {
        return Text.translatable(value ? "gui.watheextended.config.text.on" : "gui.watheextended.config.text.off");
    }

    private static void pushed() {
        WathedeluxeConfig.HANDLER.save();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getServer() != null) {
            return;
        }
        if (ClientPlayNetworking.canSend(WathedeluxeConfigPayload.ID)) {
            WathedeluxeConfig config = WathedeluxeConfig.HANDLER.instance();
            ClientPlayNetworking.send(new WathedeluxeConfigPayload(config.destruction, config.placement, config.collisionsWhileWalking, config.breakAnything, config.itemDrops, config.placeAnything, config.libertinariDiscount, config.timeDiscount, config.maxDiscount, config.uncapped));
        }
    }
}
