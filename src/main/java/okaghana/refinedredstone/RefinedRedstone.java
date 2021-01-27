package okaghana.refinedredstone;

import net.minecraftforge.common.MinecraftForge;
import okaghana.refinedredstone.setup.BlockRegister;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import okaghana.refinedredstone.setup.ItemRegister;
import okaghana.refinedredstone.setup.RegistryEvents;

import javax.annotation.Nonnull;
import java.util.function.Supplier;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Mod("refinedredstone")
public class RefinedRedstone {
    public static final String MODID = "refinedredstone";
    public static final String VERSION = "0.1";

    public static IEventBus MOD_EVENT_BUS;
    public static ItemGroup MOD_ITEM_GROUP;
    public static Logger MOD_LOGGER;

    public RefinedRedstone() {
        MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        MOD_LOGGER = LogManager.getLogManager().getLogger(MODID);

        BlockRegister.REGISTER.register(MOD_EVENT_BUS);
        ItemRegister.REGISTER.register(MOD_EVENT_BUS);

        MOD_ITEM_GROUP = new ItemGroup(MODID) {
            private final Supplier<ItemStack> icon = () -> new ItemStack(BlockRegister.REFINED_REDSTONE.get());

            @Override
            @Nonnull
            public ItemStack createIcon() {
                return icon.get();
            }
        };

        MOD_EVENT_BUS.register(RegistryEvents.class);
    }
}
