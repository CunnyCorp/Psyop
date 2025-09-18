package monster.psyop.client.plugins;

public interface JavaAddon {
    void onInit();

    default void onShutdown() {
    }

    default void refreshClient() {
    }
}
