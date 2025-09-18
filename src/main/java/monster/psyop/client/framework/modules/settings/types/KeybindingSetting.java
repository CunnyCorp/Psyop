package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImInt;
import monster.psyop.client.config.modules.settings.IntSettingConfig;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.utility.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

public class KeybindingSetting extends ProvidedObjectSetting<KeybindingSetting, ImInt> {
    List<ImInt> keySuggestions = new ArrayList<>();
    public boolean awaitingBinding = false;
    private float animationProgress = 0f;
    private long lastFrameTime = System.currentTimeMillis();
    private boolean wasAwaitingBinding = false;
    private float pulseValue = 0f;
    private boolean pulseDirection = true;

    public KeybindingSetting(ProvidedObjectSetting.Builder<KeybindingSetting, ImInt> builder) {
        super(builder);
        this.settingConfig = new IntSettingConfig();

        keySuggestions.add(new ImInt(-1));

        for (int key : KeyUtils.KEY_MAP_TO_TRANSLATION.keySet()) {
            keySuggestions.add(new ImInt(key));
        }
    }

    @Override
    public void render() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        // Update animations
        if (awaitingBinding != wasAwaitingBinding) {
            wasAwaitingBinding = awaitingBinding;
        }

        // Smooth animation for the awaiting binding state
        float targetProgress = awaitingBinding ? 1f : 0f;
        animationProgress = AnimationUtils.lerp(animationProgress, targetProgress, deltaTime * 8f);

        // Pulsing animation for the button
        if (awaitingBinding) {
            pulseValue += deltaTime * (pulseDirection ? 1f : -1f) * 2f;
            if (pulseValue >= 1f) {
                pulseValue = 1f;
                pulseDirection = false;
            } else if (pulseValue <= 0.4f) {
                pulseValue = 0.4f;
                pulseDirection = true;
            }
        } else {
            pulseValue = 1f;
        }

        ImGui.pushID(label());

        // Label with subtle animation
        ImGui.textColored(
                AnimationUtils.lerpColor(
                        0xFF888888, 0xFFFFFFFF,
                        awaitingBinding ? pulseValue : 1f
                ),
                label() + ":"
        );

        ImGui.sameLine();

        // Calculate button size with animation
        float buttonWidth = ImGui.calcTextSize(awaitingBinding ? "..." : KeyUtils.getTranslation(value().get())).x + 20f;
        buttonWidth = AnimationUtils.lerp(buttonWidth, buttonWidth * 1.1f, animationProgress * 0.1f);

        // Button styling with animations
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 6f);
        ImGui.pushStyleColor(ImGuiCol.Button, getButtonColor());
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, getButtonHoverColor());
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, getButtonActiveColor());

        // Animated button
        if (ImGui.button(awaitingBinding ? "..." : KeyUtils.getTranslation(value().get()), buttonWidth, 0)) {
            // Button clicked
        }

        // Visual feedback for awaiting binding state
        if (awaitingBinding) {
            // Draw pulsing border
            ImVec2 min = ImGui.getItemRectMin();
            ImVec2 max = ImGui.getItemRectMax();
            ImGui.getWindowDrawList().addRect(
                    min.x - 1, min.y - 1,
                    max.x + 1, max.y + 1,
                    AnimationUtils.lerpColor(0x80FF8000, 0xFFFF0000, pulseValue),
                    6f, 0, 2f * pulseValue
            );

            // Help text that fades in
            if (animationProgress > 0.3f) {
                ImGui.sameLine();
                ImGui.textColored(
                        AnimationUtils.lerpColor(0x00000000, 0xFF888888, (animationProgress - 0.3f) / 0.7f),
                        "Press any key or Backspace to cancel"
                );
            }
        }

        ImGui.popStyleColor(3);
        ImGui.popStyleVar();

        awaitingBinding = ImGui.isItemActivated() || awaitingBinding;

        if (ImGui.isKeyPressed(KeyUtils.BACKSPACE)) {
            awaitingBinding = false;
            // Add a quick visual feedback when cancelled
            pulseValue = 0.8f;
            pulseDirection = false;
        }

        ImGui.popID();
    }

    private int getButtonColor() {
        if (awaitingBinding) {
            return AnimationUtils.lerpColor(0x80333333, 0x80444444, pulseValue);
        }
        return 0x80222222;
    }

    private int getButtonHoverColor() {
        if (awaitingBinding) {
            return AnimationUtils.lerpColor(0x80444444, 0x80555555, pulseValue);
        }
        return 0x80333333;
    }

    private int getButtonActiveColor() {
        if (awaitingBinding) {
            return AnimationUtils.lerpColor(0x80555555, 0x80666666, pulseValue);
        }
        return 0x80444444;
    }

    public ImInt get() {
        return value();
    }

    @Override
    public List<ImInt> getSuggestions() {
        return keySuggestions;
    }

    @Override
    public String itemToString(ImInt v) {
        return KeyUtils.getTranslation(v.get());
    }

    @Override
    public boolean canReset() {
        return true;
    }

    public static class Builder extends ProvidedObjectSetting.Builder<KeybindingSetting, ImInt> {
        private Runnable pressAction;

        public Builder action(Runnable pressAction) {
            this.pressAction = pressAction;
            return this;
        }

        @Override
        protected void check() {
            super.check();
            if (pressAction == null)
                throw new RuntimeException("pressAction is required when building a keybinding.");
        }

        @Override
        public KeybindingSetting build() {
            check();
            return new KeybindingSetting(this);
        }
    }
}