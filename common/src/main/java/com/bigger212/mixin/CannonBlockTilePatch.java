package com.bigger212.mixin;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/*
* When a cannon fires from a ship its BlockPos and firing
* direction will use 'ship world' coordinates. This converts both the muzzle position and
* direction into 'local world' coordinates before spawning the projectile.
*/
@Mixin(CannonBlockTile.class)
public abstract class CannonBlockTilePatch {

    @Redirect(
        method = "shootProjectile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/mehvahdjukaar/supplementaries/common/block/fire_behaviors/IFireItemBehavior;fire(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FLnet/minecraft/world/phys/Vec3;FILnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean vs_sup_fix$shootProjectile(
        IFireItemBehavior behavior,
        ItemStack stack,
        ServerLevel level,
        BlockPos pos,
        float fireOffset,
        Vec3 direction,
        float power,
        int inaccuracy,
        Player owner
    ) {
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, pos);

        if (ship == null) {
            // Falls back to Supplementaries fire call when the cannon is not on a ship.
            return behavior.fire(stack, level, pos, fireOffset, direction, power, inaccuracy, owner);
        }

        Vec3 localFirePos = pos.getCenter().add(direction.normalize().scale(fireOffset));
        Vector3d worldFirePosJoml = ship.getShipToWorld().transformPosition(
            new Vector3d(localFirePos.x, localFirePos.y, localFirePos.z)
        );
        Vector3d worldDirectionJoml = ship.getShipToWorld().transformDirection(
            new Vector3d(direction.x, direction.y, direction.z)
        );
        Vec3 worldFirePos = new Vec3(
            worldFirePosJoml.x,
            worldFirePosJoml.y,
            worldFirePosJoml.z
        );
        Vec3 worldDirection = new Vec3(
            worldDirectionJoml.x,
            worldDirectionJoml.y,
            worldDirectionJoml.z
        ).normalize();
        return behavior.fire(stack, level, worldFirePos, worldDirection, power, inaccuracy, owner);
    }
}