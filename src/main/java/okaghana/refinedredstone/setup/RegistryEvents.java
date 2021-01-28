package okaghana.refinedredstone.setup;

import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegistryEvents {

    @SubscribeEvent
    public static void registerBlockColors(final ColorHandlerEvent.Block event) {
        event.getBlockColors().register(BlockRegister.REFINED_REDSTONE.get(), BlockRegister.REFINED_REDSTONE.get());
    }
}
