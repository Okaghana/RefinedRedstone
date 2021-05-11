package okaghana.refinedredstone.setup;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import okaghana.refinedredstone.block.RefinedRedstoneTileEntity;

public class RegistryEvents {
    @SubscribeEvent
    public static void FMLClientSetupEvent(final FMLClientSetupEvent event) {
        // Register TileEntity Renderers
        ClientRegistry.bindTileEntityRenderer(TileEntityRegister.REFINED_REDSTONE.get(), RefinedRedstoneTileEntity.Renderer::new);
    }
}
