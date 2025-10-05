package monster.psyop.client.utility;

import net.minecraft.world.level.dimension.DimensionType;

import java.util.concurrent.Callable;

import static monster.psyop.client.Psyop.MC;

@SuppressWarnings("unused")
public enum DimensionCheck {
    OW(() -> {
        if (MC.player == null) {
            return false;
        }

        DimensionType dimension = MC.player.clientLevel.dimensionType();
        return dimension.bedWorks();
    }),
    OW_OR_NETHER(() -> {
        if (MC.player == null) {
            return false;
        }

        DimensionType dimension = MC.player.clientLevel.dimensionType();
        return dimension.respawnAnchorWorks() || dimension.bedWorks();
    }),
    OW_OR_END(() -> {
        if (MC.player == null) {
            return false;
        }

        DimensionType dimension = MC.player.clientLevel.dimensionType();
        return !dimension.respawnAnchorWorks();
    }),
    NETHER(() -> {
        if (MC.player == null) {
            return false;
        }

        DimensionType dimension = MC.player.clientLevel.dimensionType();
        return dimension.respawnAnchorWorks();
    }),
    NETHER_OR_END(() -> {
        if (MC.player == null) {
            return false;
        }

        DimensionType dimension = MC.player.clientLevel.dimensionType();
        return !dimension.bedWorks();
    }),
    END(() -> {
        if (MC.player == null) {
            return false;
        }

        DimensionType dimension = MC.player.clientLevel.dimensionType();
        return !dimension.bedWorks() && !dimension.respawnAnchorWorks();
    });

    public final Callable<Boolean> check;

    DimensionCheck(Callable<Boolean> check) {
        this.check = check;
    }
}
