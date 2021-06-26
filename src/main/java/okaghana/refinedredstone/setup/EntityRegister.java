package okaghana.refinedredstone.setup;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import okaghana.refinedredstone.misc.FloatingTextEntity;

public class EntityRegister {
    public static EntityType<FloatingTextEntity> FLOATING_TEXT;

    @SubscribeEvent
    public static void onEntityTypeRegistration(RegistryEvent.Register<EntityType<?>> entityTypeRegisterEvent) {
        FLOATING_TEXT = EntityType.Builder.<FloatingTextEntity>create(FloatingTextEntity::new, EntityClassification.MISC).build("floating_text");
        FLOATING_TEXT.setRegistryName("floating_text");
        entityTypeRegisterEvent.getRegistry().register(FLOATING_TEXT);
    }
}
