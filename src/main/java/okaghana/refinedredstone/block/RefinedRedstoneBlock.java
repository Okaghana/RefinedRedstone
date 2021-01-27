package okaghana.refinedredstone.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class RefinedRedstoneBlock extends RedstoneWireBlock implements IBlockColor{

    public static final Properties PROPERTIES = Block.Properties.create(Material.ROCK).hardnessAndResistance(0.05f).doesNotBlockMovement().harvestLevel(0);

    private final boolean biggerHitbox = true;

    private static final VoxelShape SHAPE_CORE = Block.makeCuboidShape(6, 0, 6, 10, 2, 10);
    private static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(6, 0, 0, 10, 2, 6);
    private static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(10, 0, 6, 16, 2, 10);
    private static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(6, 0, 10, 10, 2, 16);
    private static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(0, 0, 6, 6, 2, 10);

    private boolean canProvidePower = true;

    public RefinedRedstoneBlock() {
        super(PROPERTIES);
        this.setDefaultState(this.stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE).with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(POWER, 0));
    }

    public RefinedRedstoneBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE).with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(POWER, 0));
    }

    /**
     * Returns the shape of the block
     *
     * The image that you see on the screen (when a block is rendered) is determined by the block model (i.e. the model json file).
     * But Minecraft also uses a number of other "shapes" to control the interaction of the block with its environment and with the player.
     * When the config "biggerHitbox" is True, the shape will be a whole 1x1 meter wide, else it will only be the shape of the textures itself.
     *
     * @param state     The State of the Block
     * @param worldIn   The World the Block is in
     * @param pos       The Position of the Block in the World
     * @param context   The Context
     * @return          A VoxelShape that represents the Shape of the Block
     */
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (biggerHitbox) {
            return Block.makeCuboidShape(0, 0, 0, 16, 2, 16);
        } else {
            VoxelShape shape = SHAPE_CORE;

            if (state.get(NORTH) != RedstoneSide.NONE) { shape = VoxelShapes.or(shape, SHAPE_NORTH); }
            if (state.get(EAST) != RedstoneSide.NONE) { shape = VoxelShapes.or(shape, SHAPE_EAST); }
            if (state.get(SOUTH) != RedstoneSide.NONE) { shape = VoxelShapes.or(shape, SHAPE_SOUTH); }
            if (state.get(WEST) != RedstoneSide.NONE) { shape = VoxelShapes.or(shape, SHAPE_WEST); }

            return shape;
        }
    }


    /**
     * Tint the Block according to the current Power
     *    0 = Dark Red
     *    15 = Bright Red
     *
     * The Color is calculated by the inherited method getRGBByPower from RedstoneWireBlock (for now)
     *
     * @param blockstate            The BlockState of the Block
     * @param blockDisplayReader    The DisplayReader
     * @param blockPos              The Position of the Block
     * @param tintIndex             Each block can have multiple tintable Textures. In this case it should always be 0 as we only have 1 tintindex
     * @return                      An RGBA-Value converted to an Integer. Each 8-bits of the 32-bit Integer represents a color between 0-255.
     */
    @Override
    public int getColor(BlockState blockstate, @Nullable IBlockDisplayReader blockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        return getRGBByPower(blockstate.get(POWER));
    }

    // ---------------------------------------- //
    //            BlockState Logic              //
    // ---------------------------------------- //

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.isIn(state.getBlock()) && !worldIn.isRemote) {
            this.updatePower(worldIn, pos, state);

            for(Direction direction : Direction.Plane.VERTICAL) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
            }

            this.updateNeighboursStateChange(worldIn, pos);
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.isIn(newState.getBlock())) {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            if (!worldIn.isRemote) {
                for(Direction direction : Direction.values()) {
                    worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
                }

                this.updatePower(worldIn, pos, state);
                this.updateNeighboursStateChange(worldIn, pos);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (state.isValidPosition(worldIn, pos)) {
                this.updatePower(worldIn, pos, state);
            } else {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }

        }
    }

    private void updateNeighboursStateChange(World world, BlockPos pos) {
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            this.notifyWireNeighborsOfStateChange(world, pos.offset(direction));
        }

        for(Direction direction1 : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.offset(direction1);
            if (world.getBlockState(blockpos).isNormalCube(world, blockpos)) {
                this.notifyWireNeighborsOfStateChange(world, blockpos.up());
            } else {
                this.notifyWireNeighborsOfStateChange(world, blockpos.down());
            }
        }

    }

    /**
     * Calls World.notifyNeighborsOfStateChange() for all neighboring blocks, but only if the given block is a redstone
     * wire.
     */
    private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos).isIn(this)) {
            worldIn.notifyNeighborsOfStateChange(pos, this);

            for(Direction direction : Direction.values()) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
            }

        }
    }

    // ----------------------------- //
    //           Power Logic         //
    // ------------------------------//

    private void updatePower(World world, BlockPos pos, BlockState state) {
        int i = this.getStrongestSignal(world, pos);
        if (state.get(POWER) != i) {
            if (world.getBlockState(pos) == state) {
                world.setBlockState(pos, state.with(POWER, i), 2);
            }

            world.notifyNeighborsOfStateChange(pos, this);
            for(Direction direction : Direction.values()) {
                BlockPos other = pos.offset(direction);
                world.notifyNeighborsOfStateChange(other, this);
            }
        }

    }

    private int getPower(BlockState state) {
        return state.isIn(this) ? state.get(POWER) : 0;
    }

    private int getStrongestSignal(World world, BlockPos pos) {
        this.canProvidePower = false;
        int i = world.getRedstonePowerFromNeighbors(pos);
        this.canProvidePower = true;
        int j = 0;
        if (i < 15) {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos.offset(direction);
                BlockState blockstate = world.getBlockState(blockpos);
                j = Math.max(j, this.getPower(blockstate));
                BlockPos blockpos1 = pos.up();
                if (blockstate.isNormalCube(world, blockpos) && !world.getBlockState(blockpos1).isNormalCube(world, blockpos1)) {
                    j = Math.max(j, this.getPower(world.getBlockState(blockpos.up())));
                } else if (!blockstate.isNormalCube(world, blockpos)) {
                    j = Math.max(j, this.getPower(world.getBlockState(blockpos.down())));
                }
            }
        }

        return Math.max(i, j - 1);
    }
}
