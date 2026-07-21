package kobber.getoverit.mixin.minecraft;

import kobber.getoverit.PlayerWithClimbingAnim;
import kobber.getoverit.api.ClimbingState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements PlayerWithClimbingAnim {
    @Unique
    private double getoverit$edgeY;

    @Unique
    private double getoverit$climbAnimTime = 0;

    public double getoverit$getMaxClimbAnimTime() {
        return 50;
    }

    public double getoverit$getClimbAnimTime() {
        return this.getoverit$climbAnimTime;
    }

    public void getoverit$setClimbAnimTime(double time) {
        this.getoverit$climbAnimTime = time;
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravel(Vec3 travelVector, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        boolean wasClimbing = ClimbingState.isClimbing(player);
        float climbingSpeed = 0.1F;
        float climbingSpeedMax = 0.3F;
        float horizontalDampening = 0.5F;

        if (wasClimbing) {
            player.setDeltaMovement(new Vec3(player.getDeltaMovement().x * horizontalDampening, Math.min(player.getDeltaMovement().y + climbingSpeed, climbingSpeedMax), player.getDeltaMovement().z * horizontalDampening));
        }

        if (player.level().isClientSide) {
            double travelVectorLengthSqr = travelVector.lengthSqr();
            if (!player.isCrouching() && !player.onGround() && !player.onClimbable() && !player.isSpectator() && player.horizontalCollision && travelVectorLengthSqr > 0.01) {
                Vec3 vec3 = travelVector.normalize();
                float yRotSin = Mth.sin(player.getYRot() * ((float)Math.PI / 180F));
                float yRotCos = Mth.cos(player.getYRot() * ((float)Math.PI / 180F));
                Vec3 horizontalTravelDirection = new Vec3(vec3.x * (double)yRotCos - vec3.z * (double)yRotSin, 0, vec3.z * (double)yRotCos + vec3.x * (double)yRotSin);

                Direction direction = Direction.getNearest(horizontalTravelDirection);
                if (Direction.getNearest(player.calculateViewVector(0, player.getYRot())) != direction) {
                    return;
                }

                double yVelocity = player.getDeltaMovement().y;
                if (yVelocity < 0.005 && yVelocity > -0.7) {
                    this.getoverit$climbAnimTime = this.getoverit$getMaxClimbAnimTime();
                    ClimbingState.setClimbing(player, true);
                    this.getoverit$edgeY = player.position().y + 1.05 + yVelocity * -1;
                }

                AABB collisionBox = player.getBoundingBox().move(0, this.getoverit$edgeY - player.getY(), 0).move(horizontalTravelDirection.normalize().scale(0.25));
                boolean collision = !player.level().noCollision(collisionBox);

                if (collision) {
                    ClimbingState.setClimbing(player, false);
                } else if (ClimbingState.isClimbing(player) && !wasClimbing) {
                    player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1));
                }
            } else {
                ClimbingState.setClimbing(player, false);
            }
        }
    }
}
