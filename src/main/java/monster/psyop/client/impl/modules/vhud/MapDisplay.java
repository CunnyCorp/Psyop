package monster.psyop.client.impl.modules.vhud;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnRenderSlot;
import monster.psyop.client.impl.modules.hud.HUD;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapDisplay extends HUD {
    public FloatSetting scale = new FloatSetting.Builder()
            .name("scale")
            .defaultTo(0.139f)
            .range(0.1f, 2.0f)
            .addTo(coreGroup);
    public FloatSetting xOffset = new FloatSetting.Builder()
            .name("x-offset")
            .defaultTo(-0.936f)
            .range(-8.0f, 8.0f)
            .addTo(coreGroup);
    public FloatSetting yOffset = new FloatSetting.Builder()
            .name("y-offset")
            .defaultTo(-0.829f)
            .range(-8.0f, 8.0f)
            .addTo(coreGroup);


    private final MapRenderState mapRenderState = new MapRenderState();

    public MapDisplay() {
        super("map-display", "Displays maps in GUI's!");
    }


    @EventListener
    public void onRenderSlot(OnRenderSlot.Post event) {
        if (event.stack.has(DataComponents.MAP_ID)) {
            MapId id = event.stack.get(DataComponents.MAP_ID);

            MapItemSavedData mapItemSavedData = MapItem.getSavedData(id, MC.level);

            if (mapItemSavedData != null) {
                event.guiGraphics.pose().pushMatrix();
                event.guiGraphics.pose().translate(event.x + xOffset.get(), event.y + yOffset.get());
                event.guiGraphics.pose().scale(scale.get(), scale.get());
                MC.getMapRenderer().extractRenderState(id, mapItemSavedData, this.mapRenderState);
                event.guiGraphics.submitMapRenderState(this.mapRenderState);
                event.guiGraphics.pose().popMatrix();
            }
        }
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
