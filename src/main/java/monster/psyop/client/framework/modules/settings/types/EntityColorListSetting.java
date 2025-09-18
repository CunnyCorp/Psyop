package monster.psyop.client.framework.modules.settings.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;

public class EntityColorListSetting extends ObjectColorListSetting<EntityColorListSetting, EntityType<?>> {
    private final ArrayList<EntityType<?>> suggestions;

    public EntityColorListSetting(ObjectColorListSetting.Builder<EntityColorListSetting, EntityType<?>> builder) {
        super(builder);
        this.suggestions = new ArrayList<>(BuiltInRegistries.ENTITY_TYPE.stream().filter(filter).toList());
        this.identifier = "entity";
    }

    @Override
    public ArrayList<EntityType<?>> getSuggestions() {
        return suggestions;
    }

    @Override
    public EntityType<?> parseItem(String v) {
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("minecraft:" + v)).get().value();
    }

    @Override
    public String itemToString(EntityType<?> v) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(v).getPath();
    }

    public static class Builder extends ObjectColorListSetting.Builder<EntityColorListSetting, EntityType<?>> {
        @Override
        public EntityColorListSetting build() {
            check();
            return new EntityColorListSetting(this);
        }
    }
}
