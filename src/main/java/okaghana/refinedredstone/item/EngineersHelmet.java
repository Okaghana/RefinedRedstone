package okaghana.refinedredstone.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import okaghana.refinedredstone.RefinedRedstone;
import okaghana.refinedredstone.misc.FloatingTextEntity;
import okaghana.refinedredstone.setup.BlockRegister;
import okaghana.refinedredstone.setup.ConfigHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EngineersHelmet extends ArmorItem {
    public static final IArmorMaterial ARMOR_MATERIAL = ArmorMaterial.LEATHER;
    public static final Properties PROPERTIES = new Item.Properties().maxStackSize(1).group(RefinedRedstone.MOD_ITEM_GROUP);

    private Map<BlockPos, FloatingTextEntity> floatingTexts;

    public EngineersHelmet() {
        this(ARMOR_MATERIAL, PROPERTIES);
    }

    public EngineersHelmet(IArmorMaterial materialIn, Properties builderIn) {
        super(materialIn, EquipmentSlotType.HEAD, builderIn);
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        for (Vector3d position : getRedstoneBlocks(world, player.getPosition())) {
            FloatingTextEntity text = new FloatingTextEntity(world, position);
            world.addEntity(text);
        }
    }

    public List<Vector3d> getRedstoneBlocks(World world, BlockPos center) {
        int radius = ConfigHandler.ENGINEERS_HELMET_RADIUS.get();
        List<Vector3d> blocks = new ArrayList<>();

        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    BlockPos position = new BlockPos(x, y, z);
                    if (world.getBlockState(position).isIn(BlockRegister.REFINED_REDSTONE.get())){
                        blocks.add(new Vector3d(position.getX(), position.getY(), position.getZ()));
                    }
                }
            }
        }

        return blocks;
    }
}
