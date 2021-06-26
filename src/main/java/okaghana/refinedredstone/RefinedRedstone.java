package okaghana.refinedredstone;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import okaghana.refinedredstone.setup.*;

import javax.annotation.Nonnull;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/** The main class of the mod and serves as an entry point for forge to load all mod files. <br><br>
 *
 * This will automatically register all blocks and items specified in via the {@link okaghana.refinedredstone.setup} package
 * and register various registry events to the mod event bus. <br>
 * It will also provide a {@link ItemGroup} for all items of this mod. <br><br>
 *
 * Whenever you want to log something to the logfiles, you can use {@link okaghana.refinedredstone.RefinedRedstone#MOD_LOGGER}.log() to log a message.
 */
@SuppressWarnings("unused")
@Mod("refinedredstone")
public class RefinedRedstone {
    public static final String MODID = "refinedredstone";
    public static final String VERSION = "0.1";

    public static IEventBus MOD_EVENT_BUS;
    public static ItemGroup MOD_ITEM_GROUP;
    public static Logger MOD_LOGGER;

    public RefinedRedstone() {
        MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        MOD_LOGGER = Logger.getLogger("Refined Redstone");
        LogManager.getLogManager().addLogger(MOD_LOGGER);

        // Register Blocks and Items to the
        BlockRegister.REGISTER.register(MOD_EVENT_BUS);
        ItemRegister.REGISTER.register(MOD_EVENT_BUS);

        // Create the ItemGroup for the items
        if (ConfigHandler.USE_SEPARATE_ITEM_GROUP.get()) {
            MOD_ITEM_GROUP = new ItemGroup(MODID) {
                private final Supplier<ItemStack> icon = () -> new ItemStack(BlockRegister.REFINED_REDSTONE.get());

                @Override
                @Nonnull
                public ItemStack createIcon() {
                    return icon.get();
                }
            };
        } else {
            MOD_ITEM_GROUP = ItemGroup.REDSTONE;
        }

        // Register to the Events
        MOD_EVENT_BUS.register(RegistryEvents.class);
        MOD_EVENT_BUS.register(EntityRegister.class);

        // Register (and therefore load) the Config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.config, "RefinedRedstone.toml");
    }

    // Debugging
    public static void log(String message){
        MOD_LOGGER.log(Level.INFO, message);
    }

    // more Debugging
    @OnlyIn(Dist.CLIENT)
    public static void chat(String message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendChatMessage(message);
        }
    }
}
