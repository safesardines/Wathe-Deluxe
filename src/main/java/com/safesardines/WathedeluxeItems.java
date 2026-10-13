package com.safesardines;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WathedeluxeItems {
    public static Item BREAKITEM;

    public static void register() {
        BREAKITEM = Registry.register(Registries.ITEM, Identifier.of("wathedeluxe", "breakitem"),
                new BreakItem(ToolMaterials.IRON, new Item.Settings().maxCount(1).maxDamage(420420)));
    }
}
