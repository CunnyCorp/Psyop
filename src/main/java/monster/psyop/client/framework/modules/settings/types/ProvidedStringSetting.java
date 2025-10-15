package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImInt;
import imgui.type.ImString;
import monster.psyop.client.config.modules.settings.IntSettingConfig;
import monster.psyop.client.config.modules.settings.StringSettingConfig;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.utility.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

public class ProvidedStringSetting extends ProvidedObjectSetting<ProvidedStringSetting, ImString> {
    List<ImString> suggestions = new ArrayList<>();


    public ProvidedStringSetting(Builder builder) {
        super(builder);
        this.settingConfig = new StringSettingConfig();
        this.suggestions = builder.suggestions;
    }

    public ImString get() {
        return value();
    }

    @Override
    public List<ImString> getSuggestions() {
        return suggestions;
    }

    @Override
    public String itemToString(ImString v) {
        return v.get();
    }

    @Override
    public boolean canReset() {
        return true;
    }

    public static class Builder extends ProvidedObjectSetting.Builder<ProvidedStringSetting, ImString> {
        public List<ImString> suggestions = new ArrayList<>();

        public Builder suggestions(List<ImString> suggestions) {
            this.suggestions = suggestions;
            return this;
        }

        @Override
        protected void check() {
            super.check();
        }

        @Override
        public ProvidedStringSetting build() {
            check();
            return new ProvidedStringSetting(this);
        }
    }
}