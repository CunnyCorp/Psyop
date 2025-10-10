package monster.psyop.client.framework.gui.views.features;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImInt;
import imgui.type.ImString;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.Config;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.friends.RoleType;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.impl.modules.misc.Friends;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendsView extends View {
    private final ImString newEntry = new ImString(64);
    private final ImInt addRoleIndex = new ImInt(0);

    @Override
    public String name() {
        return "friends";
    }

    @Override
    public void show() {
        if (ImGui.begin(displayName(), state())) {
            if (this.settings.hasLoaded) {
                ImGui.setWindowSize(this.settings.width, this.settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(this.settings.x, this.settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(this.settings.width, this.settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(this.settings.x, this.settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(500, 450, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(100, 100, ImGuiCond.FirstUseEver);
                this.settings.hasLoaded = true;
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 6.0f, 4.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 3.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 3.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12.0f, 12.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 6.0f, 4.0f);

            ImGui.getFont().setScale(1.2f);
            ImGui.textColored(0.00f, 0.75f, 0.75f, 1.0f, "Friends Manager");
            ImGui.getFont().setScale(1.0f);

            ImGui.separator();
            ImGui.spacing();

            ImGui.text("Add Player:");
            ImGui.sameLine();
            ImGui.setNextItemWidth(ImGui.getWindowWidth() - 250);
            ImGui.inputTextWithHint("##friend_add", "Enter player name or UUID...", newEntry, ImGuiInputTextFlags.CallbackResize);
            ImGui.sameLine();

            String[] roleNames = roleNames();
            if (FriendManager.roleTypes.isEmpty()) FriendManager.init();
            if (addRoleIndex.get() >= roleNames.length) addRoleIndex.set(0);
            ImGui.setNextItemWidth(110);
            if (ImGui.beginCombo("##add_role", roleNames.length > 0 ? roleNames[addRoleIndex.get()] : "")) {
                for (int i = 0; i < roleNames.length; i++) {
                    boolean selected = addRoleIndex.get() == i;
                    if (ImGui.selectable(roleNames[i], selected)) {
                        addRoleIndex.set(i);
                    }
                    if (selected) ImGui.setItemDefaultFocus();
                }
                ImGui.endCombo();
            }
            ImGui.sameLine();
            if (ImGui.button("Add", 80, 0)) {
                String key = newEntry.get().trim();
                if (!key.isEmpty()) {
                    String chosen = roleNames.length > 0 ? roleNames[addRoleIndex.get()] : "Friend";
                    FriendManager.addRole(key, chosen);
                    Psyop.log("Added {} as {}", key, chosen);
                    newEntry.set("");
                }
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            ImGui.text("Players:");
            ImGui.beginChild("friends_list", 0, ImGui.getWindowHeight() - 160, true);
            int index = 0;
            for (Map.Entry<String, monster.psyop.client.config.sub.PlayerRole> entry : new ArrayList<>(FriendManager.roles.entrySet())) {
                String playerKey = entry.getKey();
                var pr = entry.getValue();
                RoleType current = pr.roleType;
                if (current == null) {
                    current = FriendManager.rolesByName.getOrDefault("Friend", FriendManager.makeNewRole("Friend"));
                    FriendManager.addRole(playerKey, current.name);
                }

                ImGui.pushID(index);
                ImGui.text(playerKey);
                ImGui.sameLine(ImGui.getWindowWidth() - 260);

                int currentIndex = Math.max(0, indexOfRole(current.name));
                ImInt tempIdx = new ImInt(currentIndex);
                String currentLabel = roleNames.length > 0 && currentIndex < roleNames.length ? roleNames[currentIndex] : current.name;
                ImGui.setNextItemWidth(140);
                if (ImGui.beginCombo("##role_combo", currentLabel)) {
                    for (int i = 0; i < roleNames.length; i++) {
                        boolean selected = tempIdx.get() == i;
                        if (ImGui.selectable(roleNames[i], selected)) {
                            tempIdx.set(i);
                            String newRole = roleNames[i];
                            FriendManager.addRole(playerKey, newRole);
                            Psyop.log("Updated {} role to {}", playerKey, newRole);
                        }
                        if (selected) ImGui.setItemDefaultFocus();
                    }
                    ImGui.endCombo();
                }

                ImGui.sameLine();
                if (ImGui.button("Remove", 90, 0)) {
                    FriendManager.removeFriend(playerKey);
                    Psyop.log("Removed {} from list", playerKey);
                }

                ImGui.popID();
                index++;
            }
            ImGui.endChild();

            ImGui.spacing();
            if (ImGui.button("Import From Module", 160, 0)) {
                Friends friendsModule = Psyop.MODULES.get(Friends.class);
                if (friendsModule != null) {
                    friendsModule.refreshFriends();
                    Psyop.log("Imported {} players from Friends module", friendsModule.players.value().size());
                }
            }
            ImGui.sameLine();
            if (ImGui.button("Export To Module", 160, 0)) {
                Friends friendsModule = Psyop.MODULES.get(Friends.class);
                if (friendsModule != null) {
                    friendsModule.players.value().clear();
                    for (String key : FriendManager.roles.keySet()) {
                        var pr = FriendManager.roles.get(key);
                        if (pr != null && pr.roleType != null && "Friend".equalsIgnoreCase(pr.roleType.name)) {
                            friendsModule.players.value().add(new ImString(key));
                        }
                    }
                    friendsModule.refreshFriends();
                    Psyop.log("Exported players to Friends module");
                }
            }

            ImGui.popStyleVar(5);
        }

        this.settings.x = ImGui.getWindowPosX();
        this.settings.y = ImGui.getWindowPosY();
        this.settings.width = ImGui.getWindowWidth();
        this.settings.height = ImGui.getWindowHeight();
        ImGui.end();
    }

    @Override
    public void populateSettings(Config config) {
        this.settings = config.friendsManagerGui;
    }

    private static String[] roleNames() {
        List<String> names = new ArrayList<>();
        for (RoleType rt : FriendManager.roleTypes) {
            names.add(rt.name);
        }
        if (names.isEmpty()) {
            names.add("Friend");
        }
        return names.toArray(new String[0]);
    }

    private static int indexOfRole(String name) {
        for (int i = 0; i < FriendManager.roleTypes.size(); i++) {
            if (FriendManager.roleTypes.get(i).name.equalsIgnoreCase(name)) return i;
        }
        return 0;
    }
}
