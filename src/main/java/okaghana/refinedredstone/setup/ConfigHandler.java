package okaghana.refinedredstone.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ConfigHandler {
    public static ForgeConfigSpec config;

    public static ForgeConfigSpec.BooleanValue REFINED_REDSTONE_BLOCK_BIGGER_HITBOX;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General Settings").push("general");

        REFINED_REDSTONE_BLOCK_BIGGER_HITBOX = builder.comment("Gives the Refined Redstone Wire a bigger Hitbox. Default: true").define("biggerHitbox", true);

        builder.pop();
        config = builder.build();
    }
}
