package com.snakejac.aeroblockban;

import com.snakejac.aeroblockban.commands.BlockBan;
import dev.ryanhcode.sable.ActiveSableCompanion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AeroBlockBan.MODID)
public class PlacementEvents {
    @SubscribeEvent
    public static void onBlockPlace(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (new ActiveSableCompanion().getContaining((Level) event.getLevel(), event.getPos()) == null) {
            return;
        }

        BlockState state = event.getPlacedBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (BlockBan.isDenied(id)) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(
                        Component.translatable(state.getBlock().getDescriptionId()).append(Component.literal(" is banned on contraptions"))
                                .withStyle(ChatFormatting.RED)
                );
                player.containerMenu.sendAllDataToRemote();
            }

            event.setCanceled(true);
        }
    }
}
