package monster.psyop.client.framework.modules.settings.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;

public class EntityListSetting extends ObjectListSetting<EntityListSetting, EntityType<?>> {
    private final ArrayList<EntityType<?>> suggestions;

    public EntityListSetting(ObjectListSetting.Builder<EntityListSetting, EntityType<?>> builder) {
        super(builder);
        this.suggestions = new ArrayList<>(BuiltInRegistries.ENTITY_TYPE.stream().filter(filter).toList());
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

    public static class Builder extends ObjectListSetting.Builder<EntityListSetting, EntityType<?>> {
        @Override
        public EntityListSetting build() {
            check();
            return new EntityListSetting(this);
        }
    }
}
