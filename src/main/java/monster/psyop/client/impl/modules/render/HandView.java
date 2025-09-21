package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemUseAnimation;
import org.joml.Quaternionf;

public class HandView extends Module {
    public BoolSetting noHurt = new BoolSetting.Builder()
            .name("no-hurt")
            .description("Prevents bob-hurting.")
            .defaultTo(true)
            .addTo(coreGroup);
    public GroupedSettings animationsGroup = addGroup(new GroupedSettings("animations", "Modifications to animations."));
    public BoolSetting blockAnimation = new BoolSetting.Builder()
            .name("block")
            .description("Enable/Disable vanilla block placing.")
            .defaultTo(true)
            .addTo(animationsGroup);
    public BoolSetting attackAnimation = new BoolSetting.Builder()
            .name("attack")
            .description("Modify attacking animations.")
            .defaultTo(false)
            .addTo(animationsGroup);
    public FloatSetting attackMn0 = new FloatSetting.Builder()
            .name("attack-mn-0")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> attackAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting attackMn1 = new FloatSetting.Builder()
            .name("attack-mn-1")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> attackAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting attackConst0 = new FloatSetting.Builder()
            .name("attack-const-0")
            .defaultTo(45.0f)
            .range(-180f, 180f)
            .visible((v) -> attackAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting attackConst1 = new FloatSetting.Builder()
            .name("attack-const-1")
            .defaultTo(-20.0f)
            .range(-180f, 180f)
            .visible((v) -> attackAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting attackConst2 = new FloatSetting.Builder()
            .name("attack-const-2")
            .defaultTo(-20.0f)
            .range(-180f, 180f)
            .visible((v) -> attackAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting attackConst3 = new FloatSetting.Builder()
            .name("attack-const-3")
            .defaultTo(-80.0f)
            .range(-180f, 180f)
            .visible((v) -> attackAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting attackConst4 = new FloatSetting.Builder()
            .name("attack-const-4")
            .defaultTo(-45.0f)
            .range(-180f, 180f)
            .visible((v) -> attackAnimation.get())
            .addTo(animationsGroup);
    public BoolSetting swingAnimation = new BoolSetting.Builder()
            .name("swing")
            .description("Modify swinging animations.")
            .defaultTo(false)
            .addTo(animationsGroup);
    public FloatSetting swingYConstant = new FloatSetting.Builder()
            .name("swing-y-const")
            .defaultTo(-0.6f)
            .range(-2.0f, 2.0f)
            .visible((v) -> swingAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting swingXFactor = new FloatSetting.Builder()
            .name("swing-x")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> swingAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting swingYFactor = new FloatSetting.Builder()
            .name("swing-y")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> swingAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting swingZFactor = new FloatSetting.Builder()
            .name("swing-z")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> swingAnimation.get())
            .addTo(animationsGroup);
    public BoolSetting eatingAnimation = new BoolSetting.Builder()
            .name("eating")
            .description("Enable/Disable vanilla eating.")
            .defaultTo(true)
            .addTo(animationsGroup);
    public BoolSetting customEatingAnimation = new BoolSetting.Builder()
            .name("custom-eating")
            .description("Modifies the eating animation to a custom one.")
            .defaultTo(true)
            .visible((v) -> !eatingAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting eatingTickFactor = new FloatSetting.Builder()
            .name("eating-tick")
            .defaultTo(0.20f)
            .range(-2.0f, 2.0f)
            .visible((v) -> !eatingAnimation.get() && customEatingAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting eatingXFactor = new FloatSetting.Builder()
            .name("eating-x")
            .defaultTo(0.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> !eatingAnimation.get() && customEatingAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting eatingYFactor = new FloatSetting.Builder()
            .name("eating-y")
            .defaultTo(-0.246f)
            .range(-2.0f, 2.0f)
            .visible((v) -> !eatingAnimation.get() && customEatingAnimation.get())
            .addTo(animationsGroup);
    public FloatSetting eatingZFactor = new FloatSetting.Builder()
            .name("eating-z")
            .defaultTo(0.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> !eatingAnimation.get() && customEatingAnimation.get())
            .addTo(animationsGroup);
    public GroupedSettings mainHandGroup = addGroup(new GroupedSettings("main-hand", "Modifications for the Main Hand"));
    public BoolSetting mainHand = new BoolSetting.Builder()
            .name("enable")
            .description("Modify the Main Hand.")
            .defaultTo(true)
            .addTo(mainHandGroup);
    public BoolSetting mainHandShouldMultiY = new BoolSetting.Builder()
            .name("should-multi-y")
            .description("Multiply the main hand Y.")
            .defaultTo(false)
            .addTo(mainHandGroup);
    public FloatSetting mainHandMultiY = new FloatSetting.Builder()
            .name("multi-y")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> mainHandShouldMultiY.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandShouldOffsetX = new BoolSetting.Builder()
            .name("should-offset-x")
            .description("Add an offset to the main hand X.")
            .defaultTo(false)
            .addTo(mainHandGroup);
    public FloatSetting mainHandOffsetX = new FloatSetting.Builder()
            .name("offset-x")
            .defaultTo(0.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> mainHandShouldOffsetX.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandShouldOffsetY = new BoolSetting.Builder()
            .name("should-offset-y")
            .description("Add an offset to the main hand Y.")
            .defaultTo(false)
            .addTo(mainHandGroup);
    public FloatSetting mainHandOffsetY = new FloatSetting.Builder()
            .name("offset-y")
            .defaultTo(0.04f)
            .range(-2.0f, 2.0f)
            .visible((v) -> mainHandShouldOffsetY.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandShouldOffsetZ = new BoolSetting.Builder()
            .name("should-offset-z")
            .description("Add an offset to the main hand Z.")
            .defaultTo(false)
            .addTo(mainHandGroup);
    public FloatSetting mainHandOffsetZ = new FloatSetting.Builder()
            .name("offset-z")
            .defaultTo(0.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> mainHandShouldOffsetZ.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandShouldScale = new BoolSetting.Builder()
            .name("should-scale")
            .description("Scale the main hand.")
            .defaultTo(false)
            .addTo(mainHandGroup);
    public FloatSetting mainHandScaleX = new FloatSetting.Builder()
            .name("scale-x")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> mainHandShouldScale.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandScaleY = new FloatSetting.Builder()
            .name("scale-y")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> mainHandShouldScale.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandScaleZ = new FloatSetting.Builder()
            .name("scale-z")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> mainHandShouldScale.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandRotate = new BoolSetting.Builder()
            .name("should-rotate")
            .description("Rotate the main hand.")
            .defaultTo(false)
            .addTo(mainHandGroup);
    public FloatSetting mainHandRotateX = new FloatSetting.Builder()
            .name("rot-x")
            .defaultTo(1.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> mainHandRotate.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandRotateY = new FloatSetting.Builder()
            .name("rot-y")
            .defaultTo(1.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> mainHandRotate.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandRotateZ = new FloatSetting.Builder()
            .name("rot-z")
            .defaultTo(1.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> mainHandRotate.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandSpin = new BoolSetting.Builder()
            .name("should-spin")
            .description("Spin the main hand.")
            .defaultTo(false)
            .addTo(mainHandGroup);
    public BoolSetting mainHandSpinFPS = new BoolSetting.Builder()
            .name("fps-bound")
            .description("Ties spinning to fps, otherwise tps.")
            .defaultTo(false)
            .visible((v) -> mainHandSpin.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandTieToHealth = new BoolSetting.Builder()
            .name("tie-to-health")
            .description("Ties spinning speed to health.")
            .defaultTo(false)
            .visible((v) -> mainHandSpin.get())
            .addTo(mainHandGroup);
    public BoolSetting mainHandReverseHealth = new BoolSetting.Builder()
            .name("reverse-health")
            .description("Makes it spin faster on lower health.")
            .defaultTo(false)
            .visible((v) -> mainHandSpin.get() && mainHandTieToHealth.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandHealthMulti = new FloatSetting.Builder()
            .name("health-multi")
            .defaultTo(0.084f)
            .range(0.0f, 1.0f)
            .visible((v) -> mainHandSpin.get() && mainHandTieToHealth.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandXSpinFactor = new FloatSetting.Builder()
            .name("spin-x")
            .defaultTo(0.2f)
            .range(-180.0f, 180.0f)
            .visible((v) -> mainHandSpin.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandYSpinFactor = new FloatSetting.Builder()
            .name("spin-y")
            .defaultTo(0.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> mainHandSpin.get())
            .addTo(mainHandGroup);
    public FloatSetting mainHandZSpinFactor = new FloatSetting.Builder()
            .name("spin-z")
            .defaultTo(0.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> mainHandSpin.get())
            .addTo(mainHandGroup);
    public GroupedSettings offhandGroup = addGroup(new GroupedSettings("offhand", "Modifications for the Offhand"));
    public BoolSetting offhand = new BoolSetting.Builder()
            .name("enable")
            .description("Modify the offhand.")
            .defaultTo(true)
            .addTo(offhandGroup);
    public BoolSetting offhandShouldMultiY = new BoolSetting.Builder()
            .name("should-multi-y")
            .description("Multiply the offhand Y.")
            .defaultTo(false)
            .addTo(offhandGroup);
    public FloatSetting offhandMultiY = new FloatSetting.Builder()
            .name("multi-y")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> offhandShouldMultiY.get())
            .addTo(offhandGroup);
    public BoolSetting offhandShouldOffsetX = new BoolSetting.Builder()
            .name("should-offset-x")
            .description("Add an offset to the offhand X.")
            .defaultTo(false)
            .addTo(offhandGroup);
    public FloatSetting offhandOffsetX = new FloatSetting.Builder()
            .name("offset-x")
            .defaultTo(0.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> offhandShouldOffsetX.get())
            .addTo(offhandGroup);
    public BoolSetting offhandShouldOffsetY = new BoolSetting.Builder()
            .name("should-offset-y")
            .description("Add an offset to the offhand Y.")
            .defaultTo(false)
            .addTo(offhandGroup);
    public FloatSetting offhandOffsetY = new FloatSetting.Builder()
            .name("offset-y")
            .defaultTo(0.04f)
            .range(-2.0f, 2.0f)
            .visible((v) -> offhandShouldOffsetY.get())
            .addTo(offhandGroup);
    public BoolSetting offhandShouldOffsetZ = new BoolSetting.Builder()
            .name("should-offset-z")
            .description("Add an offset to the offhand Z.")
            .defaultTo(false)
            .addTo(offhandGroup);
    public FloatSetting offhandOffsetZ = new FloatSetting.Builder()
            .name("offset-z")
            .defaultTo(0.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> offhandShouldOffsetZ.get())
            .addTo(offhandGroup);
    public BoolSetting offhandShouldScale = new BoolSetting.Builder()
            .name("should-scale")
            .description("Scale the offhand.")
            .defaultTo(false)
            .addTo(offhandGroup);
    public FloatSetting offhandScaleX = new FloatSetting.Builder()
            .name("scale-x")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> offhandShouldScale.get())
            .addTo(offhandGroup);
    public FloatSetting offhandScaleY = new FloatSetting.Builder()
            .name("scale-y")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> offhandShouldScale.get())
            .addTo(offhandGroup);
    public FloatSetting offhandScaleZ = new FloatSetting.Builder()
            .name("scale-z")
            .defaultTo(1.0f)
            .range(-2.0f, 2.0f)
            .visible((v) -> offhandShouldScale.get())
            .addTo(offhandGroup);
    public BoolSetting offhandRotate = new BoolSetting.Builder()
            .name("should-rotate")
            .description("Rotate the offhand.")
            .defaultTo(false)
            .addTo(offhandGroup);
    public FloatSetting offhandRotateX = new FloatSetting.Builder()
            .name("rot-x")
            .defaultTo(1.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> offhandRotate.get())
            .addTo(offhandGroup);
    public FloatSetting offhandRotateY = new FloatSetting.Builder()
            .name("rot-y")
            .defaultTo(1.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> offhandRotate.get())
            .addTo(offhandGroup);
    public FloatSetting offhandRotateZ = new FloatSetting.Builder()
            .name("rot-z")
            .defaultTo(1.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> offhandRotate.get())
            .addTo(offhandGroup);
    public BoolSetting offhandSpin = new BoolSetting.Builder()
            .name("should-spin")
            .description("Spin the offhand.")
            .defaultTo(false)
            .addTo(offhandGroup);
    public BoolSetting offhandSpinFPS = new BoolSetting.Builder()
            .name("fps-bound")
            .description("Ties spinning to fps, otherwise tps.")
            .defaultTo(false)
            .visible((v) -> offhandSpin.get())
            .addTo(offhandGroup);
    public BoolSetting offhandTieToHealth = new BoolSetting.Builder()
            .name("tie-to-health")
            .description("Ties spinning speed to health.")
            .defaultTo(false)
            .visible((v) -> offhandSpin.get())
            .addTo(offhandGroup);
    public BoolSetting offhandReverseHealth = new BoolSetting.Builder()
            .name("reverse-health")
            .description("Makes it spin faster on lower health.")
            .defaultTo(false)
            .visible((v) -> offhandSpin.get() && offhandTieToHealth.get())
            .addTo(offhandGroup);
    public FloatSetting offhandHealthMulti = new FloatSetting.Builder()
            .name("health-multi")
            .defaultTo(0.084f)
            .range(0.0f, 1.0f)
            .visible((v) -> offhandSpin.get() && offhandTieToHealth.get())
            .addTo(offhandGroup);
    public FloatSetting offhandXSpinFactor = new FloatSetting.Builder()
            .name("spin-x")
            .defaultTo(0.2f)
            .range(-180.0f, 180.0f)
            .visible((v) -> offhandSpin.get())
            .addTo(offhandGroup);
    public FloatSetting offhandYSpinFactor = new FloatSetting.Builder()
            .name("spin-y")
            .defaultTo(0.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> offhandSpin.get())
            .addTo(offhandGroup);
    public FloatSetting offhandZSpinFactor = new FloatSetting.Builder()
            .name("spin-z")
            .defaultTo(0.0f)
            .range(-180.0f, 180.0f)
            .visible((v) -> offhandSpin.get())
            .addTo(offhandGroup);

    private float mainHandXSpin = 0f;
    private float mainHandYSpin = 0f;
    private float mainHandZSpin = 0f;
    private float offhandXSpin = 0f;
    private float offhandYSpin = 0f;
    private float offhandZSpin = 0f;

    public HandView() {
        super(Categories.RENDER, "hand-view", "Allows for modifying how hands are rendered.");
    }

    @Override
    public void update() {
        if (mainHand.get() && mainHandSpin.get() && !mainHandSpinFPS.get()) {
            mainHandXSpin = Mth.wrapDegrees(mainHandXSpin + (mainHandXSpinFactor.get() * getSpinMulti(false)));
            mainHandYSpin = Mth.wrapDegrees(mainHandYSpin + (mainHandYSpinFactor.get() * getSpinMulti(false)));
            mainHandZSpin = Mth.wrapDegrees(mainHandZSpin + (mainHandZSpinFactor.get() * getSpinMulti(false)));
        }

        if (offhand.get() && offhandSpin.get() && !offhandSpinFPS.get()) {
            offhandXSpin = Mth.wrapDegrees(offhandXSpin + (offhandXSpinFactor.get() * getSpinMulti(true)));
            offhandYSpin = Mth.wrapDegrees(offhandYSpin + (offhandYSpinFactor.get() * getSpinMulti(true)));
            offhandZSpin = Mth.wrapDegrees(offhandZSpin + (offhandZSpinFactor.get() * getSpinMulti(true)));
        }
    }

    public float getOffhandY(float tick, float prior, float cur) {
        float y = Mth.lerp(tick, offhandShouldOffsetY.get() ? prior + offhandOffsetY.get() : prior, offhandShouldOffsetY.get() ? cur + offhandOffsetY.get() : cur);

        if (offhandShouldMultiY.get()) {
            y *= offhandMultiY.get();
        }

        return y;
    }

    public float getMainHandY(float tick, float prior, float cur) {
        float y = Mth.lerp(tick, mainHandShouldOffsetY.get() ? prior + mainHandOffsetY.get() : prior, mainHandShouldOffsetY.get() ? cur + mainHandOffsetY.get() : cur);

        if (mainHandShouldMultiY.get()) {
            y *= mainHandMultiY.get();
        }

        return y;
    }

    public void modifyMainHandStack(PoseStack stack) {
        if (mainHandShouldScale.get()) {
            stack.scale(mainHandScaleX.get(), mainHandScaleY.get(), mainHandScaleZ.get());
        }

        stack.translate(mainHandShouldOffsetX.get() ? mainHandOffsetX.get() : 0.0f, 0.0f, mainHandShouldOffsetZ.get() ? mainHandOffsetZ.get() : 0.0f);

        if (mainHandRotate.get()) {
            stack.mulPose(new Quaternionf().rotateYXZ((float) (mainHandRotateY.get() * (Math.PI / 180)), (float) (mainHandRotateX.get() * (Math.PI / 180)), (float) (mainHandRotateZ.get() * (Math.PI / 180))));
        }

        if (mainHandSpin.get()) {
            if (mainHandSpinFPS.get()) {
                mainHandXSpin = Mth.wrapDegrees(mainHandXSpin + (mainHandXSpinFactor.get() * getSpinMulti(false)));
                mainHandYSpin = Mth.wrapDegrees(mainHandYSpin + (mainHandYSpinFactor.get() * getSpinMulti(false)));
                mainHandZSpin = Mth.wrapDegrees(mainHandZSpin + (mainHandZSpinFactor.get() * getSpinMulti(false)));
            }

            stack.mulPose(new Quaternionf().rotateYXZ((float) (mainHandYSpin * (Math.PI / 180)), (float) (mainHandXSpin * (Math.PI / 180)), (float) (mainHandZSpin * (Math.PI / 180))));
        }

        if (MC.player.getUsedItemHand() == InteractionHand.MAIN_HAND) handleAnimations(stack);
    }

    public void modifyOffhandStack(PoseStack stack) {
        if (offhandShouldScale.get()) {
            stack.scale(offhandScaleX.get(), offhandScaleY.get(), offhandScaleZ.get());
        }

        stack.translate(offhandShouldOffsetX.get() ? offhandOffsetX.get() : 0.0f, 0.0f, offhandShouldOffsetZ.get() ? offhandOffsetZ.get() : 0.0f);

        if (offhandRotate.get()) {
            stack.mulPose(new Quaternionf().rotateYXZ((float) (offhandRotateY.get() * (Math.PI / 180)), (float) (offhandRotateX.get() * (Math.PI / 180)), ((float) (offhandRotateZ.get() * (Math.PI / 180)))));
        }

        if (offhandSpin.get()) {
            if (offhandSpinFPS.get()) {
                offhandXSpin = Mth.wrapDegrees(offhandXSpin + (offhandXSpinFactor.get() * getSpinMulti(true)));
                offhandYSpin = Mth.wrapDegrees(offhandYSpin + (offhandYSpinFactor.get() * getSpinMulti(true)));
                offhandZSpin = Mth.wrapDegrees(offhandZSpin + (offhandZSpinFactor.get() * getSpinMulti(true)));
            }

            stack.mulPose(new Quaternionf().rotateYXZ((float) (offhandYSpin * (Math.PI / 180)), (float) (offhandXSpin * (Math.PI / 180)), (float) (offhandZSpin * (Math.PI / 180))));
        }

        if (MC.player.getUsedItemHand() == InteractionHand.OFF_HAND) handleAnimations(stack);
    }

    public void handleAnimations(PoseStack stack) {
        if (MC.player == null || !MC.player.isUsingItem()) {
            return;
        }

        switch (MC.player.getUseItem().getUseAnimation()) {
            case EAT, DRINK -> {
                if (!eatingAnimation.get() && customEatingAnimation.get()) {
                    animateEating(stack);
                }
            }
        }
    }

    public void animateEating(PoseStack stack) {
        if (MC.player == null || !MC.player.isUsingItem()) {
            return;
        }

        int handValue = MC.player.getUsedItemHand() == InteractionHand.MAIN_HAND ? 1 : -1;
        float eatingProgress = MC.player.getUseItemRemainingTicks() * eatingTickFactor.get();

        stack.translate(handValue * (eatingXFactor.get() * eatingProgress), eatingYFactor.get() * eatingProgress, handValue * (eatingZFactor.get() * eatingProgress));
    }

    public float getSpinMulti(boolean offhand) {
        float multi = 1.0f;

        if (MC.player == null) {
            return multi;
        }

        if (offhand) {
            if (offhandTieToHealth.get()) {
                if (offhandReverseHealth.get()) {
                    float health = Math.abs(MC.player.getHealth() - MC.player.getMaxHealth());

                    multi += health * offhandHealthMulti.get();
                } else {
                    multi += MC.player.getHealth() * offhandHealthMulti.get();
                }
            }
        } else {
            if (mainHandTieToHealth.get()) {
                if (mainHandReverseHealth.get()) {
                    float health = Math.abs(MC.player.getHealth() - MC.player.getMaxHealth());

                    multi += health * mainHandHealthMulti.get();
                } else {
                    multi += MC.player.getHealth() * mainHandHealthMulti.get();
                }
            }
        }

        return multi;
    }

    public boolean shouldCancelAnimation(ItemUseAnimation useAnimation) {
        return switch (useAnimation) {
            case BLOCK -> !blockAnimation.get();
            case EAT, DRINK -> !eatingAnimation.get();
            default -> false;
        };
    }
}
