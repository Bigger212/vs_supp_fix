package com.bigger212.mixin;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/*
* When on a ship; cannon BlockPos is in 'ship world' coordinates and moving.
* This patch keeps the player & camera in 'local world' coordinates while converting
* cannon positions and aiming vectors between 'ship world' and 'local world' coordinates as needed.
*/
@Mixin(CannonController.class)
public abstract class CannonControllerPatch {

    @Shadow
    protected static CannonBlockTile cannon;

    @Shadow
    private static boolean needsToUpdateServer;

    @Shadow
    public static void stopControlling() {
        throw new AssertionError();
    }

    @Shadow
    private static float lastCameraYaw;

    @Shadow
    private static float lastCameraPitch;

    /*
    * Convert the cannon to 'local world' coordinates first, then apply the 7-block distance limit.
    * If the cannon becomes invalid; stop locally without syncing to avoid teleporting to 'ship world' coordinates.
    */
    @Inject(
        method = "onClientTick",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void vs_sup_fix$injectOnClientTick(Minecraft mc, CallbackInfo ci) {
        if (cannon == null) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            ci.cancel();
            return;
        }

        Level level = player.level();
        BlockPos pos = cannon.getBlockPos();
        Vec3 cannonCenter = pos.getCenter();
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, pos);

        // Fallback: if no ship is found, pos.getCenter() preserves normal Supplementaries behavior.
        if (ship != null) {
            Vector3d world = ship.getShipToWorld().transformPosition(
                new Vector3d(
                    cannonCenter.x,
                    cannonCenter.y,
                    cannonCenter.z
                )
            );
            cannonCenter = new Vec3(
                world.x,
                world.y,
                world.z
            );
        }

        boolean closeEnough =
            cannonCenter.distanceToSqr(player.position()) < 49.0D;

        if (
            level.getBlockEntity(pos) == cannon &&
            !cannon.isRemoved() &&
            closeEnough
        ) {
            if (needsToUpdateServer) {
                needsToUpdateServer = false;
                CannonBlockTile.syncToServer(cannon, false, false);
            }
        } else {
            stopControlling();
        }
        ci.cancel();
    }

    /*
    * Converts cannon coordinates from 'ship world' to 'local world' for camera setup.
    */
    @Redirect(
        method = "setupCamera",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;getCenter()Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private static Vec3 vs_sup_fix$shipCannonCenterToWorld(
        BlockPos pos,
        Camera camera,
        BlockGetter blockGetter,
        Entity entity,
        boolean detached,
        boolean thirdPersonReverse,
        float partialTick
    ) {
        Vec3 vanillaCenter = pos.getCenter();

        if (!(blockGetter instanceof Level level)) {
            // Fallback: returns the normal BlockPos center when no ship is found.
            return vanillaCenter;
        }

        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, pos);

        if (ship == null) {
            return vanillaCenter;
        }

        Vector3d worldCenter = ship.getShipToWorld().transformPosition(
            new Vector3d(vanillaCenter.x, vanillaCenter.y, vanillaCenter.z)
        );

        return new Vec3(worldCenter.x, worldCenter.y, worldCenter.z);
    }

    /*
    * Converts the cursor target vector into 'local world' coordinates.
    * The camera raycast target is 'ship world' when on a ship, so cannons need the target vector converted
    * back to 'local world' before yaw/pitch are calculated.
    */
    @Redirect(
        method = "setupCamera",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private static Vec3 vs_sup_fix$targetVectorInShipSpace(Vec3 hitLocation, Vec3 cannonWorldCenter) {
        Vec3 worldTargetVector = hitLocation.subtract(cannonWorldCenter);

        Level level = Minecraft.getInstance().level;

        if (cannon == null || level == null) {
            return worldTargetVector;
        }

        BlockPos pos = cannon.getBlockPos();
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, pos);

        if (ship == null) {
            // Fallback: normal Supplementaries target-vector behavior.
            return worldTargetVector;
        }

        Vector3d localTarget = ship.getWorldToShip().transformDirection(
            new Vector3d(worldTargetVector.x, worldTargetVector.y, worldTargetVector.z)
        );

        return new Vec3(localTarget.x, localTarget.y, localTarget.z);
    }


    /*
    * Keeps cannon-aiming-mode camera stable while the ship moves.
    * Without these, the controller pins camera rotation to the entry orientation.
    * Using Supplementaries stored camera yaw/pitch lets mouse deltas accumulate.
    */
    @Redirect(
    method = "setupCamera",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/Camera;getYRot()F",
        ordinal = 1
    )
    )
    private static float vs_sup_fix$useStoredCameraYaw(Camera camera) {
        return lastCameraYaw;
    }

    @Redirect(
        method = "setupCamera",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;getXRot()F",
            ordinal = 1
        )
    )
    private static float vs_sup_fix$useStoredCameraPitch(Camera camera) {
        return lastCameraPitch;
    }
}