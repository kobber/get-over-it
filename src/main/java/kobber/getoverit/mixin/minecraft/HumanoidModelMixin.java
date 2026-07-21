package kobber.getoverit.mixin.minecraft;

import kobber.getoverit.PlayerWithClimbingAnim;
import kobber.getoverit.api.ClimbingState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void getoverit$setupClimbAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player) {
            if (ClimbingState.isClimbing(player)) {
                HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

                PlayerWithClimbingAnim climbingPlayer = (PlayerWithClimbingAnim) player;
                double animTime = climbingPlayer.getoverit$getClimbAnimTime();

                if (animTime > 0) {
                    double maxAnimTime = climbingPlayer.getoverit$getMaxClimbAnimTime();
                    float progress = (float) Math.pow(1 - animTime / maxAnimTime, 2);

                    model.rightArm.xRot = (float)Mth.lerp(progress, Math.PI * 1.7F, Math.PI * 2F );
                    model.leftArm.xRot = (float)Mth.lerp(progress, Math.PI * 1.7F, Math.PI * 2F );
                    model.rightArm.zRot = 0;
                    model.leftArm.zRot =  0;
                    model.rightArm.yRot = 0.1F;
                    model.leftArm.yRot = -0.1F;

                    model.body.xRot = 0.4F;

                    model.head.y = 3.0F;
                    model.body.y = 3.2F;
                    model.leftArm.y = 5.2F;
                    model.rightArm.y = 5.2F;

                    model.rightLeg.z = 3.0F;
                    model.leftLeg.z = 4.0F;

                    model.rightLeg.xRot = Mth.lerp(progress, -0.5F, 0 );
                    model.leftLeg.xRot = Mth.lerp(progress, 0.2F, -0.1F );

                    model.rightLeg.y = Mth.lerp(progress, 10.0F, 13.0F );
                    model.leftLeg.y = 13.2F;

                    model.rightLeg.yRot = 0;
                    model.leftLeg.yRot = 0;
                    model.rightLeg.zRot = 0;
                    model.leftLeg.zRot = 0;

                    climbingPlayer.getoverit$setClimbAnimTime(animTime - 1);
                }
            }
        }
    }
}