package com.safesardines;

import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class DeluxeShop {
    public static List<ShopEntry> apply(PlayerEntity player, List<ShopEntry> entries) {
        WathedeluxeConfig config = WathedeluxeConfig.HANDLER.instance();
        List<ShopEntry> result = entries;
        if (config.timeDiscount) {
            result = timeDiscount(player, result, config);
        }
        return LibertinariRoles.discount(player, result);
    }

    private static List<ShopEntry> timeDiscount(PlayerEntity player, List<ShopEntry> entries, WathedeluxeConfig config) {
        GameTimeComponent time = GameTimeComponent.KEY.get(player.getWorld());
        List<ShopEntry> adjusted = new ArrayList<>(entries.size());
        for (ShopEntry entry : entries) {
            adjusted.add(new DiscountedEntry(entry, timePrice(entry.price(), time, config)));
        }
        return adjusted;
    }

    public static int libertinariGrenadePrice(int base, GameTimeComponent time) {
        WathedeluxeConfig config = WathedeluxeConfig.HANDLER.instance();
        int price = config.timeDiscount && time != null ? timePrice(base, time, config) : base;
        return Math.round(price * config.libertinariDiscount);
    }

    private static int timePrice(int base, GameTimeComponent time, WathedeluxeConfig config) {
        if (time.resetTime <= 0) {
            return base;
        }
        float percent = (float) (time.resetTime - time.getTime()) / time.resetTime;
        if (!config.uncapped) {
            percent = Math.max(0.0F, percent);
        }
        percent = Math.min(percent, 1.0F);
        int discount = (int) (Math.ceil(base * config.maxDiscount * percent / 10.0) * 10.0);
        return base - discount;
    }

    private static final class DiscountedEntry extends ShopEntry {
        private final ShopEntry delegate;
        private final int price;

        DiscountedEntry(ShopEntry delegate, int price) {
            super(delegate.stack(), price, delegate.type());
            this.delegate = delegate;
            this.price = price;
        }

        @Override
        public int price() {
            return this.price;
        }

        @Override
        public boolean onBuy(PlayerEntity player) {
            return this.delegate.onBuy(player);
        }
    }
}
