package okaghana.refinedredstone.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
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

    public static final VoxelShape SHAPE_UP = Block.makeCuboidShape(0, 14, 0, 16, 16, 16);
    public static final VoxelShape SHAPE_DOWN = Block.makeCuboidShape(0, 2, 0, 16, 0, 16);
    public static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(0, 0, 2, 16, 16, 0);
    public static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(14, 0, 0, 16, 16, 16);
    public static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(0, 0, 14, 16, 16, 16);
    public static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(2, 0, 0, 0, 16, 16);

    public static final BooleanProperty CONNECTED_UP = BooleanProperty.create("connected_up");
    public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("connected_down");
    public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.create("connected_north");
    public static final BooleanProperty CONNECTED_EAST = BooleanProperty.create("connected_east");
    public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.create("connected_south");
    public static final BooleanProperty CONNECTED_WEST = BooleanProperty.create("connected_west");
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
    public static final Map<Direction, BooleanProperty> DIRECTION_TO_PROPERTY = ImmutableMap.<Direction, BooleanProperty>builder()
            .put(Direction.UP, CONNECTED_UP).put(Direction.DOWN, CONNECTED_DOWN).put(Direction.NORTH, CONNECTED_NORTH)
            .put(Direction.EAST, CONNECTED_EAST).put(Direction.SOUTH, CONNECTED_SOUTH).put(Direction.WEST, CONNECTED_WEST).build();

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
        this.setDefaultState(this.stateContainer.getBaseState().with(CONNECTED_UP, false).with(CONNECTED_DOWN, false)
                .with(CONNECTED_NORTH, false).with(CONNECTED_EAST, false).with(CONNECTED_SOUTH, false)
                .with(CONNECTED_WEST, false).with(POWER, 0));

        for(int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0F;
            float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float f3 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            powerColors[i] = new Vector3f(f1, f2, f3);
        }
    }

    /**
     * This will register the desired BlockStates for this Block to the game.
     */
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER, CONNECTED_UP, CONNECTED_DOWN, CONNECTED_NORTH, CONNECTED_EAST, CONNECTED_SOUTH, CONNECTED_WEST);
    }


    /**
     * Returns the shape of the block.
     *
     * The image that you see on the screen (when a block is rendered) is determined by the block model (i.e. the model json file).
     * But Minecraft also uses a number of other "shapes" to control the interaction of the block with its environment and with the player
     * (basically the HitBox for the gray wireframe when you look at a block).
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
        VoxelShape shape = VoxelShapes.empty();

        if (state.get(CONNECTED_UP)) { shape = VoxelShapes.or(shape, SHAPE_UP); }
        if (state.get(CONNECTED_DOWN)) { shape = VoxelShapes.or(shape, SHAPE_DOWN); }
        if (state.get(CONNECTED_NORTH)) { shape = VoxelShapes.or(shape, SHAPE_NORTH); }
        if (state.get(CONNECTED_EAST)) { shape = VoxelShapes.or(shape, SHAPE_EAST); }
        if (state.get(CONNECTED_SOUTH)) { shape = VoxelShapes.or(shape, SHAPE_SOUTH); }
        if (state.get(CONNECTED_WEST)) { shape = VoxelShapes.or(shape, SHAPE_WEST); }

        return shape;
    }


    /**
     * Calculates the BlockState for a Block when it's placed. Since want to be able to place multiple
     * wires on the same stop, we also override isReplaceable. In that case we update the Properties accordingly.
     *
     * Context->getFace: The actual face the player is looking at (Basically opposite of the viewing direction)
     * Context->getPos: The position of the new Block (most of the time air)
     *
     * @param context The context in which the block has been placed
     * @return The BlockState of the new Block
     */
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction face = context.getFace();
        BlockPos pos = context.getPos();
        World world = context.getWorld();

        if (world.getBlockState(pos).isIn(this)) {
            return world.getBlockState(pos).with(DIRECTION_TO_PROPERTY.get(face.getOpposite()), true);
        }

        BlockState state = getDefaultState().with(DIRECTION_TO_PROPERTY.get(face.getOpposite()), true);
        if (state.isValidPosition(world, pos)) {
            return state;
        }

        return null;
    }


    /**
     * Test if each connection for a block is attached to a solid Face
     *
     * @param state The BlockState of the block to be tested
     * @param world The World the block is in
     * @param pos The P
     * @return Whether all faces can connect to a solid face
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isValidPosition(@NotNull BlockState state, @NotNull IWorldReader world, @NotNull BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (state.get(DIRECTION_TO_PROPERTY.get(direction))) {
                // Return false if the attachment-block doesn't have a solid side
                BlockPos other = pos.offset(direction);
                if (!world.getBlockState(other).isSolidSide(world, other, direction.getOpposite())) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * Called whenever a Block is right-clicked on this Block
     *
     * In this case, when the player uses another RefinedRedstoneBlock, we want to add another connection to the
     * existing block. (Is done in GetStateForPlacement)
     *
     * @param state The state of the block that was right-clicked
     * @param useContext The context of the block in the players hand
     * @return Whether the new Block can replace the old one
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isReplaceable(@NotNull BlockState state, BlockItemUseContext useContext) {
        ItemStack itemstack = useContext.getItem();
        return itemstack.getItem() == this.asItem();
    }


    /**
     * After we place the Block we calculate the Power for this block
     *
     * @param state The state of the Block
     * @param facing ?
     * @param facingState ?
     * @param world The World the Block is in
     * @param currentPos The Position of the Block
     * @param facingPos ?
     * @return The new BlockState with the updated Power
     */
    @SuppressWarnings("deprecation")
    @Override
    @NotNull
    public BlockState updatePostPlacement(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull IWorld world, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        return state.with(POWER, getStrongestSignal((World) world, currentPos));
    }


    //    /**
