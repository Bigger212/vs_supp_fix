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

@Mixin({CannonBallEntity.class})
public abstract class CannonBallEntityPatch {
   public CannonBallEntityPatch() {
   }

   @Inject(
      method = {"onHitBlock"},
      at = {@At("HEAD")}
   )
   private void injectOnBlockHit(BlockHitResult result, CallbackInfo ci) {
      CannonBallEntity self = (CannonBallEntity)this;
      Level level = self.m_9236_();
      if (level instanceof ServerLevel serverLevel) {
         Vec3 hitPos = result.m_82450_();
         Ship ship = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, result.m_82425_());
         if (ship != null) {
            double radius;
            if (vs_sup_fix.DAMAGE_SHIPS_UNIQUELY) {
               radius = vs_sup_fix.CANNONBALL_BREAK_RADIUS;
            } else {
               try {
                  radius = (Double)Functional.CANNONBALL_BREAK_RADIUS.get();
               } catch (NullPointerException | NoSuchFieldError var20) {
                  radius = vs_sup_fix.CANNONBALL_BREAK_RADIUS;
               }
            }

            Vec3 movement = self.m_20184_();
            double vel = Math.abs(movement.m_82553_());
            float scaling = 5.0F;
            float maxAmount = (float)(vel * vel * (double)scaling);
            Vector3d hitPosJoml = new Vector3d(hitPos.f_82479_, hitPos.f_82480_, hitPos.f_82481_);
            Vector3d shipHitPosJoml = ship.getWorldToShip().transformPosition(hitPosJoml);
            Vec3 shipHitPos = new Vec3(shipHitPosJoml.x, shipHitPosJoml.y, shipHitPosJoml.z);
            BlockPos shipCenter = new BlockPos((int)Math.floor(shipHitPos.f_82479_), (int)Math.floor(shipHitPos.f_82480_), (int)Math.floor(shipHitPos.f_82481_));
            CannonBallExplosion explosion = new CannonBallExplosion(level, (Entity)null, shipHitPosJoml.x, shipHitPosJoml.y, shipHitPosJoml.z, shipCenter, maxAmount, (float)radius, (Set)null);
            explosion.m_46061_();
            explosion.m_46075_(true);
         }

      }
   }
}
