package monster.psyop.client.utility;

import lombok.Getter;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static monster.psyop.client.Psyop.MC;

public class EntityUtils {
    private static final List<EntityType<?>> collidable =
            List.of(EntityType.ITEM, EntityType.TRIDENT, EntityType.ARROW, EntityType.AREA_EFFECT_CLOUD);
    @Getter
    private static final List<Direction> horizontals =
            List.of(Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST);

    public static float getActualHealth() {
        assert MC.player != null;
        return MC.player.getHealth() + MC.player.getAbsorptionAmount();
    }

    public static boolean isPhased(LivingEntity entity) {
        BlockPos.MutableBlockPos mutableBlockPos = entity.blockPosition().mutable();

        if (BlockUtils.isNotAir(mutableBlockPos) && entity.isColliding(mutableBlockPos, BlockUtils.getState(mutableBlockPos))) {
            return true;
        }

        for (Direction dir : EntityUtils.horizontals) {
            mutableBlockPos.set(entity.blockPosition().getX() + dir.getStepX(), entity.blockPosition().getY(), entity.blockPosition().getZ() + dir.getStepZ());
            if (BlockUtils.isAir(mutableBlockPos)) continue;
            if (entity.isColliding(mutableBlockPos, BlockUtils.getState(mutableBlockPos))) {
                return true;
            }
        }

        return false;
    }

    // Why do we even need this????
    public static boolean isWithinRange(Entity entity) {
        return MC.player.getEyePosition().distanceTo(entity.getEyePosition()) <= MC.player.entityInteractionRange();
    }


    // For use in multi-ticking behavior, lmao?
    public static boolean isTouchingGround() {
        if (MC.player == null || MC.level == null) {
            return false;
        }

        // Account for slabs.
        if (BlockUtils.isNotAir(MC.player.getOnPos())) {
            double yS = MC.player.getBlockStateOn().getCollisionShape(MC.level, MC.player.getOnPos()).max(Direction.Axis.Y);

            // Phased ?
            if (yS == 1) {
                return true;
            }

            return MC.player.getY() - (Math.round(MC.player.getY()) + yS) < 0.02;
        }

        return MC.player.getY() - Math.round(MC.player.getY()) < 0.02;
    }

    public static TypeOfEntity getType(Entity entity) {
        if (MC.player == null) {
            return TypeOfEntity.UNKNOWN;
        }

        switch (BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath()) {
            case "creeper":
                if (((Creeper) entity).isIgnited()) {
                    return TypeOfEntity.EXPLOSION;
                }
            case "tnt":
                return TypeOfEntity.EXPLOSION_INANIMATE;
            case "end_crystal":
                return TypeOfEntity.EXPLOSION;
            case "llama":
                if (((Llama) entity).isAggressive()) return TypeOfEntity.HOSTILE;
                return TypeOfEntity.RIDEABLE;
            case "wolf":
                assert MC.player != null;
                if (((Wolf) entity).getOwner() != null && ((Wolf) entity).getOwner().getUUID() == MC.player.getUUID()) {
                    return TypeOfEntity.PET;
                }

                if (((Wolf) entity).getPersistentAngerTarget() == MC.player.getUUID()) {
                    return TypeOfEntity.HOSTILE;
                }

                return TypeOfEntity.NEUTRAL;
            case "cat":
                if (((Cat) entity).getOwnerReference() != null) return TypeOfEntity.PET;
                return TypeOfEntity.PASSIVE;
            case "enderman":
                if (((EnderMan) entity).isAggressive() && ((EnderMan) entity).getPersistentAngerTarget() == MC.player.getUUID()) {
                    return TypeOfEntity.HOSTILE;
                }
                return TypeOfEntity.NEUTRAL;
            case "pig":
                if (((Pig) entity).isSaddled()) return TypeOfEntity.RIDEABLE;
                return TypeOfEntity.PASSIVE;
            case "mule", "donkey", "skeleton_horse", "zombie_horse", "horse":
                if (((AbstractHorse) entity).getOwnerReference() != null) return TypeOfEntity.PET;
                return TypeOfEntity.RIDEABLE;
            case "minecart", "boat":
                return TypeOfEntity.RIDEABLE;
            default:
                if (!entity.isAlive()
                        && entity instanceof LivingEntity
                        && ((LivingEntity) entity).getHealth() <= 0) {
                    return TypeOfEntity.UNKNOWN;
                }
                if (entity instanceof Player) {
                    if (entity.getUUID().equals(MC.player.getUUID())) {
                        return TypeOfEntity.SELF;
                    }
                    return TypeOfEntity.PLAYER;
                }

                if (!entity.isAttackable()) {
                    return TypeOfEntity.INANIMATE;
                }

                if (entity.getType().getCategory().isFriendly()) {
                    return TypeOfEntity.PASSIVE;
                } else {
                    return TypeOfEntity.HOSTILE;
                }
        }
    }

    public static boolean canPlaceIn(Entity entity) {
        return collidable.contains(entity.getType()) || entity.isRemoved() || entity.isSpectator();
    }

    public enum TypeOfEntity {
        RIDEABLE,
        NEUTRAL,
        PASSIVE,
        HOSTILE,
        INANIMATE,
        EXPLOSION_INANIMATE,
        EXPLOSION,
        UNKNOWN,
        PLAYER,
        PET,
        SELF
    }
}
