package monster.psyop.client.framework.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;

public record ContractingBlock(BlockPos pos, long time, long keep) {
    public float getMinX() {
        return pos.getX() + getTimeMulti();
    }

    public float getMaxX() {
        return pos.getX() - getTimeMulti();
    }

    public float getMinY() {
        return pos.getY() + getTimeMulti();
    }

    public float getMaxY() {
        return pos.getY() - getTimeMulti();
    }

    public float getMinZ() {
        return pos.getZ() + getTimeMulti();
    }

    public float getMaxZ() {
        return pos.getZ() - getTimeMulti();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - time > keep;
    }

    public long getTimeMulti() {
        return (System.currentTimeMillis() - time) / keep;
    }

    public float getAlphaMulti() {
        long elapsed = System.currentTimeMillis() - time;
        return Math.max(0.0f, 1.0f - (float) elapsed / keep);
    }

    public void render(PoseStack.Pose pose, VertexConsumer quads, VertexConsumer lines, boolean outline, float[] color) {
        Render3DUtil.drawBoxFaces(quads, pose, getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ(), color[0], color[1], color[2], color[3] * getAlphaMulti());

        if (outline) {
            Render3DUtil.drawBoxEdges(lines, pose, getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ(), color[0], color[1], color[2], 1.0f);
        }
    }
}
