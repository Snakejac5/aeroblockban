package com.snakejac.aeroblockban.mixin;

import com.snakejac.aeroblockban.commands.BlockBan;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public class SubLevelAssemblyHelperMixin {

    @Inject(
            method = "assembleBlocks",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void preventTaggedBlocksAssembly(
            final ServerLevel level, final BlockPos anchor, final Iterable<BlockPos> blocks, final BoundingBox3ic bounds, CallbackInfoReturnable<ServerSubLevel> cir
    ) {
        if (level.isClientSide) {
            return;
        }

        for (BlockPos pos : blocks) {
            BlockState state = level.getBlockState(pos);
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

            double radius = 10.0;

            // Replace with your tag
            if (BlockBan.isDenied(id)) {
                for (ServerPlayer player : level.players()) {
                    if (player.blockPosition().distSqr(anchor) <= radius * radius) {
                        player.displayClientMessage(
                                Component.literal("Cannot assemble: ").append(Component.translatable(state.getBlock().getDescriptionId())).append(Component.literal(" is banned on contraptions"))
                                        .withStyle(ChatFormatting.RED),
                                false
                        );
                    }
                }
                // cancel assembly
                cir.setReturnValue(null);
                return;
            }
        }
    }
}