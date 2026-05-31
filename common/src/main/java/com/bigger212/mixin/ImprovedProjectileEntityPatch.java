package com.bigger212.mixin;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

@Mixin({ImprovedProjectileEntity.class})
public abstract class ImprovedProjectileEntityPatch {
   public ImprovedProjectileEntityPatch() {
   }

   @Redirect(
      method = {"Lnet/mehvahdjukaar/moonlight/api/entity/ImprovedProjectileEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/mehvahdjukaar/moonlight/api/util/math/MthUtils;collideWithSweptAABB(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;D)Lnet/minecraft/world/phys/BlockHitResult;"
)
   )
   private BlockHitResult redirectAABBCollision(Entity entity, Vec3 movement, double distance) {
      Vec3 start = entity.position();
      Vec3 end = start.add(movement);
      ClipContext context = new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, entity);
      
      return RaycastUtilsKt.clipIncludeShips(entity.level(), context);
   }
}
