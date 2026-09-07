package org.stellium.ignoring.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.stellium.ignoring.config.IgnoringConfig;

import java.util.concurrent.CompletableFuture;

public class IgnoringCommands {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommands.literal("!ignoring:togglerender")
            .executes(IgnoringCommands::toggleRender));

        dispatcher.register(ClientCommands.literal("!ignoring:togglechat")
            .executes(IgnoringCommands::toggleChat));

        dispatcher.register(ClientCommands.literal("!ignoring:toggletablist")
            .executes(IgnoringCommands::toggleTablist));

        dispatcher.register(ClientCommands.literal("!ignoring:togglenameplates")
            .executes(IgnoringCommands::toggleNameplates));

        dispatcher.register(ClientCommands.literal("!ignoring:toggleparticles")
            .executes(IgnoringCommands::toggleParticles));

        dispatcher.register(ClientCommands.literal("!ignoring:toggleinteraction")
            .executes(IgnoringCommands::toggleInteraction));

        dispatcher.register(ClientCommands.literal("!ignoring:addignore")
            .then(ClientCommands.argument("player", StringArgumentType.string())
                .suggests(IgnoringCommands::suggestOnlinePlayers)
                .executes(IgnoringCommands::addIgnore)));

        dispatcher.register(ClientCommands.literal("!ignoring:removeignore")
            .then(ClientCommands.argument("player", StringArgumentType.string())
                .suggests(IgnoringCommands::suggestIgnoredPlayers)
                .executes(IgnoringCommands::removeIgnore)));

        dispatcher.register(ClientCommands.literal("!ignoring:listignore")
            .executes(IgnoringCommands::listIgnore));

        dispatcher.register(ClientCommands.literal("!ignoring:transparency")
            .then(ClientCommands.argument("value", IntegerArgumentType.integer(0, 255))
                .executes(IgnoringCommands::setTransparency)));

        dispatcher.register(ClientCommands.literal("!ignoring:reload")
            .executes(IgnoringCommands::reload));

        dispatcher.register(ClientCommands.literal("!ignoring:version")
            .executes(IgnoringCommands::version));

        dispatcher.register(ClientCommands.literal("!ignoring:help")
            .executes(IgnoringCommands::help));
    }

    private static int toggleRender(CommandContext<FabricClientCommandSource> context) {
        IgnoringConfig config = IgnoringConfig.get();
        config.ignoreRender = !config.ignoreRender;
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.toggle.ignoreRender")
            .append(Component.literal(": "))
            .append(Component.translatable(config.ignoreRender ? "text.ignoring.status.enabled" : "text.ignoring.status.disabled")
                .withStyle(config.ignoreRender ? ChatFormatting.GREEN : ChatFormatting.RED)));

        return 1;
    }

    private static int toggleChat(CommandContext<FabricClientCommandSource> context) {
        IgnoringConfig config = IgnoringConfig.get();
        config.ignoreChat = !config.ignoreChat;
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.toggle.ignoreChat")
            .append(Component.literal(": "))
            .append(Component.translatable(config.ignoreChat ? "text.ignoring.status.enabled" : "text.ignoring.status.disabled")
                .withStyle(config.ignoreChat ? ChatFormatting.GREEN : ChatFormatting.RED)));

        return 1;
    }

    private static int toggleTablist(CommandContext<FabricClientCommandSource> context) {
        IgnoringConfig config = IgnoringConfig.get();
        config.ignoreTablist = !config.ignoreTablist;
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.toggle.ignoreTablist")
            .append(Component.literal(": "))
            .append(Component.translatable(config.ignoreTablist ? "text.ignoring.status.enabled" : "text.ignoring.status.disabled")
                .withStyle(config.ignoreTablist ? ChatFormatting.GREEN : ChatFormatting.RED)));

        return 1;
    }

    private static int toggleNameplates(CommandContext<FabricClientCommandSource> context) {
        IgnoringConfig config = IgnoringConfig.get();
        config.ignoreNameplates = !config.ignoreNameplates;
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.toggle.ignoreNameplates")
            .append(Component.literal(": "))
            .append(Component.translatable(config.ignoreNameplates ? "text.ignoring.status.enabled" : "text.ignoring.status.disabled")
                .withStyle(config.ignoreNameplates ? ChatFormatting.GREEN : ChatFormatting.RED)));

        return 1;
    }

    private static int toggleParticles(CommandContext<FabricClientCommandSource> context) {
        IgnoringConfig config = IgnoringConfig.get();
        config.ignoreParticles = !config.ignoreParticles;
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.toggle.ignoreParticles")
            .append(Component.literal(": "))
            .append(Component.translatable(config.ignoreParticles ? "text.ignoring.status.enabled" : "text.ignoring.status.disabled")
                .withStyle(config.ignoreParticles ? ChatFormatting.GREEN : ChatFormatting.RED)));

        return 1;
    }

    private static int toggleInteraction(CommandContext<FabricClientCommandSource> context) {
        IgnoringConfig config = IgnoringConfig.get();
        config.interactionThroughIgnoredPlayer = !config.interactionThroughIgnoredPlayer;
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.toggle.interactionThroughIgnoredPlayer")
            .append(Component.literal(": "))
            .append(Component.translatable(config.interactionThroughIgnoredPlayer ? "text.ignoring.status.enabled" : "text.ignoring.status.disabled")
                .withStyle(config.interactionThroughIgnoredPlayer ? ChatFormatting.GREEN : ChatFormatting.RED)));

        return 1;
    }

    /** Everyone currently online who is not on the list yet. */
    private static CompletableFuture<Suggestions> suggestOnlinePlayers(
        CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {

        ClientPacketListener connection = context.getSource().getClient().getConnection();
        if (connection == null) {
            return Suggestions.empty();
        }

        IgnoringConfig config = IgnoringConfig.get();

        return SharedSuggestionProvider.suggest(
            connection.getOnlinePlayers().stream()
                .map(entry -> entry.getProfile().name())
                .filter(name -> name != null && !name.isBlank() && !config.ignoredPlayerList.contains(name)),
            builder);
    }

    /** Everyone already on the list, so removing one does not need typing either. */
    private static CompletableFuture<Suggestions> suggestIgnoredPlayers(
        CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {

        return SharedSuggestionProvider.suggest(
            IgnoringConfig.get().ignoredPlayerList.stream()
                .filter(name -> name != null && !name.isBlank()),
            builder);
    }

    private static int addIgnore(CommandContext<FabricClientCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        IgnoringConfig config = IgnoringConfig.get();

        if (config.ignoredPlayerList.contains(playerName)) {
            context.getSource().sendError(Component.translatable("text.ignoring.command.addignore.duplicate", playerName));
            return 0;
        }

        config.ignoredPlayerList.remove("Insert name");
        config.ignoredPlayerList.add(playerName);
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.command.addignore.success", playerName)
            .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int removeIgnore(CommandContext<FabricClientCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        IgnoringConfig config = IgnoringConfig.get();

        if (!config.ignoredPlayerList.contains(playerName)) {
            context.getSource().sendError(Component.translatable("text.ignoring.command.removeignore.notfound", playerName));
            return 0;
        }

        config.ignoredPlayerList.remove(playerName);
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.command.removeignore.success", playerName)
            .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int listIgnore(CommandContext<FabricClientCommandSource> context) {
        IgnoringConfig config = IgnoringConfig.get();

        if (config.ignoredPlayerList.isEmpty() ||
            (config.ignoredPlayerList.size() == 1 && config.ignoredPlayerList.contains("Insert name"))) {
            context.getSource().sendFeedback(Component.translatable("text.ignoring.command.listignore.empty")
                .withStyle(ChatFormatting.YELLOW));
            return 1;
        }

        context.getSource().sendFeedback(Component.translatable("text.ignoring.command.listignore.header")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (String player : config.ignoredPlayerList) {
            if (!player.equals("Insert name")) {
                context.getSource().sendFeedback(Component.literal("  - ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(player).withStyle(ChatFormatting.WHITE)));
            }
        }

        return 1;
    }

    private static int setTransparency(CommandContext<FabricClientCommandSource> context) {
        int value = IntegerArgumentType.getInteger(context, "value");
        IgnoringConfig config = IgnoringConfig.get();
        config.transparency = value;
        saveConfig();

        context.getSource().sendFeedback(Component.translatable("text.ignoring.command.transparency.success", value)
            .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int reload(CommandContext<FabricClientCommandSource> context) {
        try {
            AutoConfig.getConfigHolder(IgnoringConfig.class).load();
            context.getSource().sendFeedback(Component.translatable("text.ignoring.command.reload.success")
                .withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Component.translatable("text.ignoring.command.reload.failed", e.getMessage()));
            return 0;
        }
    }

    private static int version(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.translatable(
                "text.ignoring.command.version.success",
                modVersion("ignoring"),
                modVersion("minecraft"))
            .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static String modVersion(String modId) {
        return FabricLoader.getInstance()
            .getModContainer(modId)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }

    private static int help(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.translatable("text.ignoring.command.help.header")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        context.getSource().sendFeedback(Component.literal("!ignoring:togglerender")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.togglerender").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:togglechat")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.togglechat").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:toggletablist")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.toggletablist").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:togglenameplates")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.togglenameplates").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:toggleparticles")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.toggleparticles").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:toggleinteraction")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.toggleinteraction").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:addignore <player>")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.addignore").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:removeignore <player>")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.removeignore").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:listignore")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.listignore").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:transparency <0-255>")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.transparency").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:reload")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.reload").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:version")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.version").withStyle(ChatFormatting.GRAY)));

        context.getSource().sendFeedback(Component.literal("!ignoring:help")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" - "))
            .append(Component.translatable("text.ignoring.command.help.help").withStyle(ChatFormatting.GRAY)));

        return 1;
    }

    private static void saveConfig() {
        AutoConfig.getConfigHolder(IgnoringConfig.class).save();
    }
}
