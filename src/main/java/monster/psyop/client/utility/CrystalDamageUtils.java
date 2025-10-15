package monster.psyop.client.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static monster.psyop.client.Psyop.MC;

public class CrystalDamageUtils {

    private static final float CRYSTAL_POWER = 6.0F;

    public static float calculateDamage(LivingEntity target, Vec3 explosionPos) {
        if (target == null || explosionPos == null || MC.level == null) return 0.0F;

        double distance = Math.sqrt(target.distanceToSqr(explosionPos));
        double radius = CRYSTAL_POWER;
        double norm = distance / (radius * 2.0);
        if (norm > 1.0) return 0.0F;

        double exposure = getExposure(explosionPos, target);
        double impact = (1.0 - norm) * exposure;

        float raw = (float) (((impact * impact + impact) / 2.0) * 7.0 * radius + 1.0);
        if (raw <= 0.0F) return 0.0F;

        return applyReductions(target, raw);
    }

    public static float calculateDamageAtBase(LivingEntity target, BlockPos basePos) {
        if (basePos == null) return 0.0F;
        Vec3 center = new Vec3(basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 0.5);
        return calculateDamage(target, center);
    }

    private static float applyReductions(LivingEntity target, float damage) {
        float armor = target.getArmorValue();
        float toughness = (float) target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        float armorReduction = Math.min(20.0f, Math.max(armor / 5.0f, armor - (damage / (2.0f + (toughness / 4.0f))))) / 25.0f;
        float afterArmor = damage * (1.0f - armorReduction);

        return Math.max(0.0F, afterArmor);
    }

    private static double getExposure(Vec3 source, Entity entity) {
        AABB box = entity.getBoundingBox();

        double stepX = 1.0 / Math.max(1, (int) Math.ceil((box.maxX - box.minX) * 2.0));
        double stepY = 1.0 / Math.max(1, (int) Math.ceil((box.maxY - box.minY) * 2.0));
        double stepZ = 1.0 / Math.max(1, (int) Math.ceil((box.maxZ - box.minZ) * 2.0));

        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;

        double unblocked = 0.0;
        double total = 0.0;

        for (double x = 0.0; x <= 1.0; x += stepX) {
            for (double y = 0.0; y <= 1.0; y += stepY) {
                for (double z = 0.0; z <= 1.0; z += stepZ) {
                    double px = box.minX + (box.maxX - box.minX) * x + ox;
                    double py = box.minY + (box.maxY - box.minY) * y + oy;
                    double pz = box.minZ + (box.maxZ - box.minZ) * z + oz;

                    Vec3 sample = new Vec3(px, py, pz);
                    if (isUnobstructed(sample, source, entity)) {
                        unblocked++;
                    }
                    total++;
                }
            }
        }

        if (total == 0.0) return 0.0;
        return unblocked / total;
    }

    private static boolean isUnobstructed(Vec3 start, Vec3 end, Entity ignore) {
        if (MC.level == null) return false;
        HitResult res = MC.level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                ignore));
        return res.getType() == HitResult.Type.MISS;
    }
}
