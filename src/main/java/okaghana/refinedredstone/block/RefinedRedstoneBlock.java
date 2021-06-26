package okaghana.refinedredstone.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
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
import okaghana.refinedredstone.setup.BlockRegister;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

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
        updateNetworkPower((World) world, currentPos);
        return world.getBlockState(currentPos);
    }


    // ---------------------------------------- //
    //               Power Logic                //
    // ---------------------------------------- //


    /** Will be called on an BlockUpdate (e.g. new Block placed next to it)
     *
     * This method will try to recalculate the Power of the whole Network. If we place a new power source which is already
     * stronger than the current level, we can just update the power of the whole network and spare some computing time.
     *
     * @param state Out BlockState
     * @param world The world the Block is in
     * @param pos The Position of this Block
     * @param block The Block that was there before
     * @param fromPos The Position the Neighbor is
     * @param isMoving If the neighbor is moving
     */
    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull BlockState state, World world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote && !(world.getBlockState(fromPos).getBlock() instanceof RefinedRedstoneBlock)) {
            if (state.isValidPosition(world, pos)) {
                Direction directionToSource = Direction.getFacingFromVector(pos.subtract(fromPos).getX(), pos.subtract(fromPos).getY(), pos.subtract(fromPos).getZ());
                if (world.getRedstonePower(fromPos, directionToSource) > state.get(POWER)) {
                    setNetworkPower(world, pos, world.getRedstonePower(fromPos, directionToSource));
                } else {
                    updateNetworkPower(world, pos);
                }
            } else {
                spawnDrops(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }


    /** Since we have the same power on all connected RefinedRedstoneBlocks, we always update our power according to all connected
     * Blocks, which we labeled as a "Network"
     *
     * 1 - First we basically set the power of the to 0 by setting {@link RefinedRedstoneBlock#canProvidePower} to false
     * 2 - We then find the maximum power of all Blocks in the Network
     * 3 - After that, set the power accordingly and turn {@link RefinedRedstoneBlock#canProvidePower} back on.
     *
     * @param world The world the Block is in
     * @param pos The Position of the Block (Used as an origin for the network)
     */
    private void updateNetworkPower(World world, BlockPos pos) {
        canProvidePower = false;

        int highestPower = getAllBlocksInNetwork(world, pos).stream()
                .map(blockPos -> getStrongestSignal(world, blockPos)).max(Integer::compareTo).orElse(0);

        setNetworkPower(world, pos, highestPower);
        canProvidePower = true;
    }


    /** Set the Power of every Block in the Network
     * @param world The World the Block is in
     * @param pos The Position of one of the Blocks
     * @param power The Power to set those Blocks to
     */
    private void setNetworkPower(World world, BlockPos pos, int power) {
        for (BlockPos position : getAllBlocksInNetwork(world, pos)) {
            world.setBlockState(position, world.getBlockState(pos).with(POWER, power));
        }
    }


    /** Return the Position of all Connected RefinedRedstoneBlocks
     * @param world The World the Block is in
     * @param pos The Position of one of the Blocks in the Network
     * @return An ArrayList with all BlockPositions
     */
    private List<BlockPos> getAllBlocksInNetwork(World world, BlockPos pos) {
        List<BlockPos> positions = new ArrayList<>();
        Queue<BlockPos> positionsToVisit = new ArrayDeque<>();
        positions.add(pos);
        positionsToVisit.add(pos);

        while (positionsToVisit.size() > 0) {
            BlockPos current = positionsToVisit.remove();

            // Loop over every neighbor and add them to both lists if they aren't/weren't already in there.
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.offset(direction);
                if (world.getBlockState(neighborPos).isIn(BlockRegister.REFINED_REDSTONE.get()) && !positions.contains(neighborPos)){
                    positionsToVisit.add(neighborPos);
                    positions.add(neighborPos);
                }
            }
        }

        return positions;
    }


    private int getStrongestSignal(World world, BlockPos pos) {
         return Arrays.stream(Direction.values())
                 .map(direction -> world.getRedstonePower(pos.offset(direction), direction.getOpposite()))
                 .max(Integer::compareTo).orElse(0);
    }
    

    /** Get the Strong Power of the Block */
    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(@NotNull BlockState blockState, @NotNull IBlockReader blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return canProvidePower ? blockState.get(POWER) : 0;
    }


    /** Get the Weak Power of the Block */
    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(@NotNull BlockState blockState, @NotNull IBlockReader blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        if (canProvidePower && blockState.get(DIRECTION_TO_PROPERTY.get(side))) {
            return blockState.get(POWER);
        }

        return 0;
    }


    /** This Block can provide Power! */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(@NotNull BlockState state) {
        return canProvidePower;
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
     * @param tintIndex Each block can have multiple tintable Textures. In this case it should always be 0 as we only have 1 tintIndex
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
