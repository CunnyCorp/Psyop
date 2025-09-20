package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.render.HandView;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandRenderer.class, priority = 1)
public abstract class ItemInHandRendererMixin {

    // HandView - MainHandHeight
    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 2))
    public float modifyMainHandY(float f, float g, float h) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.mainHand.get()) {
                return module.getMainHandY(f, g, h);
            }
        }

        return Mth.lerp(f, g, h);
    }

    // HandView - OffhandHeight
    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 3))
    public float modifyOffhandY(float f, float g, float h) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.offhand.get()) {
                return module.getOffhandY(f, g, h);
            }
        }

        return Mth.lerp(f, g, h);
    }

    // HandView - Modify Animation types
    @Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;"))
    public ItemUseAnimation modifyAnimationTypes(ItemStack instance) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.shouldCancelAnimation(instance.getUseAnimation())) {
                return ItemUseAnimation.NONE;
            }
        }

        return instance.getUseAnimation();
    }

    // HandView - Swing Modifier
    @Redirect(method = "applyItemArmTransform", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    public void modifySwingPos(PoseStack instance, float x, float y, float z) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.swingAnimation.get()) {
                instance.translate(x * (module.swingXFactor.get()), y * (module.swingXFactor.get()), z * (module.swingXFactor.get()));
                return;
            }
        }

        instance.translate(x, y, z);
    }

    // HandView - Swing Modifier
    @ModifyConstant(method = "applyItemArmTransform", constant = @Constant(floatValue = -0.6f))
    public float modifySwingYConstant(float constant) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.swingAnimation.get()) {
                return module.swingYConstant.get();
            }
        }

        return constant;
    }

    // HandView - Attack Modifier
    @Redirect(method = "applyItemArmAttackTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F", ordinal = 0))
    public float modifyAttackMagicNumber0(float f) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.attackAnimation.get()) {
                return f * module.attackMn0.get();
            }
        }

        return f;
    }

    // HandView - Attack Modifier
    @Redirect(method = "applyItemArmAttackTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F", ordinal = 1))
    public float modifyAttackMagicNumber1(float f) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.attackAnimation.get()) {
                return f * module.attackMn1.get();
            }
        }

        return f;
    }

    // HandView - Attack Modifier
    @ModifyConstant(method = "applyItemArmAttackTransform", constant = @Constant(floatValue = 45.0f, ordinal = 0))
    public float modifyAttackConst0(float constant) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.swingAnimation.get()) {
                return module.attackConst0.get();
            }
        }

        return constant;
    }

    // HandView - Attack Modifier
    @ModifyConstant(method = "applyItemArmAttackTransform", constant = @Constant(floatValue = -20.0f, ordinal = 0))
    public float modifyAttackConst1(float constant) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.swingAnimation.get()) {
                return module.attackConst1.get();
            }
        }

        return constant;
    }

    // HandView - Attack Modifier
    @ModifyConstant(method = "applyItemArmAttackTransform", constant = @Constant(floatValue = -20.0f, ordinal = 1))
    public float modifyAttackConst2(float constant) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.swingAnimation.get()) {
                return module.attackConst2.get();
            }
        }

        return constant;
    }

    // HandView - Attack Modifier
    @ModifyConstant(method = "applyItemArmAttackTransform", constant = @Constant(floatValue = -80.0f, ordinal = 0))
    public float modifyAttackConst3(float constant) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.swingAnimation.get()) {
                return module.attackConst3.get();
            }
        }

        return constant;
    }

    // HandView - Attack Modifier
    @ModifyConstant(method = "applyItemArmAttackTransform", constant = @Constant(floatValue = -45.0f, ordinal = 0))
    public float modifyAttackConst4(float constant) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.swingAnimation.get()) {
                return module.attackConst4.get();
            }
        }

        return constant;
    }

    // HandView - Scaling
    @Inject(method = "applyItemArmTransform", at = @At(value = "TAIL"))
    public void scaleHands(PoseStack poseStack, HumanoidArm humanoidArm, float f, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.mainHand.get() && humanoidArm == HumanoidArm.RIGHT) {
                module.modifyMainHandStack(poseStack);
            } else if (module.offhand.get()) {
                module.modifyOffhandStack(poseStack);
            }
        }
    }
}
