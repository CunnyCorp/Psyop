package monster.psyop.client.impl.events.game;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.framework.events.Event;

public class OnRender extends Event {
    public VertexConsumer lines;
    public VertexConsumer quads;
    public PoseStack poseStack;

    public static OnRender INSTANCE = new OnRender();

    public OnRender() {
        super(false);
    }

    public static OnRender get(VertexConsumer lines, VertexConsumer quads, PoseStack poseStack) {
        INSTANCE.lines = lines;
        INSTANCE.quads = quads;
        INSTANCE.poseStack = poseStack;
        return INSTANCE;
    }
}
