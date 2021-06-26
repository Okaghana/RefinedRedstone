package okaghana.refinedredstone.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ConfigHandler {
    public static ForgeConfigSpec config;

    public static ForgeConfigSpec.BooleanValue REFINED_REDSTONE_BLOCK_BIGGER_HITBOX;
    public static ForgeConfigSpec.BooleanValue USE_SEPARATE_ITEM_GROUP;
    public static ForgeConfigSpec.IntValue ENGINEERS_HELMET_RADIUS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General Settings").push("general");

        REFINED_REDSTONE_BLOCK_BIGGER_HITBOX = builder.comment("Gives the Refined Redstone Wire a bigger Hitbox. Default: true").define("biggerHitbox", true);
        USE_SEPARATE_ITEM_GROUP = builder.comment("Use an own Item Group for this mod. Default: true").define("separateItemGroup", true);
        ENGINEERS_HELMET_RADIUS = builder.comment("Up to what radius the Engineers helmet should function. Default: 8").defineInRange("engineersHelmetRange", 8, 0, 64);

        builder.pop();
        config = builder.build();
    }
}
