package monster.psyop.client.impl.modules.world;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnScreen;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.StringUtils;
import monster.psyop.client.utility.WorldUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class SignFucker extends Module {
    public final IntSetting iterations =
            new IntSetting.Builder()
                    .name("iterations")
                    .description("Iterations to run per tick.")
                    .defaultTo(5)
                    .range(1, 20)
                    .addTo(coreGroup);
    public final IntSetting textLength =
            new IntSetting.Builder()
                    .name("length")
                    .description("The text length.")
                    .defaultTo(16)
                    .range(1, 384)
                    .addTo(coreGroup);
    public final BoolSetting uniChars =
            new BoolSetting.Builder()
                    .name("uni-chars")
                    .description("Use unicode characters instead of ascii.")
                    .defaultTo(true)
                    .addTo(coreGroup);

    public SignFucker() {
        super(Categories.WORLD, "sign-fucker", "Fucks with the sign you're looking at.");
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        if (WorldUtils.isLookingAtBlock((block) -> BlockUtils.getKey(block).endsWith("sign"))) {
            BlockHitResult hitResult = WorldUtils.getLookingAtBlock();
            for (int i = 0; i < iterations.value().get(); i++) {
                PacketUtils.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0));
                PacketUtils.send(
                        new ServerboundSignUpdatePacket(
                                hitResult.getBlockPos(),
                                Psyop.RANDOM.nextBoolean(),
                                StringUtils.randomText(textLength.value().get(), uniChars.get()),
                                StringUtils.randomText(textLength.value().get(), uniChars.get()),
                                StringUtils.randomText(textLength.value().get(), uniChars.get()),
                                StringUtils.randomText(textLength.value().get(), uniChars.get())));
            }
        }
    }

    @EventListener
    public void onScreenOpen(OnScreen.SignEdit event) {
        event.cancel();
    }
}
