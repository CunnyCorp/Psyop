package monster.psyop.client.impl.modules.player;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.StringSetting;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import static monster.psyop.client.Psyop.MC;

public class MiddleClickUse extends Module {

    private final StringSetting mode = new StringSetting.Builder()
        .name("mode")
        .description("Which item to use. Options: Pearl, Rocket")
        .defaultTo("Rocket")
        .addTo(coreGroup);

    private final BoolSetting silentSwitch = new BoolSetting.Builder()
        .name("silent-switch")
        .description("Uses items from your hotbar without changing your selected slot.")
        .defaultTo(true)
        .addTo(coreGroup);

    private final BoolSetting disableInCreative = new BoolSetting.Builder()
        .name("disable-in-creative")
        .description("Disables middle click action in Creative mode.")
        .defaultTo(true)
        .addTo(coreGroup);

    private final BoolSetting notify = new BoolSetting.Builder()
        .name("notify")
        .description("Notifies you if you don't have the required item.")
        .defaultTo(true)
        .addTo(coreGroup);

    private boolean wasMiddleMouseDown = false;

    public MiddleClickUse() {
        super(Categories.PLAYER, "middle-click-use", "Uses certain items on middle click.");
    }

    @Override
    public void update() {
        if (MC.screen != null || (disableInCreative.get() && MC.player != null && MC.player.isCreative())) {
            return;
        }

        boolean middleMouseDown = GLFW.glfwGetMouseButton(MC.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (middleMouseDown && !wasMiddleMouseDown) {
            useItem();
        }

        wasMiddleMouseDown = middleMouseDown;
    }

    private void useItem() {
        if (MC.player == null || MC.gameMode == null) return;

        Item itemToFind;
        String modeValue = mode.value().get();
        if (modeValue.equalsIgnoreCase("Pearl")) {
            itemToFind = Items.ENDER_PEARL;
        } else if (modeValue.equalsIgnoreCase("Rocket")) {
            itemToFind = Items.FIREWORK_ROCKET;
        } else {
            if (notify.get()) {
                NotificationManager.get().addNotification("Middle Click Use", "Invalid mode: " + modeValue, NotificationEvent.Type.ERROR, 3000L);
            }
            return;
        }

        int itemSlot = findItemInHotbar(itemToFind);

        if (itemSlot == -1) {
            if (notify.get()) {
                NotificationManager.get().addNotification("Middle Click Use", "Could not find " + modeValue + " in hotbar.", NotificationEvent.Type.WARNING, 3000L);
            }
            return;
        }

        int originalSlot = MC.player.getInventory().getSelectedSlot();

        if (silentSwitch.get()) {
            PacketUtils.send(new ServerboundSetCarriedItemPacket(itemSlot));
            MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
            PacketUtils.send(new ServerboundSetCarriedItemPacket(originalSlot));
        } else {
            MC.player.getInventory().setSelectedSlot(itemSlot);
            MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
            MC.player.getInventory().setSelectedSlot(originalSlot);
        }
    }

    private int findItemInHotbar(Item item) {
        if (MC.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }
}
