package okaghana.refinedredstone.misc;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FloatingTextEntityRenderer extends EntityRenderer<FloatingTextEntity> {

    public FloatingTextEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    @NotNull
    public ResourceLocation getEntityTexture(@NotNull FloatingTextEntity entity) {
        return new ResourceLocation("Bla");
    }
}
