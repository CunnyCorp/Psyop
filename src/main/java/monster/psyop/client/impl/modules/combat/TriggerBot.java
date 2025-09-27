package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.WorldUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class TriggerBot extends Module {
    public final GroupedSettings delayGroup = addGroup(new GroupedSettings("delays", "Trigger Bot attack delays."));
    public final IntSetting minDelay =
            new IntSetting.Builder()
                    .name("min-delay")
                    .description("The minimum delay per attack")
                    .defaultTo(10)
                    .range(0, 40)
                    .addTo(delayGroup);
    public final IntSetting delayMinVariance =
            new IntSetting.Builder()
                    .name("min-variance")
                    .description("Minimum variance in delays.")
                    .defaultTo(0)
                    .range(0, 40)
                    .addTo(delayGroup);
    public final IntSetting delayMaxVariance =
            new IntSetting.Builder()
                    .name("max-variance")
                    .description("Maximum variance in delays, min variance + max variance.")
                    .defaultTo(5)
                    .range(0, 40)
                    .addTo(delayGroup);
    public final IntSetting hitDelay =
            new IntSetting.Builder()
                    .name("hit-delay")
                    .description("How many ticks to wait before sending a scheduled attack.")
                    .defaultTo(3)
                    .range(1, 40)
                    .addTo(delayGroup);

    public final GroupedSettings aimGroup = addGroup(new GroupedSettings("aim-assist", "Lock onto attacked entities."));
    public final BoolSetting aimAssist =
            new BoolSetting.Builder()
                    .name("aim-assist")
                    .description("Automatically aim at attacked entities.")
                    .defaultTo(true)
                    .addTo(aimGroup);
    public final BoolSetting onlyIfNotTargeted =
            new BoolSetting.Builder()
                    .name("no-target")
                    .description("Only aims if you don't have a target")
                    .defaultTo(true)
                    .addTo(aimGroup);
    public final BoolSetting autoLockOn =
            new BoolSetting.Builder()
                    .name("auto-lock-on")
                    .description("Assigns a target if you don't have one.")
                    .defaultTo(false)
                    .addTo(aimGroup);
    public final FloatSetting lockDistance =
            new FloatSetting.Builder()
                    .name("lock-distance")
                    .description("How far to lock onto entities from.")
                    .defaultTo(4.5f)
                    .range(1.0f, 12.0f)
                    .addTo(aimGroup);
    public final FloatSetting rotTolerance =
            new FloatSetting.Builder()
                    .name("rot-tolerance")
                    .description("Amount of leeway to rotate at, 0.5 or lower to disable")
                    .defaultTo(40f)
                    .range(0.0f, 180f)
                    .addTo(aimGroup);
    public final FloatSetting pitchTolerance =
            new FloatSetting.Builder()
                    .name("pitch-tolerance")
                    .description("Amount of leeway for pitch.")
                    .defaultTo(15f)
                    .range(0.0f, 180f)
                    .addTo(aimGroup);
    public final FloatSetting rotationSpeed =
            new FloatSetting.Builder()
                    .name("rotation-speed")
                    .description("Speed to rotate towards entities at.")
                    .defaultTo(0.32f)
                    .range(0.3f, 1.1f)
                    .addTo(aimGroup);
    public final FloatSetting exponentialFactor =
            new FloatSetting.Builder()
                    .name("exp-factor")
                    .description("Exponentially increase rotation smoothing based on aim-time.")
                    .defaultTo(0.15f)
                    .range(0.0f, 0.3f)
                    .addTo(aimGroup);
    /*public final BoolSetting onlyIfNotTargeted =
            new BoolSetting.Builder()
                    .name("no-target")
                    .description("Only aims if you don't have a target")
                    .defaultTo(true)
                    .addTo(aimGroup);

    public final FloatSetting yawPerTick =
            new FloatSetting.Builder()
                    .name("yaw-per-tick")
                    .description("Yaw per tick.")
                    .defaultTo(15f)
                    .range(10f, 160f)
                    .addTo(aimGroup);
    public final FloatSetting exponentialFactor =
            new FloatSetting.Builder()
                    .name("exp-factor")
                    .description("Exponentially increase rotation smoothing based on aim-time.")
                    .defaultTo(1.2f)
                    .range(0.0f, 20.0f)
                    .addTo(aimGroup);
    public final FloatSetting maxRotationSpeed =
            new FloatSetting.Builder()
                    .name("rotation-speed")
                    .description("Speed to rotate towards entities at.")
                    .defaultTo(0.32f)
                    .range(0.3f, 1.1f)
                    .addTo(aimGroup);*/

    public final GroupedSettings logicChecksGroup = addGroup(new GroupedSettings("logic-checks", "Checks to run before considering entities."));
    public final BoolSetting isEating =
            new BoolSetting.Builder()
                    .name("is-eating")
                    .description("Makes sure you aren't eating when you swing.")
                    .defaultTo(true)
                    .addTo(logicChecksGroup);
    public final BoolSetting requireWeapon =
            new BoolSetting.Builder()
                    .name("require-weapon")
                    .description("Must you be holding weapons to attack.")
                    .defaultTo(true)
                    .addTo(logicChecksGroup);
    public final ItemListSetting requiredWeapons =
            new ItemListSetting.Builder()
                    .name("weapons")
                    .description("The weapon you must hold before attacking.")
                    .filter(v -> v.components().has(DataComponents.WEAPON))
                    .defaultTo(List.of(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD))
                    .visible(v -> requireWeapon.get())
                    .addTo(logicChecksGroup);
    public final GroupedSettings entityChecksGroup = addGroup(new GroupedSettings("entity-checks", "Checks to run on entities before attacking."));
    public final EntityListSetting entityTypes =
            new EntityListSetting.Builder()
                    .name("entity-types")
                    .description("The types of entities to attack.")
                    .defaultTo(List.of(EntityType.PLAYER))
                    .addTo(entityChecksGroup);
    public final BoolSetting hitDelayCheck =
            new BoolSetting.Builder()
                    .name("hit-delay")
                    .description("Makes sure you can attack first.")
                    .defaultTo(true)
                    .addTo(entityChecksGroup);
    public final BoolSetting visibleCheck =
            new BoolSetting.Builder()
                    .name("is-visible")
                    .description("Makes sure a entity is visible before attacking.")
                    .defaultTo(true)
                    .addTo(entityChecksGroup);
    public final BoolSetting attackCheck =
            new BoolSetting.Builder()
                    .name("can-attack")
                    .description("Makes sure a entity can be attacked before attacking.")
                    .defaultTo(true)
                    .addTo(entityChecksGroup);
    public final BoolSetting healthCheck =
            new BoolSetting.Builder()
                    .name("healthy")
                    .description("Makes sure a entity is healthy before attacking.")
                    .defaultTo(true)
                    .addTo(entityChecksGroup);
    public final BoolSetting hopliteTeamCheck =
            new BoolSetting.Builder()
                    .name("hoplite-team")
                    .description("Makes sure you aren't in the same hoplite team.")
                    .defaultTo(true)
                    .addTo(entityChecksGroup);
    public final BoolSetting playerListCheck =
            new BoolSetting.Builder()
                    .name("player-listed")
                    .description("Makes sure a player is in the player list before attacking.")
                    .defaultTo(true)
                    .addTo(entityChecksGroup);
    public final BoolSetting playerFloatingCheck =
            new BoolSetting.Builder()
                    .name("no-player-float")
                    .description("Makes sure the player isn't floating/flying.")
                    .defaultTo(true)
                    .addTo(entityChecksGroup);

    private int delay = 0;
    private int aimTime = 0;
    private LivingEntity lastAttacked = null;
    private boolean isAwaitingAnAttack = false;
    private int queuedAttack = 0;

    public TriggerBot() {
        super(
                Categories.COMBAT,
                "trigger-bot",
                "Automatically hits an enemy when it's in the cross-hair.");
    }

    public static boolean isAttacking() {
        return Psyop.MODULES.get(TriggerBot.class).queuedAttack > 0;
    }

    @EventListener(priority = 1)
    public void onTick(OnTick.Pre event) {
        assert MC.player != null;

        delay--;

        queuedAttack--;

        boolean logicChecksPass = !isEating.get() || !MC.player.isUsingItem();

        if (requireWeapon.get() && !requiredWeapons.value().contains(MC.player.getMainHandItem().getItem())) {
            logicChecksPass = false;
        }

        if (queuedAttack > 0 || isAwaitingAnAttack) {
            logicChecksPass = false;
        }

        if (isAwaitingAnAttack && queuedAttack <= 0 && WorldUtils.isLookingAt(HitResult.Type.ENTITY)) {
            Entity entity = ((EntityHitResult) MC.hitResult).getEntity();

            if (entity == lastAttacked) {
                PacketUtils.send(
                        ServerboundInteractPacket.createAttackPacket(lastAttacked, MC.player.isShiftKeyDown()));
                PacketUtils.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                isAwaitingAnAttack = false;
            }
        }

        if (logicChecksPass && WorldUtils.isLookingAt(HitResult.Type.ENTITY)) {
            assert MC.hitResult != null;
            Entity entity = ((EntityHitResult) MC.hitResult).getEntity();

            if (entity instanceof LivingEntity livingEntity) {

                boolean shouldAttack = true;


                if (!entityTypes.value().contains(entity.getType())) {
                    shouldAttack = false;
                }

                //livingEntity.canBeSeenByAnyone() && mc.player.canAttack(livingEntity) && mc.player.getAttackStrengthScale(0.5f) >= 1.0f;

                if (healthCheck.get()) {
                    if (livingEntity.getHealth() <= 0 && livingEntity.getMaxHealth() <= 0) {
                        Psyop.log("Entity {} is not healthy?", livingEntity.getDisplayName().getString());
                        shouldAttack = false;
                    }
                }

                if (visibleCheck.get()) {
                    if (!livingEntity.canBeSeenByAnyone() || livingEntity.isInvisible()) {
                        Psyop.debug("Entity {} is not visible?", livingEntity.getDisplayName().getString());
                        shouldAttack = false;
                    }
                }

                if (attackCheck.get()) {
                    if (!MC.player.canAttack(livingEntity)) {
                        Psyop.log("Entity {} can not be attacked?", livingEntity.getDisplayName().getString());
                        shouldAttack = false;
                    }
                }

                if (hitDelayCheck.get()) {
                    if (MC.player.getAttackStrengthScale(0.5f) < 1.0f) {
                        Psyop.log("You are on attack cooldown?");
                        shouldAttack = false;
                    }
                }

                if (livingEntity instanceof Player player) {
                    Collection<UUID> playerList = MC.player.connection.getOnlinePlayerIds();

                    if (hopliteTeamCheck.get()) {
                        if (player.getTeam() != null && player.getTeam().getPlayers().contains(MC.player.getGameProfile().getName())) {
                            Psyop.log("Player {} is a team member.", player.getGameProfile().getName());
                            shouldAttack = false;
                        }
                    }

                    if (playerFloatingCheck.get()) {
                        if (player.getDeltaMovement().y == 0) {
                            if (player.getAbilities().flying) {
                                Psyop.log("Player {} is flying?", player.getGameProfile().getName());
                                shouldAttack = false;
                            } else if (!player.onGround() && !player.isFallFlying() && !player.jumping) {
                                Psyop.log("Player {} is in the air but isn't using elytra or jumping.", player.getGameProfile().getName());
                                shouldAttack = false;
                            }
                        }
                    }

                    if (playerListCheck.get() && !playerList.contains(player.getUUID())) {
                        Psyop.log("Player {}-{} does not exist? Maybe fake.", player.getStringUUID(), player.getGameProfile().getName());
                        shouldAttack = false;
                    }
                }


                if (shouldAttack && delay <= 0) {
                    MC.player.resetAttackStrengthTicker();
                    delay += minDelay.get() + 1;
                    delay += (delayMaxVariance.get() != 0 ?
                            Psyop.RANDOM.nextInt(
                                    delayMinVariance.get(),
                                    delayMinVariance.get() + delayMaxVariance.get())
                            : delayMinVariance.get());

                    lastAttacked = livingEntity;
                    queuedAttack = hitDelay.get();
                    isAwaitingAnAttack = true;
                }
            }
        }

        boolean shouldAimAssist = true;

        if (onlyIfNotTargeted.get()) {
            shouldAimAssist = !WorldUtils.isLookingAt(HitResult.Type.ENTITY);
        }

        if (aimAssist.get()) {
            if (shouldAimAssist) {
                if (lastAttacked != null && lastAttacked.isAlive() && lastAttacked.distanceTo(MC.player) < lockDistance.get()) {
                    Vec3 tracePos = lastAttacked.getEyePosition().add(0.15, -0.5f, 0.15);

                    aimTime++;

                    float playerPitch = Mth.clamp(MC.player.getXRot(), -90.0f, 90.0f);
                    float playerYaw = Mth.wrapDegrees(MC.player.getYRot());

                    float pitch = RotationUtils.getPitch(tracePos);
                    float yaw = RotationUtils.getYaw(tracePos);

                    Psyop.debug("Player: {}, {}; Desired: {}, {}", playerPitch, playerYaw, pitch, yaw);

                    if (rotTolerance.get() > 0.5 && (Math.abs(pitch - playerPitch) >= rotTolerance.get() || Math.abs(yaw - playerYaw) >= rotTolerance.get())) {
                        Psyop.log("Entity out of rot distance.");
                        return;
                    }

                    if (Math.abs(pitch - playerPitch) >= pitchTolerance.get()) {
                        playerPitch = Mth.rotLerp(rotationSpeed.get(), playerPitch, pitch);
                    }

                    playerYaw = Mth.rotLerp(rotationSpeed.get() + (exponentialFactor.get() * aimTime), playerYaw, yaw);

                    RotationUtils.rotate(playerPitch, playerYaw);
                } else {
                    aimTime = 0;

                    if (autoLockOn.get()) {
                        lastAttacked = getPossibleTarget();
                    }

                    Psyop.debug("Entity could not be aim-assisted.");
                }
            } else {
                aimTime = 0;
            }
        }
    }

    private LivingEntity getPossibleTarget() {
        List<LivingEntity> possibleTargets = new ArrayList<>();

        for (Entity entity : MC.player.level().getEntities(MC.player, new AABB(MC.player.position().x + lockDistance.get(), MC.player.position().y + lockDistance.get(), MC.player.position().z + lockDistance.get(), MC.player.position().x - lockDistance.get(), MC.player.position().y - lockDistance.get(), MC.player.position().z - lockDistance.get()))) {
            if (entity instanceof LivingEntity living) {
                if (!entityTypes.value().contains(living.getType())) {
                    continue;
                }

                if (healthCheck.get()) {
                    if (living.getHealth() <= 0 && living.getMaxHealth() <= 0) {
                        Psyop.log("Entity {} is not healthy?", living.getDisplayName().getString());
                        continue;
                    }
                }

                if (visibleCheck.get()) {
                    if (!living.canBeSeenByAnyone() || living.isInvisible()) {
                        Psyop.debug("Entity {} is not visible?", living.getDisplayName().getString());
                        continue;
                    }
                }

                if (living.distanceTo(MC.player) < lockDistance.get()) {
                    continue;
                }

                if (attackCheck.get()) {
                    if (!MC.player.canAttack(living)) {
                        Psyop.log("Entity {} can not be attacked?", living.getDisplayName().getString());
                        continue;
                    }
                }

                if (hitDelayCheck.get()) {
                    if (MC.player.getAttackStrengthScale(0.5f) < 1.0f) {
                        Psyop.log("You are on attack cooldown?");
                        continue;
                    }
                }

                if (living instanceof Player player) {
                    Collection<UUID> playerList = MC.player.connection.getOnlinePlayerIds();

                    if (hopliteTeamCheck.get()) {
                        if (player.getTeam() != null && player.getTeam().getPlayers().contains(MC.player.getGameProfile().getName())) {
                            Psyop.log("Player {} is a team member.", player.getGameProfile().getName());
                            continue;
                        }
                    }

                    if (playerFloatingCheck.get()) {
                        if (player.getDeltaMovement().y == 0) {
                            if (player.getAbilities().flying) {
                                Psyop.log("Player {} is flying?", player.getGameProfile().getName());
                                continue;
                            } else if (!player.onGround() && !player.isFallFlying() && !player.jumping) {
                                Psyop.log("Player {} is in the air but isn't using elytra or jumping.", player.getGameProfile().getName());
                                continue;
                            }
                        }
                    }

                    if (playerListCheck.get() && !playerList.contains(player.getUUID())) {
                        Psyop.log("Player {}-{} does not exist? Maybe fake.", player.getStringUUID(), player.getGameProfile().getName());
                        continue;
                    }
                }

                possibleTargets.add(living);
            }
        }

        possibleTargets.sort(Comparator.comparingDouble(v -> v.distanceTo(MC.player)));

        return possibleTargets.isEmpty() ? null : possibleTargets.get(0);
    }
}
