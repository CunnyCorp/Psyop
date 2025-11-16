package monster.psyop.client.impl.modules.world;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.phys.Vec3;

public class FastBreak extends Module {
    public GroupedSettings renderGroup = addGroup(new GroupedSettings("Rendering", "Renders visual feedback."));

    public FloatSetting breakMultiplier = new FloatSetting.Builder()
            .name("break-multiplier")
            .defaultTo(1.450f)
            .range(0.1f, 10f)
            .addTo(coreGroup);
    public BoolSetting instant = new BoolSetting.Builder()
            .name("instant")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting repeat = new BoolSetting.Builder()
            .name("repeat")
            .defaultTo(true)
            .visible(v -> instant.get())
            .addTo(coreGroup);
    public BoolSetting resetIfAir = new BoolSetting.Builder()
            .name("reset-if-air")
            .defaultTo(false)
            .visible(v -> instant.get())
            .addTo(coreGroup);

    public BoolSetting render = new BoolSetting.Builder()
            .name("render")
            .defaultTo(true)
            .addTo(renderGroup);
    public ColorSetting breakColor = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{0.5f, 0.0f, 0.3f, 0.5f})
            .addTo(renderGroup);

    public BlockPos blockPos = null;
    public Direction direction = Direction.UP;

    public FastBreak() {
        super(Categories.WORLD, "fast-break", "Allows you to quickly mine blocks.");
    }

    @EventListener
    public void onTick(OnTick.Post event) {
        if (instant.get() && repeat.get() && blockPos != null) {
            if (resetIfAir.get() && BlockUtils.isAir(blockPos)) {
                blockPos = null;
                return;
            }

            PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, blockPos.relative(Direction.UP, 1337), direction));
            PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));

        }
    }

    @EventListener
    public void onRender(OnRender event) {
        if (!render.get()) {
            return;
        }

        PoseStack.Pose pose = event.poseStack.last();

        if (blockPos != null && instant.get() && repeat.get()) {
            Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();

            Render3DUtil.drawBlockInner(event.quads, pose, blockPos, cam, 0f, breakColor.get()[0], breakColor.get()[1], breakColor.get()[2], breakColor.get()[3]);
            Render3DUtil.drawBlockOutline(event.lines, pose, blockPos, cam, 0f, breakColor.get()[0], breakColor.get()[1], breakColor.get()[2], breakColor.get()[3]);
        }
    }

    @EventListener
    public void onPacket(OnPacket.Sent event) {
        if (instant.get() && event.packet() instanceof ServerboundPlayerActionPacket packet) {
            if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().relative(Direction.UP, 1337), packet.getDirection()));
                PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, packet.getPos(), packet.getDirection()));

                if (repeat.get()) {
                    blockPos = packet.getPos();
                    direction = packet.getDirection();
                }
            }
        }
    }
}
