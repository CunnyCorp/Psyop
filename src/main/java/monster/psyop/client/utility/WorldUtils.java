package monster.psyop.client.utility;

import monster.psyop.client.Liberty;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

import static monster.psyop.client.Liberty.MC;

public class WorldUtils {
    private int checkTimer = 0;
    private static DimensionType dimensionType;

    public static void load() {
        Liberty.EVENT_HANDLER.add(new WorldUtils());


    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        if (MC.player == null) {
            return;
        }

        if (checkTimer <= 0) {
            checkTimer = 5;

            dimensionType = MC.player.level().dimensionType();
        } else {
            checkTimer--;
        }
    }

    public static boolean isLookingAt(HitResult.Type type) {
        if (MC.player == null || MC.hitResult == null) return false;

        return MC.hitResult.getType() == type;
    }

    public static boolean isLookingAtBlock(Block... block) {
        if (MC.player == null || MC.hitResult == null || !isLookingAt(HitResult.Type.BLOCK))
            return false;

        return BlockUtils.isBlock(((BlockHitResult) MC.hitResult).getBlockPos(), block);
    }

    public static boolean isLookingAtBlock(Predicate<Block> predicate) {
        if (MC.player == null || MC.hitResult == null || !isLookingAt(HitResult.Type.BLOCK))
            return false;

        return BlockUtils.testBlock(((BlockHitResult) MC.hitResult).getBlockPos(), predicate);
    }

    public static boolean isInNether() {
        if (MC.player == null || MC.level == null) {
            return false;
        }

        return dimensionType != null && dimensionType.respawnAnchorWorks();
    }

    public static boolean isInOW() {
        if (MC.player == null || MC.level == null) {
            return false;
        }

        return dimensionType != null && dimensionType.bedWorks();
    }

    public static boolean isInEnd() {
        if (MC.player == null || MC.level == null) {
            return false;
        }

        return dimensionType != null && !dimensionType.bedWorks() && !dimensionType.respawnAnchorWorks();
    }

    public static BlockHitResult getLookingAtBlock() {
        return (BlockHitResult) MC.hitResult;
    }

    public static boolean canSee(Entity entity) {
        if (MC.player == null || entity == null) return false;

        Vec3 playerEyes = MC.player.getEyePosition();
        Vec3 entityPos = entity.getBoundingBox().getCenter();

        // Check multiple points on the entity for better accuracy
        Vec3[] checkPoints = {
                entityPos,
                entityPos.add(0, entity.getBbHeight() / 2, 0), // Middle of entity
                entityPos.add(0, entity.getBbHeight() - 0.1, 0) // Top of entity
        };

        for (Vec3 point : checkPoints) {
            HitResult hitResult = MC.level.clip(new ClipContext(
                    playerEyes,
                    point,
                    ClipContext.Block.VISUAL, // Use VISUAL for better performance
                    ClipContext.Fluid.NONE,
                    MC.player
            ));

            if (hitResult.getType() == HitResult.Type.MISS) {
                return true;
            }

            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                if (entityHit.getEntity() == entity) {
                    return true;
                }
            }
        }

        return false;
    }
}
