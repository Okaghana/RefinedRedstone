package okaghana.refinedredstone.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import okaghana.refinedredstone.RefinedRedstone;

import javax.annotation.Nonnull;

public class PowerMeter extends Item {

    public static final Item.Properties PROPERTIES = new Item.Properties().group(RefinedRedstone.MOD_ITEM_GROUP).maxStackSize(1).rarity(Rarity.EPIC);

    public PowerMeter(Properties properties) {
        super(properties);
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();

        // Find the Block looking at
        RayTraceResult lookingAt = Minecraft.getInstance().objectMouseOver;
        if (lookingAt != null && player != null && lookingAt.getType() == RayTraceResult.Type.BLOCK) {
            Vector3d direction = player.getLookVec();
            BlockPos  pos = new BlockPos(lookingAt.getHitVec());
            BlockState state = world.getBlockState(pos);

            String message;
            if (state.getBlock() instanceof RedstoneWireBlock) {
                int intensity = state.get(RedstoneWireBlock.POWER);
                RedstoneSide north = state.get(RedstoneWireBlock.NORTH);
                RedstoneSide east = state.get(RedstoneWireBlock.EAST);
                RedstoneSide south = state.get(RedstoneWireBlock.SOUTH);
                RedstoneSide west = state.get(RedstoneWireBlock.WEST);

                String connections = (north.getString().equals("side") ? "North " : "") + (east.getString().equals("side") ? "East " : "")
                        + (south.getString().equals("side") ? "South " : "") + (west.getString().equals("side") ? "West " : "");

                String ups = (north.getString().equals("up") ? "North " : "") + (east.getString().equals("up") ? "East " : "")
                        + (south.getString().equals("up") ? "South " : "") + (west.getString().equals("up") ? "West " : "");

                message = String.format("Intensity: %d   Connections: %s   Up: %s", intensity, connections, ups);
            } else {
                int intensity = world.getRedstonePower(pos, Direction.getFacingFromVector(direction.getX(), direction.getY(), direction.getZ()));
                message = String.format("Intensity: %d", intensity);
            }

            player.sendMessage(new StringTextComponent(message), player.getUniqueID());
        }

        return ActionResultType.SUCCESS;
    }
}