//     * This is called whenever the BlockState changes or the Block is broken (replaced with air)
//     *
//     * @param oldState The BlockState of the old Block (sill always be a RefinedRedstoneBlock)
//     * @param worldIn What world the block is in
//     * @param pos The Position in the world
//     * @param newState The new State of the Block
//     * @param isMoving Whether the block was moved instead of destroyed
//     */
//    @SuppressWarnings("deprecation")
//    @Override
//    public void onReplaced(@NotNull BlockState oldState, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
//         if (!isMoving && !worldIn.isRemote) {
//            for(Direction direction : Direction.values()) {
//                worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
//            }
//
//            this.updatePower(worldIn, pos, oldState);
//            this.updateNeighboursStateChange(worldIn, pos);
//         }
//    }



    // ---------------------------------------- //
    //               Power Logic                //
    // ---------------------------------------- //


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
//        boolean flag = areAllSidesInvalid(state);
//        state = this.recalculateFacingState(reader, this.getDefaultState().with(POWER, state.get(POWER)), pos);
//        if (!flag || !areAllSidesInvalid(state)) {
//            boolean flag1 = state.get(NORTH).func_235921_b_();
//            boolean flag2 = state.get(SOUTH).func_235921_b_();
//            boolean flag3 = state.get(EAST).func_235921_b_();
//            boolean flag4 = state.get(WEST).func_235921_b_();
//            boolean flag5 = !flag1 && !flag2;
//            boolean flag6 = !flag3 && !flag4;
//            if (!flag4 && flag5) {
//                state = state.with(WEST, RedstoneSide.SIDE);
//            }
//
//            if (!flag3 && flag5) {
//                state = state.with(EAST, RedstoneSide.SIDE);
//            }
//
//            if (!flag1 && flag6) {
//                state = state.with(NORTH, RedstoneSide.SIDE);
//            }
//
//            if (!flag2 && flag6) {
//                state = state.with(SOUTH, RedstoneSide.SIDE);
//            }
//
//        }
        return state;
    }

    private BlockState recalculateFacingState(IBlockReader reader, BlockState state, BlockPos pos) {
//        boolean flag = !reader.getBlockState(pos.up()).isNormalCube(reader, pos);
//
//        for(Direction direction : Direction.Plane.HORIZONTAL) {
//            if (!state.get(FACING_PROPERTY_MAP.get(direction)).func_235921_b_()) {
//                RedstoneSide redstoneside = this.recalculateSide(reader, pos, direction, flag);
//                state = state.with(FACING_PROPERTY_MAP.get(direction), redstoneside);
//            }
//        }

        return state;
    }


    /**
     * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
     * from the docs for {@link IWorldWriter#setBlockState(BlockPos, BlockState, int)}.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void updateDiagonalNeighbors(@NotNull BlockState state, @NotNull IWorld worldIn, @NotNull BlockPos pos, int flags, int recursionLeft) {
        BlockPos.Mutable posMutable = new BlockPos.Mutable();

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            boolean isConnected = state.get(DIRECTION_TO_PROPERTY.get(direction));
            if (isConnected && !worldIn.getBlockState(posMutable.setAndMove(pos, direction)).isIn(this)) {
                posMutable.move(Direction.DOWN);
                BlockState blockstate = worldIn.getBlockState(posMutable);
                if (!blockstate.isIn(Blocks.OBSERVER)) {
                    BlockPos blockpos = posMutable.offset(direction.getOpposite());
                    BlockState blockstate1 = blockstate.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos), worldIn, posMutable, blockpos);
                    replaceBlockState(blockstate, blockstate1, worldIn, posMutable, flags, recursionLeft);
                }

                posMutable.setAndMove(pos, direction).move(Direction.UP);
                BlockState blockstate3 = worldIn.getBlockState(posMutable);
                if (!blockstate3.isIn(Blocks.OBSERVER)) {
                    BlockPos blockpos1 = posMutable.offset(direction.getOpposite());
                    BlockState blockstate2 = blockstate3.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos1), worldIn, posMutable, blockpos1);
                    replaceBlockState(blockstate3, blockstate2, worldIn, posMutable, flags, recursionLeft);
                }
            }
        }

    }


    private void updatePower(World world, BlockPos pos, BlockState state) {
        int i = getStrongestSignal(world, pos);
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
        return Arrays.stream(Direction.values()).map(direction -> world.getRedstonePower(pos.offset(direction), direction.getOpposite())).max(Integer::compareTo).orElse(0);
//        this.canProvidePower = false;
//        int i = world.getRedstonePowerFromNeighbors(pos);
//        this.canProvidePower = true;
//        int j = 0;
//        if (i < 15) {
//            for(Direction direction : Direction.Plane.HORIZONTAL) {
//                BlockPos blockpos = pos.offset(direction);
//                BlockState blockstate = world.getBlockState(blockpos);
//                j = Math.max(j, this.getPower(blockstate));
//                BlockPos blockpos1 = pos.up();
//                if (blockstate.isNormalCube(world, blockpos) && !world.getBlockState(blockpos1).isNormalCube(world, blockpos1)) {
//                    j = Math.max(j, this.getPower(world.getBlockState(blockpos.up())));
//                } else if (!blockstate.isNormalCube(world, blockpos)) {
//                    j = Math.max(j, this.getPower(world.getBlockState(blockpos.down())));
//                }
//            }
//        }
//
//        return Math.max(i, j - 1);
    }

    /**
     * @deprecated call via {@link BlockState#getStrongPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(@NotNull BlockState blockState, @NotNull IBlockReader blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return /*this.canProvidePower ?*/ blockState.getWeakPower(blockAccess, pos, side); // : 0;
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
            return !getUpdatedState(blockAccess, blockState, pos).get(DIRECTION_TO_PROPERTY.get(side.getOpposite())) ? 0 : i;
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
        return true; //this.canProvidePower;
    }



    // ------------------------------
    //           Visuals
    // ------------------------------

    /**
     * Tint the Block according to the current Power
     *    0 = Dark Red
     *    15 = Bright Red
     *
     * @param state The BlockState of the Block
     * @param blockDisplayReader The DisplayReader
     * @param blockPos The Position of the Block
     * @param tintIndex Each block can have multiple tintable Textures. In this case it should always be 0 as we only have 1 tintindex
     * @return An RGBA-Value converted to an Integer. Each 8-bits of the 32-bit Integer represents a color between 0-255.
     */
    @Override
    public int getColor(BlockState state, @Nullable IBlockDisplayReader blockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        Vector3f vector3f = powerColors[state.get(POWER)];
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
    public void animateTick(BlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull Random rand) {
        int i = state.get(POWER);
        if (i != 0) {
            for(Direction direction : Direction.values()) {
                if (state.get(DIRECTION_TO_PROPERTY.get(direction))) {
                    spawnPoweredParticle(world, rand, pos, powerColors[i], direction, Direction.UP, 0.0f, 0.5f);
                }
            }

        }
    }
}
