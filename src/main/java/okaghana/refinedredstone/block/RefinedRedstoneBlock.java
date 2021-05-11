package okaghana.refinedredstone.block;

import com.google.common.collect.*;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import okaghana.refinedredstone.setup.BlockRegister;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import okaghana.refinedredstone.setup.TileEntityRegister;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This is the main block of the mod and serves as an improved version of the default redstone wire. <br><br>
 *
 * All methods originally copied from the basic {@link RedstoneWireBlock}, as we want to ensure the same behaviour and functionality
 * as the default Redstone and only want to change this where we want to add features
 */
public class RefinedRedstoneBlock extends Block {

    public static final Properties PROPERTIES = Block.Properties.create(Material.ROCK).hardnessAndResistance(0.05f).doesNotBlockMovement().harvestLevel(0);

    public static final DirectionProperty FACE = BlockStateProperties.FACING;
    public static final BooleanProperty CONNECTION_UP = BooleanProperty.create("connected_up");
    public static final BooleanProperty CONNECTION_DOWN = BooleanProperty.create("connected_down");
    public static final BooleanProperty CONNECTION_LEFT = BooleanProperty.create("connected_left");
    public static final BooleanProperty CONNECTION_RIGHT = BooleanProperty.create("connected_right");
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

    private static final Table<RedstoneSide, Direction, VoxelShape> REDUCED_SHAPES = HashBasedTable.create(2, 5);  // VoxelShapes to define the HitBox

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

        // Set default state (all disconnected)
        setDefaultState(this.stateContainer.getBaseState().with(POWER, 0).with(CONNECTION_UP, false)
                .with(CONNECTION_DOWN, false).with(CONNECTION_LEFT, false).with(CONNECTION_RIGHT, false));

        // Initializing Colors
        for(int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0F;
            float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float f3 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            powerColors[i] = new Vector3f(f1, f2, f3);
        }

        // Precalculate the required VoxelShapes
        REDUCED_SHAPES.put(RedstoneSide.SIDE, Direction.DOWN,  Block.makeCuboidShape(6, 0, 6, 10, 2, 10));
        REDUCED_SHAPES.put(RedstoneSide.SIDE, Direction.NORTH, Block.makeCuboidShape(6, 0, 0, 10, 2, 6));
        REDUCED_SHAPES.put(RedstoneSide.SIDE, Direction.EAST,  Block.makeCuboidShape(10, 0, 6, 16, 2, 10));
        REDUCED_SHAPES.put(RedstoneSide.SIDE, Direction.SOUTH, Block.makeCuboidShape(6, 0, 10, 10, 2, 16));
        REDUCED_SHAPES.put(RedstoneSide.SIDE, Direction.WEST,  Block.makeCuboidShape(0, 0, 6, 6, 2, 10));

