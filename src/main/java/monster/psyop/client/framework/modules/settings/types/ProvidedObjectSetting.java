package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.impl.modules.client.GUIModule;
import monster.psyop.client.utility.StringUtils;

import java.util.List;
import java.util.function.Predicate;

public abstract class ProvidedObjectSetting<S, T> extends Setting<S, T> {
    public final Predicate<T> filter;
    private final ImString textFilter = new ImString();
    private boolean showSuggestions = false;

    public ProvidedObjectSetting(Builder<S, T> builder) {
        super(builder);
        this.filter = builder.filter;
        textFilter.inputData.isResizable = true;
    }

    public abstract List<T> getSuggestions();

    @Override
    public void render() {
        float[] tooltipColor = GUIModule.INSTANCE.tooltipColor.get();

        ImGui.spacing();

        ImGui.text("Filter:");
        ImGui.sameLine();
        float filterWidth = ImGui.getContentRegionAvail().x - 60;
        ImGui.setNextItemWidth(filterWidth);
        ImGui.inputTextWithHint("##filter_" + name, "Type to filter...", textFilter, ImGuiInputTextFlags.CallbackResize);

        ImGui.sameLine();
        if (ImGui.button("Clear##" + name)) {
            textFilter.set("");
        }

        ImGui.spacing();

        ImGui.text("Current: " + StringUtils.readable(itemToString(value())));

        ImGui.spacing();

        ImGui.text("Select new item:");

        if (getSuggestions() != null && !getSuggestions().isEmpty()) {
            if (ImGui.button("Show Suggestions##" + name)) {
                showSuggestions = !showSuggestions;
            }

            if (showSuggestions) {
                ImGui.beginChild(name + "_suggestions", 0, 150, true);

                boolean isFilterEmpty = textFilter.isEmpty();
                String lowerFilter = textFilter.get().toLowerCase();

                for (T suggestion : getSuggestions()) {
                    if (value() == suggestion) {
                        continue;
                    }

                    String nameStr = StringUtils.readable(itemToString(suggestion));

                    if (isFilterEmpty || nameStr.toLowerCase().contains(lowerFilter)) {
                        if (ImGui.selectable(nameStr)) {
                            synchronized (value()) {
                                value(suggestion);
                            }
                        }

                        if (ImGui.isItemHovered()) {
                            ImGui.beginTooltip();
                            ImGui.textColored(tooltipColor[0], tooltipColor[1], tooltipColor[2], tooltipColor[3], "Click to select: " + nameStr);
                            ImGui.endTooltip();
                        }
                    }
                }

                ImGui.endChild();
            }
        }
    }

    public abstract String itemToString(T v);

    @Override
    public boolean canReset() {
        return false;
    }

    public static class Builder<S, T> extends SettingBuilder<S, Builder<S, T>, T> {
        private Predicate<T> filter = (item) -> true;

        public Builder<S, T> filter(Predicate<T> v) {
            this.filter = v;
            return this;
        }
    }
}