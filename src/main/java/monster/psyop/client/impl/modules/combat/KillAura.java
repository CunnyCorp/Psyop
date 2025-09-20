package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.EntityListSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
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

    private int delay = 0;

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

        PacketUtils.send(
                ServerboundInteractPacket.createAttackPacket(
                        entity, MC.player.isShiftKeyDown()));
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

            boolean isPlayer = entity instanceof Player;

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
