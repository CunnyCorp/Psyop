package monster.psyop.client.impl.modules.misc;

import imgui.type.ImString;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.StringListSetting;
import monster.psyop.client.impl.events.game.OnMouseClick;
import monster.psyop.client.utility.WorldUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class Friends extends Module {
    public List<String> friends = new ArrayList<>();

    public final BoolSetting midClick =
            new BoolSetting.Builder()
                    .name("mid-click")
                    .description("Middle clicking while looking at a player friends them.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final StringListSetting players =
            new StringListSetting.Builder()
                    .name("players")
                    .description("Friends!")
                    .defaultTo(new ArrayList<>())
                    .addTo(coreGroup);

    public Friends() {
        super(Categories.MISC, "friends", "Prevents modules from attacking friends.");
    }

    @Override
    protected void enabled() {
        super.enabled();
        refreshFriends();
    }

    @EventListener
    public void onMouse(OnMouseClick event) {
        if (event.key == 2 && event.action == 1 && midClick.get()) {
            if (WorldUtils.isLookingAt(HitResult.Type.ENTITY)) {
                EntityHitResult hitResult = (EntityHitResult) MC.hitResult;
                if (hitResult.getEntity() instanceof Player) {
                    if (friends.contains(hitResult.getEntity().getName().getString())) {
                        ImString removeStr = null;
                        for (ImString name : players.value()) {
                            if (name.get().equals(hitResult.getEntity().getName().getString())) {
                                removeStr = name;
                            }
                        }

                        if (removeStr != null) {
                            players.value().remove(removeStr);
                            NotificationManager.get().addNotification("Friends", removeStr + " was unfriended.", NotificationEvent.Type.INFO, 5000L);
                        }
                    } else {
                        friends.add(hitResult.getEntity().getName().getString());
                        NotificationManager.get().addNotification("Friends", hitResult.getEntity().getName().getString() + " was added as a friend.", NotificationEvent.Type.INFO, 5000L);
                    }
                    event.cancel();
                }
            }
        }
    }

    public void refreshFriends() {
        friends.clear();
        for (ImString name : players.value()) {
            friends.add(name.get());
            FriendManager.addRole(name.get(), "Friend");
        }
    }
}
