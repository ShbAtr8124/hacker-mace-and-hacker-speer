package com.example.hackeritems;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HackerSpearItem extends Item {
    public HackerSpearItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            Vec3d dir = user.getRotationVec(1.0F).multiply(1.6);
            ItemEntity thrown = new ItemEntity(world, user.getX() + dir.x, user.getEyeY() + dir.y, user.getZ() + dir.z, new ItemStack(this));
            thrown.setVelocity(dir.x, dir.y + 0.1, dir.z);
            thrown.setOwner(user);
            world.spawnEntity(thrown);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
