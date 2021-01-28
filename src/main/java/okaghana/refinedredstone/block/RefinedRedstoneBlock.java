package okaghana.refinedredstone.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import okaghana.refinedredstone.setup.ConfigHandler;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

/**
 * This is the main block of the mod and serves as an improved version of the default redstone wire. <br><br>
 *
 * All methods originally copied from the basic {@link RedstoneWireBlock}, as we want to ensure the same behaviour and functionality
 * as the default Redstone and only want to change this where we want to add features
 */
public class RefinedRedstoneBlock extends Block implements IBlockColor{

    public static final Properties PROPERTIES = Block.Properties.create(Material.ROCK).hardnessAndResistance(0.05f).doesNotBlockMovement().harvestLevel(0);

    private static final VoxelShape SHAPE_CORE = Block.makeCuboidShape(6, 0, 6, 10, 2, 10);
    private static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(6, 0, 0, 10, 2, 6);
    private static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(10, 0, 6, 16, 2, 10);
    private static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(6, 0, 10, 10, 2, 16);
    private static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(0, 0, 6, 6, 2, 10);

    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.REDSTONE_NORTH;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.REDSTONE_EAST;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.REDSTONE_SOUTH;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.REDSTONE_WEST;
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
    public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));

    private static final Vector3f[] powerColors = new Vector3f[16];
    private boolean canProvidePower = true;


    /**
     * Initializes the Block with {@link RefinedRedstoneBlock#PROPERTIES} as the Property
     */
    public RefinedRedstoneBlock() {
        this(PROPERTIES);
    }


    /**
     * Create a new Object with the given Properties. As we have the desired Properties as a static property, this will probably
     * only ever be called from {@link RefinedRedstoneBlock#RefinedRedstoneBlock()} <br><br>
     *
     * @param properties The Properties of the Block
     */
    public RefinedRedstoneBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE).with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(POWER, 0));

        for(int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0F;
            float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float f3 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            powerColors[i] = new Vector3f(f1, f2, f3);
        }
    }

    /**
     * Returns the shape of the block <br><br>
     *
     * The image that you see on the screen (when a block is rendered) is determined by the block model (i.e. the model json file).
     * But Minecraft also uses a number of other "shapes" to control the interaction of the block with its environment and with the player
     * (basically the hitbox for the gray wireframe when you look at a block). <br><br>
     *
     * When the config "biggerHitbox" is True, the shape will be a whole 1x1 meter wide, else it will only be the shape of the textures itself. <br><br>
     *
     * @param state     The State of the Block
     * @param worldIn   The World the Block is in
     * @param pos       The Position of the Block in the World
     * @param context   The Context
     * @return          A VoxelShape that represents the Shape of the Block
     */
    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull IBlockReader worldIn, @NotNull BlockPos pos, @NotNull ISelectionContext context) {
        if (ConfigHandler.REFINED_REDSTONE_BLOCK_BIGGER_HITBOX.get()) {
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
     * This will register the desired Blockstate for this Block to the game.
     */
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POWER);
    }

    /**
     * Tint the Block according to the current Power
     *    0 = Dark Red
     *    15 = Bright Red <br><br>
     *
     * The Color is calculated by the inherited method getRGBByPower from RedstoneWireBlock (for now)<br><br>
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

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getUpdatedState(context.getWorld(), getDefaultState(), context.getPos());
    }

    // ---------------------------------------- //
    //            BlockState Logic              //
    // ---------------------------------------- //


    /**
     * Will be called BEFORE a Block of this type is placed into the world
     *
     * @param state     The Blockstate
     * @param worldIn   What world the block is in
     * @param pos       The Position in the world
     * @param oldState  The old Blockstate
     * @param isMoving  Whether the block is moving
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onBlockAdded(BlockState state, @NotNull World worldIn, @NotNull BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.isIn(state.getBlock()) && !worldIn.isRemote) {
            this.updatePower(worldIn, pos, state);

            for(Direction direction : Direction.Plane.VERTICAL) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
            }

            this.updateNeighboursStateChange(worldIn, pos);
        }
    }

    /**
     * This is called whenever the BlockState changes or the Block is broken
     *
     * @param state     The Blockstate
     * @param worldIn   What world the block is in
     * @param pos       The Position in the world
     * @param newState  The new State of the Block
     * @param isMoving  Whether the block is moving
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(@NotNull BlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!isMoving && !state.isIn(newState.getBlock())) {

            // Previously super.onReplaced()
            if (state.hasTileEntity() && (!state.isIn(newState.getBlock()) || !newState.hasTileEntity())) {
                worldIn.removeTileEntity(pos);
            }

            if (!worldIn.isRemote) {
                for(Direction direction : Direction.values()) {
                    worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
                }

                this.updatePower(worldIn, pos, state);
                this.updateNeighboursStateChange(worldIn, pos);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull BlockState state, World worldIn, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving) {
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

    private BlockState getUpdatedState(IBlockReader reader, BlockState state, BlockPos pos) {
        boolean flag = areAllSidesInvalid(state);
        state = this.recalculateFacingState(reader, this.getDefaultState().with(POWER, state.get(POWER)), pos);
        if (!flag || !areAllSidesInvalid(state)) {
            boolean flag1 = state.get(NORTH).func_235921_b_();
            boolean flag2 = state.get(SOUTH).func_235921_b_();
            boolean flag3 = state.get(EAST).func_235921_b_();
            boolean flag4 = state.get(WEST).func_235921_b_();
            boolean flag5 = !flag1 && !flag2;
            boolean flag6 = !flag3 && !flag4;
            if (!flag4 && flag5) {
                state = state.with(WEST, RedstoneSide.SIDE);
            }

            if (!flag3 && flag5) {
                state = state.with(EAST, RedstoneSide.SIDE);
            }

            if (!flag1 && flag6) {
                state = state.with(NORTH, RedstoneSide.SIDE);
            }

            if (!flag2 && flag6) {
                state = state.with(SOUTH, RedstoneSide.SIDE);
            }

        }
        return state;
    }

    private BlockState recalculateFacingState(IBlockReader reader, BlockState state, BlockPos pos) {
        boolean flag = !reader.getBlockState(pos.up()).isNormalCube(reader, pos);

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (!state.get(FACING_PROPERTY_MAP.get(direction)).func_235921_b_()) {
                RedstoneSide redstoneside = this.recalculateSide(reader, pos, direction, flag);
                state = state.with(FACING_PROPERTY_MAP.get(direction), redstoneside);
            }
        }

        return state;
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public BlockState updatePostPlacement(@NotNull BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull IWorld worldIn, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (facing == Direction.DOWN) {
            return stateIn;
        } else if (facing == Direction.UP) {
            return this.getUpdatedState(worldIn, stateIn, currentPos);
        } else {
            RedstoneSide redstoneside = this.getSide(worldIn, currentPos, facing);
            return redstoneside.func_235921_b_() == stateIn.get(FACING_PROPERTY_MAP.get(facing)).func_235921_b_() && !areAllSidesValid(stateIn) ? stateIn.with(FACING_PROPERTY_MAP.get(facing), redstoneside) : this.getUpdatedState(worldIn, getDefaultState().with(POWER, stateIn.get(POWER)).with(FACING_PROPERTY_MAP.get(facing), redstoneside), currentPos);
        }
    }


    /**
     * Checks if all sides are valid.
     *
     * @param state The Current BlockState
     * @return If all sides of the BlockState have a valid value
     */
    private static boolean areAllSidesValid(BlockState state) {
        return state.get(NORTH).func_235921_b_() && state.get(SOUTH).func_235921_b_() && state.get(EAST).func_235921_b_() && state.get(WEST).func_235921_b_();
    }

    /**
     * Checks if all sides are invalid.
     *
     * @param state The Current BlockState
     * @return If none of the sides of the BlockState have a valid value
     */
    private static boolean areAllSidesInvalid(BlockState state) {
        return !state.get(NORTH).func_235921_b_() && !state.get(SOUTH).func_235921_b_() && !state.get(EAST).func_235921_b_() && !state.get(WEST).func_235921_b_();
    }


    /**
     * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
     * from the docs for {@link IWorldWriter#setBlockState(BlockPos, BlockState, int)}.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void updateDiagonalNeighbors(@NotNull BlockState state, @NotNull IWorld worldIn, @NotNull BlockPos pos, int flags, int recursionLeft) {
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = state.get(FACING_PROPERTY_MAP.get(direction));
            if (redstoneside != RedstoneSide.NONE && !worldIn.getBlockState(blockpos$mutable.setAndMove(pos, direction)).isIn(this)) {
                blockpos$mutable.move(Direction.DOWN);
                BlockState blockstate = worldIn.getBlockState(blockpos$mutable);
                if (!blockstate.isIn(Blocks.OBSERVER)) {
                    BlockPos blockpos = blockpos$mutable.offset(direction.getOpposite());
                    BlockState blockstate1 = blockstate.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos), worldIn, blockpos$mutable, blockpos);
                    replaceBlockState(blockstate, blockstate1, worldIn, blockpos$mutable, flags, recursionLeft);
                }

                blockpos$mutable.setAndMove(pos, direction).move(Direction.UP);
                BlockState blockstate3 = worldIn.getBlockState(blockpos$mutable);
                if (!blockstate3.isIn(Blocks.OBSERVER)) {
                    BlockPos blockpos1 = blockpos$mutable.offset(direction.getOpposite());
                    BlockState blockstate2 = blockstate3.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos1), worldIn, blockpos$mutable, blockpos1);
                    replaceBlockState(blockstate3, blockstate2, worldIn, blockpos$mutable, flags, recursionLeft);
                }
            }
        }

    }

    private RedstoneSide getSide(IBlockReader worldIn, BlockPos pos, Direction face) {
        return this.recalculateSide(worldIn, pos, face, !worldIn.getBlockState(pos.up()).isNormalCube(worldIn, pos));
    }

    private RedstoneSide recalculateSide(IBlockReader reader, BlockPos pos, Direction direction, boolean nonNormalCubeAbove) {
        BlockPos blockpos = pos.offset(direction);
        BlockState blockstate = reader.getBlockState(blockpos);
        if (nonNormalCubeAbove) {
            boolean flag = this.canPlaceOnTopOf(reader, blockpos, blockstate);
            if (flag && canConnectTo(reader.getBlockState(blockpos.up()), reader, blockpos.up(), null) ) {
                if (blockstate.isSolidSide(reader, blockpos, direction.getOpposite())) {
                    return RedstoneSide.UP;
                }

                return RedstoneSide.SIDE;
            }
        }

        return !canConnectTo(blockstate, reader, blockpos, direction) && (blockstate.isNormalCube(reader, blockpos) || !canConnectTo(reader.getBlockState(blockpos.down()), reader, blockpos.down(), null)) ? RedstoneSide.NONE : RedstoneSide.SIDE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isValidPosition(@NotNull BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.down();
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return this.canPlaceOnTopOf(worldIn, blockpos, blockstate);
    }

    private boolean canPlaceOnTopOf(IBlockReader reader, BlockPos pos, BlockState state) {
        return state.isSolidSide(reader, pos, Direction.UP) || state.isIn(Blocks.HOPPER);
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

    /**
     * @deprecated call via {@link BlockState#getStrongPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(@NotNull BlockState blockState, @NotNull IBlockReader blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return !this.canProvidePower ? 0 : blockState.getWeakPower(blockAccess, pos, side);
    }

    /**
     * @deprecated call via {@link BlockState#getWeakPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(@NotNull BlockState blockState, @NotNull IBlockReader blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        if (this.canProvidePower && side != Direction.DOWN) {
            int i = blockState.get(POWER);
            if (i == 0) {
                return 0;
            } else {
                return side != Direction.UP && !this.getUpdatedState(blockAccess, blockState, pos).get(FACING_PROPERTY_MAP.get(side.getOpposite())).func_235921_b_() ? 0 : i;
            }
        } else {
            return 0;
        }
    }

    protected static boolean canConnectTo(BlockState blockState, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        if (blockState.isIn(Blocks.REDSTONE_WIRE)) {
            return true;
        } else if (blockState.isIn(Blocks.REPEATER)) {
            Direction direction = blockState.get(RepeaterBlock.HORIZONTAL_FACING);
            return direction == side || direction.getOpposite() == side;
        } else if (blockState.isIn(Blocks.OBSERVER)) {
            return side == blockState.get(ObserverBlock.FACING);
        } else {
            return blockState.canConnectRedstone(world, pos, side) && side != null;
        }
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     * @deprecated call via {@link BlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(@NotNull BlockState state) {
        return this.canProvidePower;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getRGBByPower(int power) {
        Vector3f vector3f = powerColors[power];
        return MathHelper.rgb(vector3f.getX(), vector3f.getY(), vector3f.getZ());
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnPoweredParticle(World world, Random rand, BlockPos pos, Vector3f rgbVector, Direction directionFrom, Direction directionTo, float minChance, float maxChance) {
        float f = maxChance - minChance;
        if (!(rand.nextFloat() >= 0.2F * f)) {
            float f2 = minChance + f * rand.nextFloat();
            double d0 = 0.5D + (double)(0.4375F * (float)directionFrom.getXOffset()) + (double)(f2 * (float)directionTo.getXOffset());
            double d1 = 0.5D + (double)(0.4375F * (float)directionFrom.getYOffset()) + (double)(f2 * (float)directionTo.getYOffset());
            double d2 = 0.5D + (double)(0.4375F * (float)directionFrom.getZOffset()) + (double)(f2 * (float)directionTo.getZOffset());
            world.addParticle(new RedstoneParticleData(rgbVector.getX(), rgbVector.getY(), rgbVector.getZ(), 1.0F), (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method  will always be called regardless of whether the block can receive random update ticks
     */

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull Random rand) {
        int i = stateIn.get(POWER);
        if (i != 0) {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                RedstoneSide redstoneside = stateIn.get(FACING_PROPERTY_MAP.get(direction));
                switch(redstoneside) {
                    case UP:
                        this.spawnPoweredParticle(worldIn, rand, pos, powerColors[i], direction, Direction.UP, -0.5F, 0.5F);
                    case SIDE:
                        this.spawnPoweredParticle(worldIn, rand, pos, powerColors[i], Direction.DOWN, direction, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        this.spawnPoweredParticle(worldIn, rand, pos, powerColors[i], Direction.DOWN, direction, 0.0F, 0.3F);
                }
            }

        }
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link BlockState#rotate(Rotation)} whenever possible. Implementing/overriding is
     * fine.
     */
    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public BlockState rotate(@NotNull BlockState state, Rotation rot) {
        switch(rot) {
            case CLOCKWISE_180:
                return state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
            case COUNTERCLOCKWISE_90:
                return state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
            case CLOCKWISE_90:
                return state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
            default:
                return state;
        }
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link BlockState#mirror(Mirror)} whenever possible. Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public BlockState mirror(@NotNull BlockState state, Mirror mirrorIn) {
        switch(mirrorIn) {
            case LEFT_RIGHT:
                return state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
            case FRONT_BACK:
                return state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
            default:
                return super.mirror(state, mirrorIn);
        }
    }
}
