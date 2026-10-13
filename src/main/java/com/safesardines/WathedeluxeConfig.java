package com.safesardines;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.autogen.FloatSlider;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class WathedeluxeConfig {
    public static final ConfigClassHandler<WathedeluxeConfig> HANDLER = ConfigClassHandler
            .createBuilder(WathedeluxeConfig.class)
            .id(Identifier.of("wathedeluxe", "config"))
            .serializer(handler -> GsonConfigSerializerBuilder.create(handler)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("wathedeluxe.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @AutoGen(category = "blocks")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean destruction = false;

    @AutoGen(category = "blocks")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean placement = false;

    @AutoGen(category = "blocks")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean collisionsWhileWalking = false;

    @AutoGen(category = "blocks")
    @FloatSlider(min = 0.0F, max = 1.0F, step = 0.05F)
    @SerialEntry
    public float libertinariDiscount = 0.8F;

    @AutoGen(category = "blocks")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean timeDiscount = true;

    @AutoGen(category = "blocks")
    @FloatSlider(min = 0.0F, max = 1.0F, step = 0.01F)
    @SerialEntry
    public float maxDiscount = 100.0F / 350.0F;

    @AutoGen(category = "blocks")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean uncapped = false;

    @AutoGen(category = "silly")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean breakAnything = false;

    @AutoGen(category = "silly")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean itemDrops = false;

    @AutoGen(category = "silly")
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @SerialEntry
    public boolean placeAnything = false;
}
