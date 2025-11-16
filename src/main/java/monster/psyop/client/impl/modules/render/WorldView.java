package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;

public class WorldView extends Module {
    public BoolSetting modifyFogColor = new BoolSetting.Builder()
            .name("modify-fog-color")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting fogColor = new ColorSetting.Builder()
            .name("fog-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.6f})
            .addTo(coreGroup);
    public GroupedSettings worldGroup = addGroup(new GroupedSettings("world", "Modifies how things in the world render."));
    public BoolSetting modifyAmbient = new BoolSetting.Builder()
            .name("modify-ambient")
            .defaultTo(true)
            .addTo(worldGroup);
    public FloatSetting ambientLight = new FloatSetting.Builder()
            .name("ambient-light")
            .defaultTo(0.124f)
            .range(0.0f, 0.5f)
            .visible((v) -> modifyAmbient.get())
            .addTo(worldGroup);
    public BoolSetting forceLightMap = new BoolSetting.Builder()
            .name("force-light-map")
            .defaultTo(true)
            .addTo(worldGroup);
    public GroupedSettings entityGroup = addGroup(new GroupedSettings("entities", "Modifies how entities are rendered in the world."));
    public BoolSetting modifyEntityLighting = new BoolSetting.Builder()
            .name("modify-entity-lighting")
            .defaultTo(true)
            .addTo(entityGroup);
    public IntSetting entityLight = new IntSetting.Builder()
            .name("entity-light")
            .defaultTo(15)
            .range(0, 15)
            .visible((v) -> modifyEntityLighting.get())
            .addTo(entityGroup);

    public WorldView() {
        super(Categories.RENDER, "world-view", "Allows for EXTREME modification of the games rendering values.");
    }

}
