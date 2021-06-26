package okaghana.refinedredstone.misc;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import okaghana.refinedredstone.RefinedRedstone;
import okaghana.refinedredstone.setup.EntityRegister;
import org.jetbrains.annotations.NotNull;

public class FloatingTextEntity extends Entity {

    public <T extends FloatingTextEntity> FloatingTextEntity(EntityType<T> entityType, World world) {
        super(entityType, world);
    }

    public FloatingTextEntity(World world, Vector3d pos) {
        super(EntityRegister.FLOATING_TEXT, world);
        setPosition(pos.x, pos.y, pos.z);
        RefinedRedstone.chat(String.format("Spawned FloatingText at %s", pos));
    }

    // --------------------------
    //     Entity Properties
    // --------------------------

    // The floating text should: be unattackable, uncollidable, unpushable, unridable,
    //   not activate pressure plates and have no hitbox!

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected boolean canBeRidden(@NotNull Entity entityIn) {
        return false;
    }

    @Override
    public boolean canBeRiddenInWater() {
        return false;
    }

    @Override
    public boolean canCollide(@NotNull Entity entity) {
        return false;
    }

    @Override
    protected boolean canFitPassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    @NotNull
    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(Vector3d.ZERO, Vector3d.ZERO);
    }

    // ---------------------------
    //           Text
    // ---------------------------

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    @NotNull
    public ITextComponent getCustomName() {
        return new StringTextComponent("Test123");
    }

    // ------------------------
    //      Some NBT shit
    // ------------------------

    @Override
    protected void registerData() { }

    @Override
    protected void readAdditional(CompoundNBT compound) { }

    @Override
    protected void writeAdditional(CompoundNBT compound) { }

    @Override
    @NotNull
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this);
    }
}
