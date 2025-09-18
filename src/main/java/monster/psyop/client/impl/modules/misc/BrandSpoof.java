package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.StringSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.StringUtils;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;

public class BrandSpoof extends Module {
    public final BoolSetting randomBuffer =
            new BoolSetting.Builder()
                    .name("random")
                    .description("Randomize the buffer text size.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final StringSetting stringText =
            new StringSetting.Builder()
                    .name("text")
                    .description("The brand text size.")
                    .visible((s) -> randomBuffer.value().get())
                    .defaultTo("Cunny")
                    .addTo(coreGroup);
    public final IntSetting bufferSize =
            new IntSetting.Builder()
                    .name("size")
                    .description("The buffer text size.")
                    .defaultTo(15)
                    .visible(s -> randomBuffer.isVisible())
                    .range(1, 256)
                    .addTo(coreGroup);

    public BrandSpoof() {
        super(Categories.MISC, "brand-spoof", "Spoofs your client brand.");
    }

    @EventListener(inGame = false)
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundCustomPayloadPacket packet) {
            if (packet.payload().type() == BrandPayload.TYPE) {
                event.packet(new ServerboundCustomPayloadPacket(
                        new BrandPayload(
                                randomBuffer.value().get()
                                        ? StringUtils.randomText(bufferSize.value().get(), true)
                                        : stringText.value().get())));
            }
        }
    }
}
