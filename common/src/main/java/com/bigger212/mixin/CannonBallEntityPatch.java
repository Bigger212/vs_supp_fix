package com.bigger212.mixin;

import java.util.Set;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBallEntity;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs.Functional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/*
* When a cannonball hits a ship, Supplementaries explosion receives
* 'ship world' coordinates to perform destruction on.
* Falls back to Supplementaries logic when the impact is not on a ship.
*/
@Mixin({CannonBallEntity.class})
public abstract class CannonBallEntityPatch {
   public CannonBallEntityPatch() {
   }

   @Inject(
      method = {"onHitBlock"},
      at = {@At("HEAD")}
   )
   private void vs_sup_fix$injectOnBlockHit(BlockHitResult result, CallbackInfo ci) {
      CannonBallEntity self = (CannonBallEntity)(Object)this;
      Level level = self.level();
      
      if (level instanceof ServerLevel serverLevel) {
         Vec3 hitPos = result.getLocation();
         Ship ship = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, result.getBlockPos());
         
         if (ship != null) {
            double radius;
            // Simple try-catch if previous versions of Supplementaries do not have CANNONBALL_BREAK_RADIUS.
            try {
                radius = (Double)Functional.CANNONBALL_BREAK_RADIUS.get();
            } catch (NullPointerException | NoSuchFieldError var20) {
                radius = 1.1;
            }
            
            Vec3 movement = self.getDeltaMovement();
            double vel = Math.abs(movement.length());
            float scaling = 5.0F;
            float maxAmount = (float)(vel * vel * (double)scaling);
            Vector3d hitPosJoml = new Vector3d(hitPos.x, hitPos.y, hitPos.z);
            Vector3d shipHitPosJoml = ship.getWorldToShip().transformPosition(hitPosJoml);
            Vec3 shipHitPos = new Vec3(shipHitPosJoml.x, shipHitPosJoml.y, shipHitPosJoml.z);
            BlockPos shipCenter = new BlockPos((int)Math.floor(shipHitPos.x), (int)Math.floor(shipHitPos.y), (int)Math.floor(shipHitPos.z));
            CannonBallExplosion explosion = new CannonBallExplosion(level, (Entity)null, shipHitPosJoml.x, shipHitPosJoml.y, shipHitPosJoml.z, shipCenter, maxAmount, (float)radius, (Set)null);
            /*
            * Do not cancel so that the Supplementaries explosion effects the 'local world' simultaneously.
            * Essentially double the explosions for double the 'worlds'.
            */
            explosion.explode();
            explosion.finalizeExplosion(true);
         }
      }
   }
}
