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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class SpinBot extends Module {
    // Rotation settings
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

    // Activation settings
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

    // Delay settings
    public final GroupedSettings delayGroup = addGroup(new GroupedSettings("delay", "SpinBot delay settings."));
    public final IntSetting tickDelay = new IntSetting.Builder()
            .name("tick-delay")
            .description("Delay between rotation updates (ticks).")
            .defaultTo(1)
            .range(1, 20)
            .addTo(delayGroup);

    // Rotation storage - completely independent of client state
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
        // Reset state when enabled
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

        // Initialize rotations based on current player state if not already initialized
        if (!initialized) {
            initializeRotations();
            initialized = true;
        }

        // Apply tick delay
        tickCounter++;
        if (tickCounter < tickDelay.get()) {
            return;
        }
        tickCounter = 0;

        // Check activation conditions
        if (!shouldActivate()) return;

        // Update spin direction if needed
        updateSpinDirection();

        // Calculate new rotation
        updateRotation();

        // Send rotation packet with proper hand selection
        sendRotationPacket();
    }

    private void initializeRotations() {
        if (MC.player != null) {
            // Start from current player rotation but store independently
            currentYaw = MC.player.getYRot();
            currentPitch = MC.player.getXRot();
        } else {
            // Default values if player is null (shouldn't happen)
            currentYaw = 0.0f;
            currentPitch = 0.0f;
        }
    }

    private boolean shouldActivate() {
        // Check if we're holding food in either hand
        if (isHoldingFood(InteractionHand.MAIN_HAND) || isHoldingFood(InteractionHand.OFF_HAND)) {
            return false;
        }

        // Check if we should only activate when attacking
        if (onlyWhenAttacking.get() && !WorldUtils.isLookingAt(HitResult.Type.ENTITY)) {
            return false;
        }

        // Check if we require a target and there is one
        if (requireTarget.get()) {
            // Simple target check - you could expand this with more sophisticated targeting logic
            if (!hasValidTarget()) {
                return false;
            }
        }

        return true;
    }

    private boolean isHoldingFood(InteractionHand hand) {
        ItemStack item = MC.player.getItemInHand(hand);
        return item != null && item.getComponents().has(DataComponents.FOOD);
    }

    private boolean hasValidTarget() {
        // Simple implementation - check if we're looking at an entity
        // You could expand this with more sophisticated targeting logic
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
        // Update yaw (full rotation) - completely independent of client state
        currentYaw += rotationSpeed.get() * spinDirection;

        // Normalize yaw to stay within -180 to 180 range
        if (currentYaw > 180.0f) currentYaw -= 360.0f;
        if (currentYaw < -180.0f) currentYaw += 360.0f;

        // Update pitch based on settings
        if (randomizePitch.get()) {
            // Random pitch variation within specified range
            currentPitch = (float) ((Math.random() * 2 - 1) * pitchRange.get());
            currentPitch = Math.max(-90.0f, Math.min(90.0f, currentPitch));
        } else {
            // Keep pitch relatively stable with slight variation
            currentPitch = (float) (Math.sin(tickCounter * 0.1) * 5.0);
            currentPitch = Math.max(-90.0f, Math.min(90.0f, currentPitch));
        }
    }

    private void sendRotationPacket() {
        // Determine which hand to use (avoid food items)
        InteractionHand handToUse = determineHandToUse();

        // Send the rotation packet using our stored rotations
        PacketUtils.send(new ServerboundUseItemPacket(handToUse, 0, currentYaw, currentPitch));

        // Optionally update player's rotation visually (commented out to maintain independence)
        // MC.player.setYRot(currentYaw);
        // MC.player.setXRot(currentPitch);
    }

    private InteractionHand determineHandToUse() {
        // Prefer main hand if it's not food
        if (!isHoldingFood(InteractionHand.MAIN_HAND)) {
            return InteractionHand.MAIN_HAND;
        }

        // Use off hand if it's not food
        if (!isHoldingFood(InteractionHand.OFF_HAND)) {
            return InteractionHand.OFF_HAND;
        }

        // Default to main hand if both have food (shouldn't happen due to activation check)
        return InteractionHand.MAIN_HAND;
    }
}