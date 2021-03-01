package draylar.lulgf.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    private Vec3d lulgf_cachedVelocity = null;

    @Inject(
            method = "applyClimbingSpeed",
            at = @At("HEAD"))
    private void storeClimbingSpeedContext(Vec3d motion, CallbackInfoReturnable<Vec3d> cir) {
        lulgf_cachedVelocity = motion;
    }

    /**
     * When an entity is on a ladder, their negative velocity is capped to -0.15.
     * This mixin removes this restriction so {@link ClientPlayerEntityMixin} can apply a stronger fall speed to the player.
     *
     * @param motion  original player motion (which has been re-assigned to by the method at this point)
     * @param cir     mixin callback info
     */
    @Inject(
            method = "applyClimbingSpeed",
            at = @At("RETURN"),
            cancellable = true)
    private void cancelDescentSpeedReduction(Vec3d motion, CallbackInfoReturnable<Vec3d> cir) {
        Vec3d returnValue = cir.getReturnValue();

        if(pitch > 45 && !isSneaking()) {
            cir.setReturnValue(new Vec3d(returnValue.getX(), lulgf_cachedVelocity.getY(), returnValue.getZ()));
        }
    }
}
