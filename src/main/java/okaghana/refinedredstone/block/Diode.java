package okaghana.refinedredstone.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class Diode extends Block implements IBlockColor {

    public static final AbstractBlock.Properties PROPERTIES = Block.Properties.create(Material.ROCK).hardnessAndResistance(0.05f).doesNotBlockMovement().harvestLevel(0);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty INPUT_POWER = IntegerProperty.create("power_in", 0, 15);
    public static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER_0_15;

    public Diode() {
        this(PROPERTIES);
    }

    public Diode(Properties properties) {
        super(properties);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull IBlockReader worldIn, @NotNull BlockPos pos, @NotNull ISelectionContext context) {
        return Block.makeCuboidShape(0, 0, 0, 16, 2, 16);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, INPUT_POWER, OUTPUT_POWER);
    }

    @Override
    public int getColor(@NotNull BlockState blockState, @Nullable IBlockDisplayReader displayReader, @Nullable BlockPos blockPos, int tintIndex) {
        if (tintIndex == 0) {
            return RedstoneWireBlock.getRGBByPower(blockState.get(INPUT_POWER));
        } else {
            return RedstoneWireBlock.getRGBByPower(blockState.get(OUTPUT_POWER));
        }
    }
}
