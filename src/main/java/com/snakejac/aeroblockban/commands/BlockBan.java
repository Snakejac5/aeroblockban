package com.snakejac.aeroblockban.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.snakejac.aeroblockban.AeroBlockBan;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = AeroBlockBan.MODID)
public class BlockBan {

    private static final Set<ResourceLocation> DENIED = new HashSet<>();

    private static final Path SAVE_FILE = FMLPaths.CONFIGDIR.get()
            .resolve("aeroblockban_blocks.txt");


    private static void save() {
        try {
            Files.write(
                    SAVE_FILE,
                    DENIED.stream()
                            .map(ResourceLocation::toString)
                            .toList()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void load() {
        if (!Files.exists(SAVE_FILE)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(SAVE_FILE)) {
                DENIED.add(ResourceLocation.parse(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDenied(ResourceLocation id) {
        return DENIED.contains(id);
    }

    public static void add(ResourceLocation id) {
        DENIED.add(id);
        save();
    }

    public static void remove(ResourceLocation id) {
        DENIED.remove(id);
        save();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher(), event);
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, RegisterCommandsEvent event) {
        load();
        dispatcher.register(
            Commands.literal("aeroblockban").then(
                Commands.literal("add").requires(source -> source.hasPermission(2)).then(
                        Commands.argument(
                                "block or tag",
                                ResourceOrTagArgument.resourceOrTag(
                                        event.getBuildContext(),
                                        Registries.BLOCK
                                )
                        ).executes(context -> {
                        ResourceOrTagArgument.Result<Block> result =
                                ResourceOrTagArgument.getResourceOrTag(
                                        context,
                                        "block or tag",
                                        Registries.BLOCK
                                );

                        Registry<Block> registry = context.getSource()
                                .getServer()
                                .registryAccess()
                                .registryOrThrow(Registries.BLOCK);

                        result.unwrap().ifLeft(resource -> {
                            assert resource.getKey() != null;
                            ResourceLocation id = resource.getKey().location();

                            add(id);

                            context.getSource().sendSuccess(
                                    () -> Component.literal("Added ").append(Component.translatable(BuiltInRegistries.BLOCK.get(id).getDescriptionId())),
                                    false
                            );
                        });

                        result.unwrap().ifRight(tag -> {
                            for (Holder<Block> holder : registry.getTagOrEmpty(tag.key())) {
                                ResourceLocation id = registry.getKey(holder.value());

                                add(id);

                                context.getSource().sendSuccess(
                                        () -> Component.literal("Added ").append(Component.translatable(BuiltInRegistries.BLOCK.get(id).getDescriptionId())),
                                        false
                                );
                            }

                            save();
                        });

                        return 1;
                    })
                )
            ).then(
                Commands.literal("remove").requires(source -> source.hasPermission(2)).then(
                        Commands.argument(
                                "block or tag",
                                ResourceOrTagArgument.resourceOrTag(
                                        event.getBuildContext(),
                                        Registries.BLOCK
                                )
                        ).executes(context -> {
                            ResourceOrTagArgument.Result<Block> result =
                                    ResourceOrTagArgument.getResourceOrTag(
                                            context,
                                            "block or tag",
                                            Registries.BLOCK
                                    );

                            Registry<Block> registry = context.getSource()
                                    .getServer()
                                    .registryAccess()
                                    .registryOrThrow(Registries.BLOCK);

                            result.unwrap().ifLeft(resource -> {
                                assert resource.getKey() != null;
                                ResourceLocation id = resource.getKey().location();

                                remove(id);

                                context.getSource().sendSuccess(
                                        () -> Component.literal("Removed ").append(Component.translatable(BuiltInRegistries.BLOCK.get(id).getDescriptionId())),
                                        false
                                );
                            });

                            result.unwrap().ifRight(tag -> {
                                for (Holder<Block> holder : registry.getTagOrEmpty(tag.key())) {
                                    ResourceLocation id = registry.getKey(holder.value());

                                    remove(id);

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Removed ").append(Component.translatable(BuiltInRegistries.BLOCK.get(id).getDescriptionId())),
                                            false
                                    );
                                }

                                save();
                            });

                            return 1;
                        }
                    ).suggests((context, builder) -> {
                        for (ResourceLocation id : DENIED) {
                            builder.suggest(id.toString());
                        }

                        return builder.buildFuture();
                    })
                )
            ).then(
                Commands.literal("list").executes(
                    context -> {
                        for (ResourceLocation id : DENIED) {
                            Block block = BuiltInRegistries.BLOCK.get(id);

                            context.getSource().sendSuccess(
                                    () -> Component.translatable(block.getDescriptionId()),
                                    false
                            );
                        }
                        return 1;
                    }
                )
            )
        );
    }
}
