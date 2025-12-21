package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.modules.render.ArmorView;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EquipmentLayerRenderer.class, priority = 749)
public class EquipmentLayerRendererMixin {
    @Unique
    private boolean renderingWings = false;

    @Inject(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", ordinal = 0), cancellable = true)
    public void renderLayers
            (EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> resourceKey, Model model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, @Nullable ResourceLocation resourceLocation, CallbackInfo ci) {
        renderingWings = layerType == EquipmentClientInfo.LayerType.WINGS;
    }

    @Redirect(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", ordinal = 0))
    public void renderArmorPieces(Model instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
        if (Psyop.MODULES.isActive(ArmorView.class)) {
            ArmorView module = Psyop.MODULES.get(ArmorView.class);

            if (renderingWings) {
                if (module.hideWings.get()) {
                    return;
                }

                if (module.modifyWingsColor.get()) {
                    instance.renderToBuffer(poseStack, vertexConsumer, i, j, ImColorW.toInt(module.wingsColor.get()));
                    return;
                }
            } else {
                if (module.hideArmor.get()) {
                    return;
                }

                if (module.modifyArmorColor.get()) {
                    instance.renderToBuffer(poseStack, vertexConsumer, i, j, ImColorW.toInt(module.armorColor.get()));
                    return;
                }
            }
        }

        instance.renderToBuffer(poseStack, vertexConsumer, i, j, k);
    }

    @Redirect(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V", ordinal = 0))
    public void renderTrims(Model instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
        if (Psyop.MODULES.isActive(ArmorView.class)) {
            ArmorView module = Psyop.MODULES.get(ArmorView.class);

            if (module.hideTrims.get()) {
                return;
            }

            if (module.modifyTrimsColor.get()) {
                instance.renderToBuffer(poseStack, vertexConsumer, i, j, ImColorW.toInt(module.trimsColor.get()));
                return;
            }
        }

        instance.renderToBuffer(poseStack, vertexConsumer, i, j);
    }
}
