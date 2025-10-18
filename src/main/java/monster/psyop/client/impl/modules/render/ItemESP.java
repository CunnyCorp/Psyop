package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ItemESP extends Module {
    public BoolSetting useList = new BoolSetting.Builder()
            .name("use-list")
            .defaultTo(false)
            .addTo(coreGroup);
    public ItemListSetting itemList = new ItemListSetting.Builder()
            .name("items")
            .defaultTo(new ArrayList<>(List.of(Items.DIAMOND)))
            .addTo(coreGroup);
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{1.0f, 0.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public BoolSetting pretty = new BoolSetting.Builder()
            .name("pretty")
            .defaultTo(false)
            .addTo(coreGroup);
    public FloatSetting fillOpacity = new FloatSetting.Builder()
            .name("fill-opacity")
            .defaultTo(0.5f)
            .range(0.1f, 1.0f)
            .addTo(coreGroup);
    public FloatSetting radius = new FloatSetting.Builder()
            .name("radius")
            .defaultTo(0.5f)
            .range(0.1f, 1.0f)
            .addTo(coreGroup);
    public IntSetting segments = new IntSetting.Builder()
            .name("segments")
            .defaultTo(24)
            .range(16, 32)
            .addTo(coreGroup);

    boolean goUp = true;
    float b = 0.0f;

    public ItemESP() {
        super(Categories.RENDER, "item-esp", "Draws 3D boxes around items.");
    }

    @Override
    public void update() {
        if (pretty.get()) {
            if (goUp) b += 0.01f;
            else b -= 0.01f;

            if (b >= 1.0f) {
                goUp = false;
            } else if (b <= 0.0f) {
                goUp = true;
            }
        }
    }

    @EventListener
    public void onRender(OnRender event) {
        PoseStack.Pose pose = event.poseStack.last();

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity.getType() == EntityType.ITEM) {
                ItemEntity item = (ItemEntity) entity;

                float cx = (float) (entity.position().x - camX);
                float cy = (float) ((entity.position().y + radius.get()) - camY);
                float cz = (float) (entity.position().z - camZ);

                float[] c;

                if (pretty.get()) {
                    c = new float[]{1.0f, 1.0f, b, fillOpacity.get()};
                } else {
                    c = new float[]{color.get()[0], color.get()[1], color.get()[2], fillOpacity.get()};
                }

                if (!useList.get() || itemList.value().contains(item.getItem().getItem())) {
                    Render3DUtil.drawSphereFaces(event.quads, pose, cx, cy, cz, radius.get(), segments.get(), c[0], c[1], c[2], c[3]);
                }
            }
        }
    }
}
