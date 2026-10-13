package com.safesardines;

import dev.doctor4t.wathe.util.AdventureUsable;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;

public class BreakItem extends PickaxeItem implements AdventureUsable {
    public BreakItem(ToolMaterial material, Item.Settings settings) {
        super(material, settings);
    }
}
