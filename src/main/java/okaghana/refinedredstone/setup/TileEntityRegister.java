package okaghana.refinedredstone.setup;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import okaghana.refinedredstone.RefinedRedstone;
import okaghana.refinedredstone.block.RefinedRedstoneTileEntity;

import java.util.function.Function;
import java.util.function.Supplier;

public class TileEntityRegister {
    public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, RefinedRedstone.MODID);

    public static final RegistryObject<TileEntityType<RefinedRedstoneTileEntity>> REFINED_REDSTONE = REGISTER.register("mini_model", () ->
            TileEntityType.Builder.create(RefinedRedstoneTileEntity::new, BlockRegister.REFINED_REDSTONE.get())
                    .build(null)
    );
    // public static final RegistryObject<TileEntityType<RefinedRedstoneTileEntity>> REFINED_REDSTONE = registerTileEntity("name", RefinedRedstoneTileEntity::new, RefinedRedstoneTileEntity.Renderer::new, BlockRegister.REFINED_REDSTONE.get());

    // Register a TileEntity
    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> registerTileEntity(String name, Supplier<T> entity, Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<T>> renderer, Block... blocks) {
        TileEntityType.Builder<T> builder = TileEntityType.Builder.create(entity, blocks);
        return REGISTER.register(name, () -> builder.build(null));
    }
}
