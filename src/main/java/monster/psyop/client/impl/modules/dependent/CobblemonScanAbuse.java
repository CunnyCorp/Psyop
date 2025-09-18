package monster.psyop.client.impl.modules.dependent;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.FinishScanningPacket;
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Dependencies;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

@Dependencies.DependentModule
public class CobblemonScanAbuse extends Module {
    public final IntSetting iterations =
            new IntSetting.Builder()
                    .name("iterations")
                    .description("How many times to spam scans.")
                    .defaultTo(10)
                    .range(0, 40)
                    .addTo(coreGroup);
    public final IntSetting radius =
            new IntSetting.Builder()
                    .name("radius")
                    .description("Radius to scan.")
                    .defaultTo(64)
                    .range(0, 128)
                    .addTo(coreGroup);
    public int wait = 0;
    public List<Integer> scannedAlready = new ArrayList<>();
    public int lastScan = 0;

    public CobblemonScanAbuse() {
        super(Categories.EXPLOITS, "scan-abuse", "Abuses cobblemon scanning, ez?");
        this.dependencies.add(Dependencies.COBBLEMON);
    }

    @EventListener
    public void onTickPre(OnTick.Pre event) {
        if (MC.player == null || MC.player.level() == null) {
            return;
        }

        List<Entity> entities = MC.player.level().getEntities(null, AABB.of(new BoundingBox((int) (-radius.value().get() + MC.player.getX()), (int) (-radius.value().get() + MC.player.getY()), (int) (-radius.value().get() + MC.player.getZ()), (int) (radius.value().get() + MC.player.getX()), (int) (radius.value().get() + MC.player.getY()), (int) (radius.value().get() + MC.player.getZ()))));

        wait++;

        if (wait >= 30) {
            wait = 0;
            Cobblemon.implementation.getNetworkManager().sendToServer(new FinishScanningPacket(lastScan, 999999999));
            scannedAlready.add(lastScan);
            lastScan = 0;
            return;
        }

        if (lastScan != 0) {
            return;
        }

        for (Entity entity : entities) {
            if (entity instanceof PokemonEntity) {
                if (!scannedAlready.contains(entity.getId())) {
                    Cobblemon.implementation.getNetworkManager().sendToServer(new StartScanningPacket(entity.getId(), 999999999));
                }
            }
        }
    }
}
