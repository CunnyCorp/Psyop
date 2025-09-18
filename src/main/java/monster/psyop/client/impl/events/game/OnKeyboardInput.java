package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;

//
public class OnKeyboardInput extends Event {
    public static final OnKeyboardInput INSTANCE = new OnKeyboardInput();
    public Input input;
    public ClientInput clientInput;

    public OnKeyboardInput() {
        super(true);
    }

    public static OnKeyboardInput get(ClientInput clientInput) {
        INSTANCE.refresh();
        INSTANCE.clientInput = clientInput;
        INSTANCE.input = null;
        return INSTANCE;
    }
}
