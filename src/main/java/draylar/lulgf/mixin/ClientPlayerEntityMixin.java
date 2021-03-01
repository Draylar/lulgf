package draylar.lulgf.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {

    @Shadow public abstract boolean isSneaking();

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        // -90 pitch is up, 90 is down
        boolean isInsideLadder = world.getBlockState(getBlockPos()).getBlock() instanceof LadderBlock;

        if(isInsideLadder && !isSneaking()) {
            // Going up
            if(pitch < -45 && !MinecraftClient.getInstance().options.keyBack.isPressed()) {
                int ladderCount = countLadders(1);
                setVelocity(getVelocity().getX(), Math.min(3, ladderCount / 10f) * ((pitch + 45) / -45), getVelocity().getZ());
            }

            // Going down
            else if (pitch > 45 && !MinecraftClient.getInstance().options.keyForward.isPressed()) {
                int ladderCount = countLadders(-1);
                System.out.println(((pitch + 45) / -45));
                setVelocity(getVelocity().getX(), Math.min(3, ladderCount / 10f) * ((pitch - 45) / 45), getVelocity().getZ());
            }
        }
    }

    @Unique
    private int countLadders(int direction) {
        int count = 0;
        BlockState state;

        do {
            state = world.getBlockState(getBlockPos().add(0, count, 0));
            count += direction;
        } while (state.getBlock() instanceof LadderBlock);

        return count;
    }
}
