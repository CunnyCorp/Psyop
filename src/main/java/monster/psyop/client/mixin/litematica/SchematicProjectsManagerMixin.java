package monster.psyop.client.mixin.litematica;

import fi.dy.masa.litematica.schematic.projects.SchematicProject;
import fi.dy.masa.litematica.schematic.projects.SchematicProjectsManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(value = SchematicProjectsManager.class, remap = false)
public class SchematicProjectsManagerMixin {
    @Shadow
    @Nullable
    private SchematicProject currentProject;

    @Inject(method = "openProject", at = @At("TAIL"))
    public void openProject(File projectFile, CallbackInfoReturnable<Boolean> cir) {
        //LiteHacks.currentProject = currentProject;
    }

    @Inject(method = "closeCurrentProject", at = @At("HEAD"))
    public void closeCurrentProject(CallbackInfo ci) {
        //if (currentProject != null) {
        //  Cunny.EVENT_HANDLER.call(CloseSchematicEvent.get(currentProject));
        //}
    }
}
