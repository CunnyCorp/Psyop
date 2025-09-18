package monster.psyop.client.framework.gui.utility;

import imgui.flag.ImGuiKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("unused")
public class KeyUtils {
    public static int[] keyMap = new int[512];
    public static int additions = ImGuiKey.COUNT;
    public static final Map<Integer, Integer> REMAPPED_KEYS = new HashMap<>(512);
    public static final Map<Integer, Integer> FLIPPED_MAPPED_KEYS = new HashMap<>(512);
    public static final Map<Integer, String> KEY_MAP_TO_TRANSLATION = new HashMap<>(512);
    public static final int TAB = mapKey(ImGuiKey.Tab, GLFW_KEY_TAB, "tab"),
            LEFT = mapKey(ImGuiKey.LeftArrow, GLFW_KEY_LEFT, "left"),
            RIGHT = mapKey(ImGuiKey.RightArrow, GLFW_KEY_RIGHT, "right"),
            UP = mapKey(ImGuiKey.UpArrow, GLFW_KEY_UP, "up"),
            DOWN = mapKey(ImGuiKey.DownArrow, GLFW_KEY_DOWN, "down"),
            PAGE_UP = mapKey(ImGuiKey.PageUp, GLFW_KEY_PAGE_UP, "page-up"),
            PAGE_DOWN = mapKey(ImGuiKey.PageDown, GLFW_KEY_PAGE_DOWN, "page-down"),
            HOME = mapKey(ImGuiKey.Home, GLFW_KEY_HOME, "home"),
            END = mapKey(ImGuiKey.End, GLFW_KEY_END, "end"),
            INSERT = mapKey(ImGuiKey.Insert, GLFW_KEY_INSERT, "ins"),
            DELETE = mapKey(ImGuiKey.Delete, GLFW_KEY_DELETE, "del"),
            BACKSPACE = mapKey(ImGuiKey.Backspace, GLFW_KEY_BACKSPACE, "back"),
            SPACE = mapKey(ImGuiKey.Space, GLFW_KEY_SPACE, "space"),
            ENTER = mapKey(ImGuiKey.Enter, GLFW_KEY_ENTER, null),
            ESCAPE = mapKey(ImGuiKey.Escape, GLFW_KEY_ESCAPE, null),
            KP_ENTER = mapKey(ImGuiKey.KeyPadEnter, GLFW_KEY_KP_ENTER, "kp-enter"),
            NUM_0 = mapKey(++additions, GLFW_KEY_0, "0"),
            NUM_1 = mapKey(++additions, GLFW_KEY_1, "1"),
            NUM_2 = mapKey(++additions, GLFW_KEY_2, "2"),
            NUM_3 = mapKey(++additions, GLFW_KEY_3, "3"),
            NUM_4 = mapKey(++additions, GLFW_KEY_4, "4"),
            NUM_5 = mapKey(++additions, GLFW_KEY_5, "5"),
            NUM_6 = mapKey(++additions, GLFW_KEY_6, "6"),
            NUM_7 = mapKey(++additions, GLFW_KEY_7, "7"),
            NUM_8 = mapKey(++additions, GLFW_KEY_8, "8"),
            NUM_9 = mapKey(++additions, GLFW_KEY_9, "9"),
            SEMICOLON = mapKey(++additions, GLFW_KEY_SEMICOLON, "'"),
            EQUAL = mapKey(++additions, GLFW_KEY_EQUAL, "="),
            A = mapKey(ImGuiKey.A, GLFW_KEY_A, "a"),
            B = mapKey(++additions, GLFW_KEY_B, "b"),
            C = mapKey(ImGuiKey.C, GLFW_KEY_C, "c"),
            D = mapKey(++additions, GLFW_KEY_D, "d"),
            E = mapKey(++additions, GLFW_KEY_E, "e"),
            F = mapKey(++additions, GLFW_KEY_F, "f"),
            G = mapKey(++additions, GLFW_KEY_G, "g"),
            H = mapKey(++additions, GLFW_KEY_H, "h"),
            I = mapKey(++additions, GLFW_KEY_I, "i"),
            J = mapKey(++additions, GLFW_KEY_J, "j"),
            K = mapKey(++additions, GLFW_KEY_K, "k"),
            L = mapKey(++additions, GLFW_KEY_L, "l"),
            M = mapKey(++additions, GLFW_KEY_M, "m"),
            N = mapKey(++additions, GLFW_KEY_N, "n"),
            O = mapKey(++additions, GLFW_KEY_O, "o"),
            P = mapKey(++additions, GLFW_KEY_P, "p"),
            Q = mapKey(++additions, GLFW_KEY_Q, "q"),
            R = mapKey(++additions, GLFW_KEY_R, "r"),
            S = mapKey(++additions, GLFW_KEY_S, "s"),
            T = mapKey(++additions, GLFW_KEY_T, "t"),
            U = mapKey(++additions, GLFW_KEY_U, "u"),
            V = mapKey(ImGuiKey.V, GLFW_KEY_V, "v"),
            W = mapKey(++additions, GLFW_KEY_W, "w"),
            X = mapKey(ImGuiKey.X, GLFW_KEY_X, "x"),
            Y = mapKey(ImGuiKey.Y, GLFW_KEY_Y, "y"),
            Z = mapKey(ImGuiKey.Z, GLFW_KEY_Z, "z"),
            LEFT_BRACKET = mapKey(++additions, GLFW_KEY_LEFT_BRACKET, "["),
            BACKSLASH = mapKey(++additions, GLFW_KEY_BACKSLASH, "\\"),
            RIGHT_BRACKET = mapKey(++additions, GLFW_KEY_RIGHT_BRACKET, "]"),
            GRAVE_ACCENT = mapKey(++additions, GLFW_KEY_GRAVE_ACCENT, "`"),
            WORLD_1 = mapKey(++additions, GLFW_KEY_WORLD_1, "w1"),
            WORLD_2 = mapKey(++additions, GLFW_KEY_WORLD_2, "w2"),
            COUNT = mapKey(ImGuiKey.COUNT, additions, null);

    private static int mapKey(int keyMapPos, int glfwKey, @Nullable String translation) {
        REMAPPED_KEYS.put(keyMapPos, glfwKey);
        FLIPPED_MAPPED_KEYS.put(glfwKey, keyMapPos);
        if (translation != null) KEY_MAP_TO_TRANSLATION.put(keyMapPos, translation.toUpperCase(Locale.ROOT));
        keyMap[keyMapPos] = glfwKey;
        return keyMap[keyMapPos];
    }

    public static int getGlfwCodeFromKeyMap(int key) {
        return REMAPPED_KEYS.getOrDefault(key, -1);
    }

    public static int getKeyMapFromGlfwCode(int key) {
        return FLIPPED_MAPPED_KEYS.getOrDefault(key, -1);
    }

    public static String getTranslation(int key) {
        return KEY_MAP_TO_TRANSLATION.getOrDefault(key, "none");
    }
}
