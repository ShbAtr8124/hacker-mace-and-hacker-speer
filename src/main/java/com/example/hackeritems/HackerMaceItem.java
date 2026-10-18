package com.example.hackeritems;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class HackerMaceItem extends SwordItem {
    public HackerMaceItem(net.minecraft.item.ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = attacker.world;
        if (!world.isClient) {
            // zusätzlicher "Hacker" Direktschaden
            target.damage(net.minecraft.entity.damage.DamageSource.GENERIC, 4.0F);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        // dauerhaftes Glitzern
        return true;
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (!this.isIn(group)) return;
        ItemStack stack = new ItemStack(this);

        Map<Enchantment, Integer> enchants = new HashMap<>();

        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            Identifier id = Registries.ENCHANTMENT.getId(enchantment);
            if (id == null) continue;
            String path = id.getPath();

            // Sharpness vollständig ausschließen
            if ("sharpness".equals(path)) continue;

            // Nur Enchantments hinzufügen, die für dieses Item anwendbar sind
            if (!enchantment.isAcceptableItem(stack)) continue;

            int level = enchantment.getMaxLevel();

            // Wind-Burst auf Stufe 1 begrenzen (so sind Stufe 2 und 3 nicht möglich)
            if (path.contains("wind_burst") || path.contains("wind")) {
                level = Math.min(level, 1);
            }

            if (level <= 0) continue;
            enchants.put(enchantment, level);
        }

        EnchantmentHelper.set(enchants, stack);
        stacks.add(stack);
    }
}
