package monster.psyop.client.impl.modules.player;

import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.litematica.render.RenderUtils;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AutoTotem extends Module {
    public BoolSetting rendering = new BoolSetting.Builder()
            .name("rendering")
            .description("Renders a sphere when you swap totems.")
            .defaultTo(true)
            .addTo(coreGroup);


    public AutoTotem() {
        super(Categories.PLAYER, "auto-totem", "Automatically move totems to the offhand.");
    }


    @Override
    public void update() {
        if (!MC.player.getOffhandItem().getItem().equals(Items.TOTEM_OF_UNDYING)) {
            int slot = InventoryUtils.findAnySlot(Items.TOTEM_OF_UNDYING);
            if (slot == -1) {
                return;
            }

            NotificationManager.get().addNotification("Auto Totem", "Moved totem to offhand", NotificationEvent.Type.INFO, 5000L);
            InventoryUtils.pickup(slot);
            InventoryUtils.placeItem(InventoryUtils.getOffhandOffset());
        }
    }
}