        REDUCED_SHAPES.put(RedstoneSide.UP, Direction.NORTH, Block.makeCuboidShape(6, 0, 0, 10, 16, 2));
        REDUCED_SHAPES.put(RedstoneSide.UP, Direction.EAST,  Block.makeCuboidShape(16, 0, 6, 14, 16, 10));
        REDUCED_SHAPES.put(RedstoneSide.UP, Direction.SOUTH, Block.makeCuboidShape(6, 0, 16, 10, 16, 14));
        REDUCED_SHAPES.put(RedstoneSide.UP, Direction.WEST,  Block.makeCuboidShape(0, 0, 6, 2, 16, 10));
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityRegister.REFINED_REDSTONE.get().create();
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
        switch (state.get(FACE)){
            case DOWN: return Block.makeCuboidShape(0, 0, 0, 16, 2, 16);
            case NORTH: return Block.makeCuboidShape(0, 0, 0, 16, 16, 2);
            case WEST: return Block.makeCuboidShape(0, 0, 0, 2, 16, 16);
            case UP: return Block.makeCuboidShape(16, 16, 16, 0, 14, 0);
            case SOUTH: return Block.makeCuboidShape(16, 16, 16, 0, 0, 14);
            case EAST: return Block.makeCuboidShape(16, 16, 16, 14, 0, 0);
            default: return VoxelShapes.empty();
        }
    }


    // ---------------------------------------- //
    //            BlockState Logic              //
    // ---------------------------------------- //


    /**
     * This will register the desired BlockState properties for this Block to the game.
     */
    @Override
    protected void fillStateContainer(StateContainer.@NotNull Builder<Block, BlockState> builder) {
        builder.add(FACE, POWER, CONNECTION_UP, CONNECTION_DOWN, CONNECTION_LEFT, CONNECTION_RIGHT);
    }


    /**
     * Generate the BlockState when placing the block.
     *
     * @param context   The Context of the placed block
     * @return          The BlockState at placing
     */
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACE, context.getFace().getOpposite());
    }


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
     * @param newState  The State of the new Block on this position (Minecraft:air when destroyed)
     * @param isMoving  Whether the block is moving
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(@NotNull BlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!isMoving && !state.isIn(newState.getBlock())) {
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

    /**
     * Update the blockstate of this block depending on neighbors
     *
     * @param reader    The BlockReader, often the world the block is in
     * @param state     The initial blockstate
     * @param pos       The Position in the world
     * @return          The Updated Blockstate
     */
    private BlockState getUpdatedState(IBlockReader reader, BlockState state, BlockPos pos) {
        state = this.getDefaultState().with(POWER, state.get(POWER));
        boolean noBlockAbove = !reader.getBlockState(pos.up()).isNormalCube(reader, pos);

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
            return redstoneside.func_235921_b_() == stateIn.get(CONNECTION_UP) && !allSidesConnected(stateIn) ? stateIn.with(CONNECTION_UP, false) : this.getUpdatedState(worldIn, getDefaultState().with(POWER, stateIn.get(POWER)).with(CONNECTION_DOWN, false), currentPos);
        }
    }


    /**
     * Checks if all sides are connected
     *
     * @param state The Current BlockState
     * @return If all sides of the BlockState have {@link RedstoneSide#SIDE} or {@link RedstoneSide#UP} as value
     */
    private static boolean allSidesConnected(BlockState state) {
        return state.get(CONNECTION_UP) && state.get(CONNECTION_DOWN) && state.get(CONNECTION_LEFT) && state.get(CONNECTION_RIGHT);
    }


    /**
     * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
     * from the docs for {@link IWorldWriter#setBlockState(BlockPos, BlockState, int)}.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void updateDiagonalNeighbors(@NotNull BlockState state, @NotNull IWorld worldIn, @NotNull BlockPos pos, int flags, int recursionLeft) {
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

//        for(Direction direction : Direction.Plane.HORIZONTAL) {
//            RedstoneSide redstoneside = state.get(FACING_PROPERTY_MAP.get(direction));
//            if (redstoneside != RedstoneSide.NONE && !worldIn.getBlockState(blockpos$mutable.setAndMove(pos, direction)).isIn(this)) {
//                blockpos$mutable.move(Direction.DOWN);
//                BlockState blockstate = worldIn.getBlockState(blockpos$mutable);
//                if (!blockstate.isIn(Blocks.OBSERVER)) {
//                    BlockPos blockpos = blockpos$mutable.offset(direction.getOpposite());
//                    BlockState blockstate1 = blockstate.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos), worldIn, blockpos$mutable, blockpos);
//                    replaceBlockState(blockstate, blockstate1, worldIn, blockpos$mutable, flags, recursionLeft);
//                }
//
//                blockpos$mutable.setAndMove(pos, direction).move(Direction.UP);
//                BlockState blockstate3 = worldIn.getBlockState(blockpos$mutable);
//                if (!blockstate3.isIn(Blocks.OBSERVER)) {
//                    BlockPos blockpos1 = blockpos$mutable.offset(direction.getOpposite());
//                    BlockState blockstate2 = blockstate3.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos1), worldIn, blockpos$mutable, blockpos1);
//                    replaceBlockState(blockstate3, blockstate2, worldIn, blockpos$mutable, flags, recursionLeft);
//                }
//            }
//        }

    }

    private RedstoneSide getSide(IBlockReader worldIn, BlockPos pos, Direction face) {
        return this.recalculateSide(worldIn, pos, face, !worldIn.getBlockState(pos.up()).isNormalCube(worldIn, pos));
    }

    /**
     * Recalculate the position from this block to the block in the given direction
     *
     * @param reader            The BlockReader, most of the time the world
     * @param pos               The Position of this block
     * @param direction         The Direction towards the neighbor
     * @param noFullCubeAbove   If a solid Cube is above
     * @return                  The new RedstoneSide in the given direction
     */
    private RedstoneSide recalculateSide(IBlockReader reader, BlockPos pos, Direction direction, boolean noFullCubeAbove) {
        BlockPos neighborPos = pos.offset(direction);
        BlockState neighborState = reader.getBlockState(neighborPos);

        // If there is a solid block above this block, we don't want to check for upward connections
        if (noFullCubeAbove) {
            boolean canBePlacedOnTop = this.canPlaceOnTopOf(reader, neighborPos, neighborState);
            boolean canConnectTo = canConnectTo(reader.getBlockState(neighborPos.up()), reader, neighborPos.up(), null);
            boolean solidWall = neighborState.isSolidSide(reader, neighborPos, direction.getOpposite());

            if (canBePlacedOnTop && canConnectTo ) {
                if (solidWall) return RedstoneSide.UP;
                return RedstoneSide.SIDE;
            }
        }

        boolean canConnectToNeighbor = canConnectTo(neighborState, reader, neighborPos, direction);
        boolean canConnectBelow = canConnectTo(reader.getBlockState(neighborPos.down()), reader, neighborPos.down(), null);
        return !canConnectToNeighbor && (neighborState.isNormalCube(reader, neighborPos) || !canConnectBelow) ? RedstoneSide.NONE : RedstoneSide.SIDE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isValidPosition(@NotNull BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.down();
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return this.canPlaceOnTopOf(worldIn, blockpos, blockstate);
    }


    /**
     * If Redstone can be placed on top of the block. <br>
     * TODO: Make a list of Blocks that should be placeable
     *
     * @param reader    The BlockReader
     * @param pos       The Position of the Block in Question
     * @param state     The Blockstate of that block
     * @return          If RefinedRedstone can be placed on top
     */
    private boolean canPlaceOnTopOf(IBlockReader reader, BlockPos pos, BlockState state) {
        return state.isSolidSide(reader, pos, Direction.UP) || state.isIn(Blocks.HOPPER);
    }

    protected static boolean canConnectTo(BlockState blockState, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        if (blockState.isIn(Blocks.REDSTONE_WIRE) || blockState.isIn(BlockRegister.REFINED_REDSTONE.get())) {
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

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    // ----------------------------- //
    //           Power Logic         //
    // ------------------------------//


    /**
     * Update the Power of the Block
     *
     * @param world The World with the block
     * @param pos   The Position of the Block
     * @param state The state of the Block
     */
    private void updatePower(World world, BlockPos pos, BlockState state) {
        //BlockPos sourcePosition = pos.offset(getPowerFrom);
        //BlockState sourceState = world.getBlockState(sourcePosition);
        //if (getPower(sourceState) == 0) {
            //world.setBlockState(pos, state.with(POWER, 0));
        //}

        int strongestSignal = this.getStrongestSignal(world, pos);

        if (state.get(POWER) != strongestSignal) {
            if (world.getBlockState(pos) == state) {
                world.setBlockState(pos, state.with(POWER, strongestSignal), 2);
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
        int powerFromNeighbors = world.getRedstonePowerFromNeighbors(pos);
        this.canProvidePower = true;

        //if (powerFromNeighbors == 15) {
        //    return powerFromNeighbors;
        //}

        int max = 0;
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.offset(direction);
            BlockState blockstate = world.getBlockState(blockpos);
            max = Math.max(max, this.getPower(blockstate));

            BlockPos blockpos1 = pos.up();
            if (blockstate.isNormalCube(world, blockpos) && !world.getBlockState(blockpos1).isNormalCube(world, blockpos1)) {
                max = Math.max(max, this.getPower(world.getBlockState(blockpos.up())));
            } else if (!blockstate.isNormalCube(world, blockpos)) {
                max = Math.max(max, this.getPower(world.getBlockState(blockpos.down())));
            }
        }

        return Math.max(powerFromNeighbors, max);
    }

    /**
     * @deprecated call via {@link BlockState#getStrongPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(@NotNull BlockState blockState, @NotNull IBlockReader blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return this.canProvidePower ? blockState.getWeakPower(blockAccess, pos, side) : 0;
    }

    /**
     * @deprecated call via {@link BlockState#getWeakPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(@NotNull BlockState blockState, @NotNull IBlockReader blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        if (this.canProvidePower && side != Direction.DOWN) {
            int power = blockState.get(POWER);

            if (power == 0) {
                return 0;
            } else {
                return side != Direction.UP && !this.getUpdatedState(blockAccess, blockState, pos).get(CONNECTION_UP) ? 0 : power;
            }
        } else {
            return 0;
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
    public BlockState rotate(@NotNull BlockState state, @NotNull Rotation rot) {
        return state;
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link BlockState#mirror(Mirror)} whenever possible. Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirrorIn) {
        return state;
    }
}
