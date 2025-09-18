package monster.psyop.client.framework.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

public class Dependencies {
    public static final ArrayList<Dependency> EMPTY = new ArrayList<>();
    public static Dependency LITEMATICA = new Dependency("litematica");
    public static Dependency BARITONE = new Dependency("baritone-meteor");
    public static Dependency METEOR_CLIENT = new Dependency("meteor-client");
    public static Dependency VFP = new Dependency("viafabricplus");
    public static Dependency COBBLEMON = new Dependency("cobblemon");

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface DependentModule {
    }
}
