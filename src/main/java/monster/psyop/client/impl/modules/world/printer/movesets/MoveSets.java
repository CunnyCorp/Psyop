package monster.psyop.client.impl.modules.world.printer.movesets;

public enum MoveSets {
    VANILLA(new VanillaMove()),
    ADVANCED(new AdvancedMove()),
    BARITONE(new BaritoneMove());

    public final DefaultMove movement;

    MoveSets(DefaultMove movement) {
        this.movement = movement;
    }
}
