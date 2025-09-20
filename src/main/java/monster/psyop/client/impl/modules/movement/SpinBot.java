package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.WorldUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;

public class SpinBot extends Module {
    public final GroupedSettings rotationGroup = addGroup(new GroupedSettings("rotation", "SpinBot rotation settings."));
    public final FloatSetting rotationSpeed = new FloatSetting.Builder()
            .name("rotation-speed")
            .description("How fast to spin (degrees per tick).")
            .defaultTo(10.0f)
            .range(0.1f, 45.0f)
            .addTo(rotationGroup);
    public final BoolSetting randomizeDirection = new BoolSetting.Builder()
            .name("randomize-direction")
            .description("Randomly change spin direction periodically.")
            .defaultTo(true)
            .addTo(rotationGroup);
    public final IntSetting directionChangeInterval = new IntSetting.Builder()
            .name("direction-change-interval")
            .description("How often to change spin direction (ticks).")
            .defaultTo(100)
            .range(10, 500)
            .visible(v -> randomizeDirection.get())
            .addTo(rotationGroup);
    public final BoolSetting randomizePitch = new BoolSetting.Builder()
            .name("randomize-pitch")
            .description("Randomize pitch instead of keeping it stable.")
            .defaultTo(false)
            .addTo(rotationGroup);
    public final FloatSetting pitchRange = new FloatSetting.Builder()
            .name("pitch-range")
            .description("Maximum pitch variation when randomized.")
            .defaultTo(30.0f)
            .range(0.0f, 90.0f)
            .visible(v -> randomizePitch.get())
            .addTo(rotationGroup);

    public final GroupedSettings activationGroup = addGroup(new GroupedSettings("activation", "When to activate SpinBot."));
    public final BoolSetting onlyWhenAttacking = new BoolSetting.Builder()
            .name("only-when-attacking")
            .description("Only activate when attacking an entity.")
            .defaultTo(false)
            .addTo(activationGroup);
    public final BoolSetting requireTarget = new BoolSetting.Builder()
            .name("require-target")
            .description("Only activate when a valid target is in range.")
            .defaultTo(true)
            .addTo(activationGroup);

    public final GroupedSettings delayGroup = addGroup(new GroupedSettings("delay", "SpinBot delay settings."));
    public final IntSetting tickDelay = new IntSetting.Builder()
            .name("tick-delay")
            .description("Delay between rotation updates (ticks).")
            .defaultTo(1)
            .range(1, 20)
            .addTo(delayGroup);

    private float currentYaw = 0.0f;
    private float currentPitch = 0.0f;
    private int spinDirection = 1;
    private int directionChangeCounter = 0;
    private int tickCounter = 0;
    private boolean initialized = false;

    public SpinBot() {
        super(
                Categories.MOVEMENT,
                "spin-bot",
                "Spins your character rapidly to make aiming more difficult.");
    }

    @Override
    public void enabled() {
        super.enabled();
        initialized = false;
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        spinDirection = 1;
        directionChangeCounter = 0;
        tickCounter = 0;
    }

    @EventListener
    public void onTick(OnTick.Post event) {
        if (MC.player == null || MC.level == null || MC.player.isDeadOrDying()) return;

        if (!initialized) {
            initializeRotations();
            initialized = true;
        }

        tickCounter++;
        if (tickCounter < tickDelay.get()) {
            return;
        }
        tickCounter = 0;

        if (!shouldActivate()) return;

        updateSpinDirection();
        updateRotation();
        sendRotationPacket();
    }

    private void initializeRotations() {
        if (MC.player != null) {
            currentYaw = MC.player.getYRot();
            currentPitch = MC.player.getXRot();
        } else {
            currentYaw = 0.0f;
            currentPitch = 0.0f;
        }
    }

    private boolean shouldActivate() {
        if (isHoldingUsable(InteractionHand.MAIN_HAND) || isHoldingUsable(InteractionHand.OFF_HAND)) {
            return false;
        }

        if (onlyWhenAttacking.get() && !WorldUtils.isLookingAt(HitResult.Type.ENTITY)) {
            return false;
        }

        if (requireTarget.get()) {
            if (!hasValidTarget()) {
                return false;
            }
        }

        return true;
    }

    private boolean isHoldingUsable(InteractionHand hand) {
        ItemStack item = MC.player.getItemInHand(hand);

        return item != null && item.getComponents().has(DataComponents.CONSUMABLE) || isUsable(item.getItem());
    }

    private boolean isUsable(Item item) {
        return item == Items.BOW || item == Items.CROSSBOW || item == Items.EGG || item == Items.SNOWBALL || item == Items.BLUE_EGG || item == Items.ENDER_PEARL || item == Items.EXPERIENCE_BOTTLE || item == Items.FIREWORK_ROCKET || item == Items.ENDER_EYE;
    }

    private boolean hasValidTarget() {
        return WorldUtils.isLookingAt(HitResult.Type.ENTITY);
    }

    private void updateSpinDirection() {
        if (randomizeDirection.get()) {
            directionChangeCounter++;
            if (directionChangeCounter >= directionChangeInterval.get()) {
                spinDirection = Math.random() > 0.5 ? 1 : -1;
                directionChangeCounter = 0;
            }
        }
    }

    private void updateRotation() {
        currentYaw += rotationSpeed.get() * spinDirection;

        if (currentYaw > 180.0f) currentYaw -= 360.0f;
        if (currentYaw < -180.0f) currentYaw += 360.0f;

        if (randomizePitch.get()) {
            currentPitch = (float) ((Math.random() * 2 - 1) * pitchRange.get());
            currentPitch = Math.max(-90.0f, Math.min(90.0f, currentPitch));
        } else {
            currentPitch = (float) (Math.sin(tickCounter * 0.1) * 5.0);
            currentPitch = Math.max(-90.0f, Math.min(90.0f, currentPitch));
        }
    }

    private void sendRotationPacket() {
        InteractionHand handToUse = determineHandToUse();

        PacketUtils.send(new ServerboundUseItemPacket(handToUse, 0, currentYaw, currentPitch));
    }

    private InteractionHand determineHandToUse() {
        if (!isHoldingUsable(InteractionHand.MAIN_HAND)) {
            return InteractionHand.MAIN_HAND;
        }

        if (!isHoldingUsable(InteractionHand.OFF_HAND)) {
            return InteractionHand.OFF_HAND;
        }

        // Default to main hand if both have food (shouldn't happen due to activation check)
        return InteractionHand.MAIN_HAND;
    }
}