package com.example.hackeritems;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
            if (enchantment == Enchantments.SHARPNESS) continue;

            // Nur Enchantments hinzufügen, die für dieses Item anwendbar sind
            if (!enchantment.isAcceptableItem(stack)) continue;

            int level = enchantment.getMaxLevel();

            // Wind-Burst Stufen 2 und 3 ausschließen: max auf 1 begrenzen
            if (path.contains("wind_burst") || path.contains("wind")) {
                level = Math.min(level, 1);
            }

            if (level <= 0) continue;
            enchants.put(enchantment, level);
        }

        // Stelle sicher, dass Fire Aspect drauf ist
        if (Enchantments.FIRE_ASPECT.isAcceptableItem(stack)) {
            enchants.put(Enchantments.FIRE_ASPECT, Enchantments.FIRE_ASPECT.getMaxLevel());
        }

        EnchantmentHelper.set(enchants, stack);
        stacks.add(stack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            // Erzeuge eine "Wind Charge" und wende sie sofort an: stoße alle LivingEntities vor dem Spieler weg
            Vec3d look = user.getRotationVec(1.0F);
            double range = 6.0D;

            // Erstelle eine Box in Blickrichtung, grob konisch
            Box box = user.getBoundingBox().stretch(look.multiply(range)).expand(1.0D, 1.0D, 1.0D).expand(range);

            List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box, e -> e != user && e.isAlive());

            for (LivingEntity target : targets) {
                Vec3d dir = target.getPos().subtract(user.getPos()).normalize();
                if (Double.isNaN(dir.x) || Double.isNaN(dir.y) || Double.isNaN(dir.z)) continue;

                // Setze Velocity so, dass das Ziel wegfliegt (Wind-Effekt)
                double strength = 1.6D;
                target.setVelocity(dir.x * strength, Math.max(0.4D, dir.y + 0.2D), dir.z * strength);
                target.velocityModified = true;

                // Optional: leichte Empfehlung von Schaden/Knockback
                target.takeKnockback(0.5F, dir.x, dir.z);
            }

            if (world instanceof ServerWorld server) {
                server.spawnParticles(ParticleTypes.CLOUD, user.getX(), user.getY() + 1.0, user.getZ(), 20, 1.0, 0.5, 1.0, 0.05);
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
