package okaghana.refinedredstone.setup;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import okaghana.refinedredstone.block.bakedModel.RefinedRedstoneBakedModel;

public class RegistryEvents {

    // Allows to register the Redstone Blocks as an IBlockColor Block
    @SubscribeEvent
    public static void registerBlockColors(final ColorHandlerEvent.Block event) {
        event.getBlockColors().register(BlockRegister.REFINED_REDSTONE.get(), BlockRegister.REFINED_REDSTONE.get());
    }


    // Called after all the other baked block models have been added to the modelRegistry
    // Allows us to manipulate the modelRegistry before BlockModelShapes caches them.
    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) {
        // Find the existing mappings for RefinedRedstoneBlock, which have been added from the json
        // Replace the mapping with our RefinedRedstoneBakedModel.
        for (BlockState blockState : BlockRegister.REFINED_REDSTONE.get().getStateContainer().getValidStates()) {
            ModelResourceLocation variantMRL = BlockModelShapes.getModelLocation(blockState);
            IBakedModel existingModel = event.getModelRegistry().get(variantMRL);
            RefinedRedstoneBakedModel customModel = new RefinedRedstoneBakedModel(existingModel);
            event.getModelRegistry().put(variantMRL, customModel);
        }
    }
}
