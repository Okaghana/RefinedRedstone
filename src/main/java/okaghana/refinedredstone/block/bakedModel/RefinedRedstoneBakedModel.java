package okaghana.refinedredstone.block.bakedModel;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import okaghana.refinedredstone.block.RefinedRedstoneBlock;
import okaghana.refinedredstone.setup.BlockRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RefinedRedstoneBakedModel implements IDynamicBakedModel {
    // Holds the identifiers for the ModelData which stores which parts of the model need to be rendered
    // The first key is the side of the Block, the second which part (the core or a "arm")
    public final Table<Direction, ConnectionPart, ModelProperty<Boolean>> modelProperties;

    public enum ConnectionPart {
        CENTER, UP, DOWN, LEFT, RIGHT;

        public static ConnectionPart[] ARMS = new ConnectionPart[]{UP, DOWN, LEFT, RIGHT};

        public static Direction PartToDirection(ConnectionPart part, Direction facing) {
            if (part == ConnectionPart.UP) {
                if (Direction.Plane.HORIZONTAL.test(facing)) {
                    return Direction.UP;
                }
                return Direction.NORTH;
            } else if (part == ConnectionPart.DOWN) {
                if (Direction.Plane.HORIZONTAL.test(facing)) {
                    return Direction.DOWN;
                }
                return Direction.SOUTH;
            } else if (part == ConnectionPart.LEFT) {
                switch (facing) {
                    case DOWN: case UP: case NORTH: return Direction.WEST;
                    case SOUTH: return Direction.EAST;
                    case WEST: return Direction.SOUTH;
                    case EAST: return Direction.NORTH;
                }
            } else if (part == ConnectionPart.RIGHT) {
                switch (facing) {
                    case DOWN: case UP: case NORTH: return Direction.EAST;
                    case SOUTH: return Direction.WEST;
                    case WEST: return Direction.NORTH;
                    case EAST: return Direction.SOUTH;
                }
            }
            throw new IllegalStateException("Unexpected ConnectionsPart: " + part);
        }
    }

    // Things for rendering the Blocks
    public final ResourceLocation modelLocation = new ResourceLocation("refinedredstone:block/refined_redstone");
    public final TextureAtlasSprite texture = ModelLoader.instance().getSpriteMap().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).getSprite(modelLocation);
    public final FaceBakery bakery = new FaceBakery();

    // Core (On the Ground)
    public final Vector3f coreFrom = new Vector3f(6, 0, 6);
    public final Vector3f coreTo = new Vector3f(10, 2, 10);

    // Arm (On the Ground pointing North)
    public final Vector3f armFrom = new Vector3f(6, 0, 0);
    public final Vector3f armTo = new Vector3f(10, 2, 6);

    // UV Mappings
    public final BlockPartFace UVCenter = new BlockPartFace(null, 0, "refinedredstone:block/refined_redstone", new BlockFaceUV(new float[]{0, 0, 4, 4}, 0));
    public final BlockPartFace UVFront = new BlockPartFace(null, 0, "refinedredstone:block/refined_redstone", new BlockFaceUV(new float[]{0, 0, 4, 2}, 0));
    public final BlockPartFace UVSide = new BlockPartFace(null, 0, "refinedredstone:block/refined_redstone", new BlockFaceUV(new float[]{0, 0, 6, 2}, 0));
    public final BlockPartFace UVArm = new BlockPartFace(null, 0, "refinedredstone:block/refined_redstone", new BlockFaceUV(new float[]{0, 0, 6, 4}, 0));

    public RefinedRedstoneBakedModel(IBakedModel previousModel) {
        // Build the ModelProperties which serve as identifiers
        ImmutableTable.Builder<Direction, ConnectionPart, ModelProperty<Boolean>> builder = ImmutableTable.builder();
        for (Direction direction : Direction.values()) {
            for (ConnectionPart part : ConnectionPart.values()) {
                builder.put(direction, part, new ModelProperty<>());
            }
        }
        modelProperties = builder.build();
    }


    // getModelData --------------------------------------------------------------------------------------------------------------------------------

    /**
     * The first method to be called. Generate the ModelData for the actual rendering in getQuads. We will decide what parts of
     * out model will actually be rendered here and the getQuads function will then generate those models accordingly
     *
     * @param world The world the Block is in
     * @param pos The position of the Block
     * @param state The BlockState
     * @param tileData The existing ModelData
     * @return The ModelData that will be passed to the getQuads() method
     */
    @Override
    @NotNull
    public IModelData getModelData(@NotNull IBlockDisplayReader world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData tileData) {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();

        for (Direction direction : Direction.values()) {
            Map<ConnectionPart, Boolean> connections = getConnectionsForDirection(state, pos, world, direction);
            for (Map.Entry<ConnectionPart, Boolean> part : connections.entrySet()) {
                builder.withInitial(modelProperties.get(direction, part.getKey()), part.getValue());
            }
        }

        return builder.build();
    }


    /**
     * Generate what Connection a given Side needs to render
     *
     * @param state The State of the Block
     * @param pos This Position of a Block
     * @param world The World the Block is in
     * @param direction The Direction we want to calculate
     * @return A Map for each direction if this part is going to be rendered
     */
    private Map<ConnectionPart, Boolean> getConnectionsForDirection(BlockState state, BlockPos pos, IBlockDisplayReader world, Direction direction) {
        Map<ConnectionPart, Boolean> map = new EnumMap<>(ConnectionPart.class);
        map.put(ConnectionPart.CENTER, state.get(RefinedRedstoneBlock.DIRECTION_TO_PROPERTY.get(direction)));

        // Connection Up
        map.put(ConnectionPart.UP, hasNeighborTowards(world, direction, pos, ConnectionPart.UP));
        map.put(ConnectionPart.DOWN, hasNeighborTowards(world, direction, pos, ConnectionPart.DOWN));
        map.put(ConnectionPart.LEFT, hasNeighborTowards(world, direction, pos, ConnectionPart.LEFT));
        map.put(ConnectionPart.RIGHT, hasNeighborTowards(world, direction, pos, ConnectionPart.RIGHT));
        return map;
    }


    /**
     * Decide if a RefineRedstone Block need to have a given arm whilst connected to the given face.
     *
     * @param world The world the Block is in
     * @param direction The Direction we are concerned about
     * @param pos The position of the Block
     * @param part The arm we are concerned about (Must not be CENTER)
     * @return Whether this part needs to be rendered
     */
    private boolean hasNeighborTowards(IBlockDisplayReader world, Direction direction, BlockPos pos, ConnectionPart part) {
        Direction offset = ConnectionPart.PartToDirection(part, direction);
        RefinedRedstoneBlock block = BlockRegister.REFINED_REDSTONE.get();

        boolean centerOnThisSide = world.getBlockState(pos).get(RefinedRedstoneBlock.DIRECTION_TO_PROPERTY.get(direction));
        boolean sameBlockHasCenterThere = world.getBlockState(pos).get(RefinedRedstoneBlock.DIRECTION_TO_PROPERTY.get(offset));

        // If there is another RefinedRedstone on the same level with a connection in the same Direction
        boolean otherHasCenterOnSameSide = world.getBlockState(pos.offset(offset)).isIn(block) &&
                world.getBlockState(pos.offset(offset)).get(RefinedRedstoneBlock.DIRECTION_TO_PROPERTY.get(direction));

        // If there is a RefinedRedstone "one Below" (Only for Direction.Down truly below) that has a perpendicular connection towards us
        boolean belowHasCenterTowardsUs = world.getBlockState(pos.offset(offset).offset(direction)).isIn(block) &&
                world.getBlockState(pos.offset(offset).offset(direction)).get(RefinedRedstoneBlock.DIRECTION_TO_PROPERTY.get(offset.getOpposite()));

        return centerOnThisSide && (sameBlockHasCenterThere || otherHasCenterOnSameSide || belowHasCenterTowardsUs);
    }

    // getQuads ---------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Generate the Quads (Faces) for a Block using the ModelData above
     *
     * @param state The BlockState of that Block
     * @param side ?
     * @param rand RNG
     * @param extraData The Data we calculated above
 * @return A List of all Baked Quads
     */
    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();

        for (Direction direction : Direction.values()){
            if (extraData.getData(modelProperties.get(direction, ConnectionPart.CENTER))){
                quads.addAll(generateCore(direction));

                // Render Arms
                for (ConnectionPart part : ConnectionPart.ARMS) {
                    if (extraData.getData(modelProperties.get(direction, part))) {
                        quads.addAll(generateArm(direction, part));
                    }
                }
            }
        }

        return quads;
    }


    /**
     * The Texture of the Block (e.g. when it's broken)
     * @return The Sprite
     */
    @Override
    @NotNull
    public TextureAtlasSprite getParticleTexture() {
        return texture;
    }


    /**
     * Generate the Quads for a Center on the given Direction
     * @param direction The Direction we want to render
     * @return A List with all generated Quads
     */
    @NotNull
    private List<BakedQuad> generateCore(@NotNull Direction direction) {
        IModelTransform transform;

        switch (direction) {
            case DOWN: transform = ModelRotation.X0_Y0; break;
            case UP: transform = ModelRotation.X180_Y0; break;
            case NORTH: transform = ModelRotation.X270_Y0; break;
            case SOUTH: transform = ModelRotation.X90_Y0; break;
            case WEST: transform = ModelRotation.X90_Y90; break;
            case EAST: transform = ModelRotation.X90_Y270; break;
            default: throw new IllegalStateException("Unexpected direction: " + direction);
        }

        List<BakedQuad> quads = new ArrayList<>(6);
        quads.add(bakery.bakeQuad(coreFrom, coreTo, UVCenter, texture, Direction.UP, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(coreFrom, coreTo, UVCenter, texture, Direction.DOWN, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(coreFrom, coreTo, UVFront, texture, Direction.NORTH, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(coreFrom, coreTo, UVFront, texture, Direction.EAST, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(coreFrom, coreTo, UVFront, texture, Direction.SOUTH, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(coreFrom, coreTo, UVFront, texture, Direction.WEST, transform, null, true, modelLocation));

        return quads;
    }


    /**
     * Generate the Quads for an Arm on the given Direction
     * @param direction The Direction we want to render
     * @param part What arm we want to render
     * @return A List with all generated Quads
     */
    @NotNull
    private List<BakedQuad> generateArm(@NotNull Direction direction, ConnectionPart part) {
        Quaternion rotation;

        if (part == ConnectionPart.UP) {
            switch (direction) {
                case DOWN: rotation = new Quaternion(0, 0, 0, true); break;
                case UP: rotation = new Quaternion(0, 0, 180, true); break;
                case NORTH: rotation = new Quaternion(90, 0, 0, true); break;
                case SOUTH: rotation = new Quaternion(270, 180, 0, true); break;
                case WEST: rotation = new Quaternion(90, 0, 270, true); break;
                case EAST: rotation = new Quaternion(90, 0, 90, true); break;
                default: throw new IllegalStateException("Unexpected direction: " + direction);
            }
        } else if (part == ConnectionPart.DOWN) {
            switch (direction) {
                case DOWN: rotation = new Quaternion(0, 180, 0, true); break;
                case UP: rotation = new Quaternion(0, 180, 180, true); break;
                case NORTH: rotation = new Quaternion(90, 180, 0, true); break;
                case SOUTH: rotation = new Quaternion(270, 0, 0, true); break;
                case WEST: rotation = new Quaternion(270, 0, 270, true); break;
                case EAST: rotation = new Quaternion(270, 0, 90, true); break;
                default: throw new IllegalStateException("Unexpected direction: " + direction);
            }
        } else if (part == ConnectionPart.LEFT) {
            switch (direction) {
                case DOWN: rotation = new Quaternion(0, 90, 0, true); break;
                case UP: rotation = new Quaternion(180, 90, 0, true); break;
                case NORTH: rotation = new Quaternion(0, 90, 90, true); break;
                case SOUTH: rotation = new Quaternion(0, 270, 90, true); break;
                case WEST: rotation = new Quaternion(0, 180, 90, true); break;
                case EAST: rotation = new Quaternion(0, 0, 90, true); break;
                default: throw new IllegalStateException("Unexpected direction: " + direction);
            }
        } else if (part == ConnectionPart.RIGHT) {
            switch (direction) {
                case DOWN: rotation = new Quaternion(0, 270, 0, true); break;
                case UP: rotation = new Quaternion(180, 270, 0, true); break;
                case NORTH: rotation = new Quaternion(90, 270, 0, true); break;
                case SOUTH: rotation = new Quaternion(0, 90, 270, true); break;
                case WEST: rotation = new Quaternion(0, 0, 270, true); break;
                case EAST: rotation = new Quaternion(0, 180, 270, true); break;
                default: throw new IllegalStateException("Unexpected direction: " + direction);
            }
        } else {
            throw new IllegalStateException("Unexpected direction: " + direction);
        }

        IModelTransform transform = new IModelTransform() {
            public @Override @NotNull TransformationMatrix getRotation() {
                return new TransformationMatrix(null, rotation, null, null);
            }
        };

        List<BakedQuad> quads = new ArrayList<>(6);
        quads.add(bakery.bakeQuad(armFrom, armTo, UVArm, texture, Direction.UP, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(armFrom, armTo, UVArm, texture, Direction.DOWN, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(armFrom, armTo, UVFront, texture, Direction.NORTH, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(armFrom, armTo, UVFront, texture, Direction.EAST, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(armFrom, armTo, UVSide, texture, Direction.SOUTH, transform, null, true, modelLocation));
        quads.add(bakery.bakeQuad(armFrom, armTo, UVSide, texture, Direction.WEST, transform, null, true, modelLocation));

        return quads;
    }

    // ------------------------------------------------------------------------------------------------------------
    // We just return the some basic properties

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }


    @Override
    public boolean isGui3d() {
        return true;
    }


    @Override
    public boolean isSideLit() {
        return true;
    }


    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }


    @Override
    @NotNull
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }


    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }
}
