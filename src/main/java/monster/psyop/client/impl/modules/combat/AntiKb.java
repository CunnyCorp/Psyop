package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.Liberty;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.MathUtils;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class AntiKb extends Module {
    public final BoolSetting noLiquid =
            new BoolSetting.Builder()
                    .name("no-liquid")
                    .description("Prevents motion in liquid.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final IntSetting minLiquidTime =
            new IntSetting.Builder()
                    .name("ticks-out-of-liquid")
                    .description("How many ticks must you be out of liquids.")
                    .defaultTo(4)
                    .range(0, 20)
                    .visible(intSetting -> !noLiquid.get())
                    .addTo(coreGroup);
    public final BoolSetting whileGliding =
            new BoolSetting.Builder()
                    .name("while-gliding")
                    .description("Prevent motion if elytra-gliding.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting grimMode =
            new BoolSetting.Builder()
                    .name("grim-mode")
                    .description("Disables motions from being re-set for grim.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting explosions =
            new BoolSetting.Builder()
                    .name("explosions")
                    .description("Modifies explosion knockback.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final FloatSetting horizontalMulti =
            new FloatSetting.Builder()
                    .name("horizontal-multi")
                    .description("What to multiply horizontal knockback by.")
                    .defaultTo(0f)
                    .range(0.0f, 1f)
                    .addTo(coreGroup);
    public final FloatSetting verticalMulti =
            new FloatSetting.Builder()
                    .name("vertical-multi")
                    .description("What to multiply vertical knockback by.")
                    .defaultTo(0.4f)
                    .range(0.0f, 1f)
                    .addTo(coreGroup);
    public final BoolSetting log =
            new BoolSetting.Builder()
                    .name("log")
                    .description("Logs when knockback is attempted to be set.")
                    .defaultTo(false)
                    .addTo(coreGroup);

    private int outOfLiquidTime = 0;

    public AntiKb() {
        super(Categories.COMBAT, "anti-kb", "Prevents/reduces knock-back.");
    }

    @EventListener
    public void onTickPost(OnTick.Post event) {
        if (!MC.player.isInLiquid() && !MC.player.isInWater()) {
            outOfLiquidTime++;
        } else {
            outOfLiquidTime = 0;
        }
    }

    @EventListener
    public void onPacketReceived(OnPacket.Received event) {
        if (skipCaring()) {
            return;
        }

        if (event.packet() instanceof ClientboundSetEntityMotionPacket packet) {
            assert MC.player != null;
            if (packet.getId() == MC.player.getId()) {
                Vec3 deltaMovement = MC.player.getDeltaMovement();

                double xVel = ((packet.getXa() - deltaMovement.x) * horizontalMulti.get()) + deltaMovement.x;
                double yVel = ((packet.getYa() - deltaMovement.y) * verticalMulti.get()) + deltaMovement.y;
                double zVel = ((packet.getXa() - deltaMovement.z) * horizontalMulti.get()) + deltaMovement.z;

                if (log.get()) {
                    Liberty.log("KB: {}, {}, {} from {}, {}, {}", xVel, yVel, zVel, packet.getXa(), packet.getYa(), packet.getZa());
                }

                MC.player.lerpMotion(xVel, yVel, zVel);
                event.cancel();
            }
        } else if (event.packet() instanceof ClientboundExplodePacket packet && explosions.get()) {
            if (packet.playerKnockback().isEmpty()) {
                return;
            }

            Vec3 knockback = packet.playerKnockback().get();

            MathUtils.set(packet.playerKnockback().get(), knockback.x * horizontalMulti.get(), knockback.y * verticalMulti.get(), knockback.z * horizontalMulti.get());
        }
    }

    public boolean skipCaring() {
        if (!noLiquid.get() && (MC.player.isInLiquid() || MC.player.isInWater() || outOfLiquidTime < minLiquidTime.get())) {
            return true;
        }

        return !whileGliding.get() && MC.player.isFallFlying();
    }
}
