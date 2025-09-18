package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.Liberty;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.EntityListSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.RotationUtils;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {
    public final FloatSetting reach =
            new FloatSetting.Builder()
                    .name("reach")
                    .description("How far you can hit entities.")
                    .defaultTo(5f)
                    .range(1.5f, 5f)
                    .addTo(coreGroup);
    public final GroupedSettings checksGroup = addGroup(new GroupedSettings("checks", "Checks to run on entities before attacking."));
    public final EntityListSetting entityTypes =
            new EntityListSetting.Builder()
                    .name("entity-types")
                    .description("The types of entities to attack.")
                    .defaultTo(List.of(EntityType.PLAYER))
                    .addTo(checksGroup);
    public final BoolSetting noCustomNames =
            new BoolSetting.Builder()
                    .name("no-named")
                    .description("Don't attack entities with custom names.")
                    .defaultTo(false)
                    .addTo(checksGroup);
    public final BoolSetting visibleCheck =
            new BoolSetting.Builder()
                    .name("is-visible")
                    .description("Makes sure a entity is visible before attacking.")
                    .defaultTo(true)
                    .addTo(checksGroup);
    public final BoolSetting attackCheck =
            new BoolSetting.Builder()
                    .name("can-attack")
                    .description("Makes sure a entity can be attacked before attacking.")
                    .defaultTo(true)
                    .addTo(checksGroup);
    public final BoolSetting healthCheck =
            new BoolSetting.Builder()
                    .name("healthy")
                    .description("Makes sure a entity is healthy before attacking.")
                    .defaultTo(true)
                    .addTo(checksGroup);
    public final BoolSetting teamCheck =
            new BoolSetting.Builder()
                    .name("no-teamed")
                    .description("Makes sure you aren't in the same team.")
                    .defaultTo(false)
                    .addTo(checksGroup);
    public final BoolSetting playerListCheck =
            new BoolSetting.Builder()
                    .name("player-listed")
                    .description("Makes sure a player is in the player list before attacking.")
                    .defaultTo(true)
                    .addTo(checksGroup);
    public final BoolSetting playerFloatingCheck =
            new BoolSetting.Builder()
                    .name("no-player-float")
                    .description("Makes sure the player isn't floating/flying.")
                    .defaultTo(true)
                    .addTo(checksGroup);

    private int delay = 0;
    private int holdRotFor = 0;
    private boolean attackNextTick = false;
    private Entity lastEntity = null;

    public KillAura() {
        super(
                Categories.COMBAT,
                "kill-aura",
                "Automatically hits entities around the player.");
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        assert MC.player != null;

        delay--;

        if (holdRotFor > 0) {
            rotate(lastEntity);
            holdRotFor--;
            return;
        }

        if (MC.player.getAttackStrengthScale(0.5f) < 1.0f || delay > 0) {
            return;
        }

        List<Entity> entities = filterEntities();

        if (entities.isEmpty()) {
            return;
        }

        entities.sort(Comparator.comparingDouble((e) -> e.distanceToSqr(MC.player)));

        attackEntity(entities.get(0));
    }

    protected void attackEntity(Entity entity) {
        assert MC.player != null;
        MC.player.resetAttackStrengthTicker();

        lastEntity = entity;

        if (!attackNextTick) {
            PacketUtils.send(
                    ServerboundInteractPacket.createAttackPacket(
                            entity, MC.player.isShiftKeyDown()));
            if (!MC.player.swinging) {
                PacketUtils.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
    }

    protected void rotate(Entity entity) {
        float pitch = RotationUtils.getPitch(entity.getEyePosition());
        float yaw = RotationUtils.getYaw(entity.getEyePosition());

        Liberty.log("Rotating to: {}, {}", pitch, yaw);

        PacketUtils.rotate(pitch, yaw, true);
    }

    protected List<Entity> filterEntities() {
        ArrayList<Entity> filteredEntities = new ArrayList<>();

        if (MC.player == null) {
            return filteredEntities;
        }

        for (Entity entity : getNearbyEntitiesRaw()) {
            if (!entityTypes.value().contains(entity.getType())) {
                continue;
            }

            if (MC.player.getEyePosition().distanceTo(entity.position()) > reach.get()) {
                continue;
            }

            if (attackCheck.get() && !entity.isAttackable()) {
                continue;
            }

            if (visibleCheck.get() && (entity.isInvisible() || entity.isInvisibleTo(MC.player))) {
                continue;
            }

            boolean isPlayer = false;

            if (entity instanceof Player player) {
                isPlayer = true;

                if (playerListCheck.get() && !MC.player.connection.getOnlinePlayerIds().contains(player.getUUID())) {
                    continue;
                }

                if (teamCheck.get() && player.getTeam() != null && player.getTeam().getPlayers().contains(MC.player.getGameProfile().getName())) {
                    continue;
                }

                if (playerFloatingCheck.get()) {
                    if (player.getDeltaMovement().y == 0) {
                        if (player.getAbilities().flying) {
                            continue;
                        } else if (!player.onGround() && !player.isFallFlying() && !player.jumping) {
                            continue;
                        }
                    }
                }
            }

            if (entity instanceof LivingEntity living) {
                if (visibleCheck.get() && !living.canBeSeenByAnyone()) {
                    continue;
                }

                if (!isPlayer && noCustomNames.get() && living.hasCustomName()) {
                    continue;
                }

                if (healthCheck.get() && (living.getHealth() <= 0 && living.getMaxHealth() <= 0)) {
                    continue;
                }
            }

            filteredEntities.add(entity);
        }

        return filteredEntities;
    }

    protected List<Entity> getNearbyEntitiesRaw() {
        if (MC.player == null) {
            return new ArrayList<>();
        }

        return MC.player.level().getEntities(MC.player, new AABB(MC.player.getX() + 9, MC.player.getY() + 9, MC.player.getZ() + 9, MC.player.getX() - 9, MC.player.getY() - 9, MC.player.getZ() - 9));
    }
}
