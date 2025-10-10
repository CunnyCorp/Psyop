package monster.psyop.client.framework.modules;

import imgui.type.ImBoolean;
import imgui.type.ImInt;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.framework.gui.views.client.ModuleConfigView;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.KeybindingSetting;
import monster.psyop.client.utility.StringUtils;
import monster.psyop.client.utility.TextUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Module {
    protected static final Minecraft MC = Psyop.MC;
    protected boolean dev = false;
    public final ImBoolean active = new ImBoolean(false);
    public final Category category;
    public final String name;
    public final List<Dependency> dependencies = new ArrayList<>();
    private final Runnable onEnabled;
    private final Runnable onDisabled;
    @Getter
    private final List<GroupedSettings> groupedSettings = new ObjectArrayList<>(1);
    @Getter
    @Setter
    private String label;
    private String description;
    public GroupedSettings coreGroup = addGroup(new GroupedSettings("core", "Core module settings."));
    public KeybindingSetting keybinding =
            new KeybindingSetting.Builder()
                    .action(() -> this.active(!this.active()))
                    .name("bind")
                    .description("The key to bind for the client.")
                    .defaultTo(new ImInt(-1))
                    .addTo(coreGroup);
    public IntSetting priority =
            new IntSetting.Builder()
                    .name("priority")
                    .description("The priority of the module.")
                    .defaultTo(100)
                    .range(1, 1000)
                    .addTo(coreGroup);
    public BoolSetting preTick =
            new BoolSetting.Builder()
                    .name("pre-tick")
                    .description("Run before the tick.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public BoolSetting postTick =
            new BoolSetting.Builder()
                    .name("post-tick")
                    .description("Run after the tick.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public BoolSetting array =
            new BoolSetting.Builder()
                    .name("array")
                    .description("Show the module in the array.")
                    .defaultTo(true)
                    .addTo(coreGroup);


    public Module(Category category, String name, String description) {
        this(category, name, description, null, null);
    }

    public Module(
            Category category,
            String name,
            String description,
            @Nullable Runnable onEnabled,
            @Nullable Runnable onDisabled) {
        this.category = category;
        this.name = name;
        this.label = StringUtils.readable(name);
        this.onEnabled = onEnabled;
        this.onDisabled = onDisabled;
        this.description = description;
    }

    protected void enabled() {
        Psyop.log("Module {} was enabled.", name);
        Psyop.EVENT_HANDLER.add(this);
        if (onEnabled != null) {
            onEnabled.run();
        }
    }

    protected void disabled() {
        Psyop.log("Module {} was disabled.", name);
        Psyop.EVENT_HANDLER.remove(this);
        if (onDisabled != null) {
            onDisabled.run();
        }
    }

    public void toggled() {
        Psyop.log("Module {} was {}", name, this.active.get() ? "enabled." : "disabled.");
        if (this.active.get()) {
            enabled();
        } else {
            disabled();
        }
    }

    public String description() {
        return description;
    }

    public String description(String description) {
        return this.description = description;
    }

    public void active(boolean active) {
        if (this.active.get() != active) {
            if (active) {
                enabled();
            } else {
                disabled();
            }

            if (MC.player != null) {
                MutableComponent component = Component.empty();
                component.append(Component.literal(label).withStyle(TextUtils.MODULE_NAME_STYLE));
                component.append(Component.literal(" was ").withStyle(TextUtils.MODULE_INFO_STYLE));
                component.append(Component.literal(active ? "enabled." : "disabled.").withStyle(TextUtils.MODULE_INFO_SUB_STYLE).withColor(active ? new Color(154, 243, 232, 255).getRGB() : new Color(199, 109, 244, 255).getRGB()));

                MC.gui.getChat().addMessage(component);
            }

            NotificationManager.get().addNotification("Module Toggled", this.label + " was " + (active ? "enabled." : "disabled."), NotificationEvent.Type.INFO, 5000L);

            Psyop.log("Module {} was {}", name, active ? "enabled." : "disabled.");
        }

        this.active.set(active);
    }

    public boolean active() {
        return this.active.get();
    }

    public GroupedSettings addGroup(GroupedSettings sg) {
        groupedSettings.add(sg);
        return sg;
    }

    public boolean shouldAssignBinding() {
        ModuleConfigView view = ViewHandler.get(ModuleConfigView.class);

        return Gui.IS_LOADED.get() && view.state().get() && view.getModule() == this && getActiveBinding() != null;
    }

    public void clearBindings() {
        for (GroupedSettings gs : getGroupedSettings()) {
            for (Setting<?, ?> setting : gs.getRaw()) {
                if (setting instanceof KeybindingSetting keybindingSetting) {
                    keybindingSetting.awaitingBinding = false;
                }
            }
        }
    }

    public void update() {

    }

    public boolean controlsHotbar() {
        return false;
    }

    public boolean inUse() {
        return false;
    }

    public @Nullable KeybindingSetting getActiveBinding() {
        for (GroupedSettings gs : getGroupedSettings()) {
            for (Setting<?, ?> setting : gs.getRaw()) {
                if (setting instanceof KeybindingSetting keybindingSetting) {
                    if (keybindingSetting.awaitingBinding) {
                        return keybindingSetting;
                    }
                }
            }
        }

        return null;
    }

    public void keyPressed(int key, int action) {
        if (shouldAssignBinding()) {
            if (!Objects.equals(KeyUtils.getTranslation(key), "none") && !Objects.equals(KeyUtils.getTranslation(key), "back")) {
                Objects.requireNonNull(getActiveBinding()).value(new ImInt(key));
            } else {
                Objects.requireNonNull(getActiveBinding()).value(new ImInt(-1));
            }

            clearBindings();
        } else if (getActiveBinding() != null) {
            clearBindings();
        } else if (action == 1 && key == keybinding.value().get()) {
            active(!active());
        }
    }

    public void load() {
        if (this.getClass() == Module.class || this.name == null) {
            return;
        }

        if (dependencies.isEmpty() || dependencies.stream().allMatch(Dependency::isLoaded))
            Psyop.MODULES.add(this);
    }
}
