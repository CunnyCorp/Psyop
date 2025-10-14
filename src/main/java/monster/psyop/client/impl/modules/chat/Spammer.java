package monster.psyop.client.impl.modules.chat;

import imgui.type.ImString;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.StringListSetting;
import monster.psyop.client.utility.CollectionUtils;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Spammer extends Module {
    private final IntSetting delay = new IntSetting.Builder()
            .name("delay")
            .description("Delay between messages.")
            .defaultTo(1200)
            .range(1, 3600)
            .addTo(coreGroup);
    private final StringListSetting messages = new StringListSetting.Builder()
            .name("messages")
            .defaultTo(new ArrayList<>(List.of(new ImString("Download psyop today at `https://psyop.monster"), new ImString("All cute girls use `https://psyop.monster"), new ImString("My doctor recommended me to use `psyop.monster"))))
            .addTo(coreGroup);
    private final BoolSetting random = new BoolSetting.Builder()
            .name("random")
            .defaultTo(false)
            .addTo(coreGroup);
    public final IntSetting messageLength = new IntSetting.Builder()
            .name("message-length")
            .defaultTo(10)
            .range(1, 250)
            .addTo(coreGroup);

    public int delayTimer = 0;

    public Spammer() {
        super(Categories.CHAT, "spammer", "Lets you spam chat with messages.");
    }

    @Override
    public void update() {
        if (delayTimer > 0) {
            delayTimer--;
            return;
        }

        String message;

        if (random.get()) {
            message = StringUtils.randomText(messageLength.get());
        } else {
            message = CollectionUtils.random(Objects.requireNonNull(messages.value()).toArray(new ImString[0])).get();
        }

        PacketUtils.command("say " + message);
        delayTimer = delay.get();
    }
}
