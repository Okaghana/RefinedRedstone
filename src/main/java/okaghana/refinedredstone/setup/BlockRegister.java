package okaghana.refinedredstone.setup;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import okaghana.refinedredstone.RefinedRedstone;
import okaghana.refinedredstone.block.Diode;
import okaghana.refinedredstone.block.RefinedRedstoneBlock;

import java.util.function.Supplier;


// Creates a Deferred Register with all Blocks
@SuppressWarnings("unused")
public class BlockRegister {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, RefinedRedstone.MODID);

    public static final RegistryObject<RefinedRedstoneBlock> REFINED_REDSTONE = registerBlock("refined_redstone", RefinedRedstoneBlock::new);
    public static final RegistryObject<Diode> DIODE = registerBlock("diode", Diode::new);

    // Register a block and the respective item
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> ret = REGISTER.register(name, block);
        ItemRegister.REGISTER.register(name, () -> new BlockItem(ret.get(), new Item.Properties().group(RefinedRedstone.MOD_ITEM_GROUP)));
        return ret;
    }

    // Register a block without the item
    private static <T extends Block> RegistryObject<T> registerBlockNoItem(String name, Supplier<T> block) {
        return REGISTER.register(name, block);
    }
}
