package okaghana.refinedredstone.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import okaghana.refinedredstone.setup.TileEntityRegister;
import org.jetbrains.annotations.NotNull;

public class RefinedRedstoneTileEntity extends TileEntity {
    public RefinedRedstoneTileEntity() {
        super(TileEntityRegister.REFINED_REDSTONE.get());
    }

    @Override
    public void read(@NotNull BlockState state, @NotNull CompoundNBT nbt) {
        super.read(state, nbt);
    }

    @Override
    public @NotNull CompoundNBT write(@NotNull CompoundNBT compound) {
        return super.write(compound);
    }

    // The Renderer
    public static class Renderer extends TileEntityRenderer<RefinedRedstoneTileEntity> {
        ModelRenderer renderer;
        RenderMaterial rendermaterial;

        public Renderer(TileEntityRendererDispatcher rendererDispatcherIn) {
            super(rendererDispatcherIn);
            renderer = new ModelRenderer(16, 16, 0, 0);
            renderer.addBox(0,0,0,16,2,16);
            renderer.showModel = true;
            rendermaterial = Atlases.CHEST_LEFT_MATERIAL;
        }

        @Override
        public void render(@NotNull RefinedRedstoneTileEntity tileEntityIn, float partialTicks, @NotNull MatrixStack matrixStackIn, @NotNull IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
            renderer.render(matrixStackIn, rendermaterial.getBuffer(bufferIn, RenderType::getEntitySolid), 15, 15);
        }
    }
}