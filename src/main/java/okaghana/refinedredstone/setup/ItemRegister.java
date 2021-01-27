package okaghana.refinedredstone.setup;

import net.minecraftforge.fml.RegistryObject;
import okaghana.refinedredstone.RefinedRedstone;

import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import okaghana.refinedredstone.item.PowerMeter;
import okaghana.refinedredstone.item.RefinedRedstonePile;

import java.util.function.Supplier;

public class ItemRegister {
     public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, RefinedRedstone.MODID);
     
     public static final RegistryObject<Item> REFINED_REDSTONE_PILE = registerItem("refined_redstone_pile", () -> new RefinedRedstonePile(RefinedRedstonePile.PROPERTIES));
     public static final RegistryObject<Item> POWER_METER = registerItem("power_meter", () -> new PowerMeter(PowerMeter.PROPERTIES));

     private static <T extends Item> RegistryObject<T> registerItem(String name, Supplier<T> item) {
          return REGISTER.register(name, item);
     }
}
