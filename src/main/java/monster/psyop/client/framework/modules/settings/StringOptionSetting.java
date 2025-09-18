// StringOptionSetting.java - Create this new class
package monster.psyop.client.framework.modules.settings;

import imgui.ImGui;
import imgui.type.ImString;
import monster.psyop.client.framework.gui.utility.ColoredText;
import monster.psyop.client.framework.gui.utility.GuiUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class StringOptionSetting extends Setting<String, ImString> {
    private final List<String> options;

    public StringOptionSetting(Builder builder) {
        super(builder);
        this.options = builder.options;
    }

    @Override
    public void render() {
        GuiUtils.text(new ColoredText(label() + ": ", Color.WHITE),
                new ColoredText(value().get(), Color.LIGHT_GRAY));

        if (ImGui.beginCombo("##" + name, value().get())) {
            for (String option : options) {
                boolean isSelected = option.equals(value().get());
                if (ImGui.selectable(option, isSelected)) {
                    value().set(option);
                }
                if (isSelected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }

        if (description != null && ImGui.isItemHovered()) {
            ImGui.setTooltip(description);
        }
    }

    public static class Builder extends SettingBuilder<StringOptionSetting, Builder, ImString> {
        private List<String> options;

        public Builder options(String... options) {
            this.options = Arrays.asList(options);
            return this;
        }

        public Builder options(List<String> options) {
            this.options = options;
            return this;
        }

        public StringOptionSetting build() {
            return new StringOptionSetting(this);
        }
    }
}