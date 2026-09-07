


package org.stellium.ignoring;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stellium.ignoring.command.IgnoringCommands;
import org.stellium.ignoring.config.IgnoringConfig;


public class Ignoring implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Ignoring");

    @Override
    public void onInitialize() {
        LOGGER.info("Initialized");
        ClientCommandRegistrationCallback.EVENT.register(IgnoringCommands::register);
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("ignoring", "category"));

        KeyMapping openConfigKeybind = new KeyMapping(
          "text.ignoring.key.openConfig",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_P,
          category
        );

        KeyMapping toggleIgnoreRenderKeybind = new KeyMapping(
          "text.ignoring.key.toggleIgnoreRender",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_SEMICOLON,
          category
        );

        KeyMapping toggleIgnoreChatKeybind = new KeyMapping(
          "text.ignoring.key.toggleIgnoreChat",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_APOSTROPHE,
          category
        );

        KeyMapping toggleIgnoreTablistKeybind = new KeyMapping(
          "text.ignoring.key.toggleIgnoreTablist",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_UNKNOWN,
          category
        );

        KeyMapping toggleInteractionThroughIgnoredPlayerKeybind = new KeyMapping(
          "text.ignoring.key.toggleInteractionThroughIgnoredPlayer",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_UNKNOWN,
          category
        );

        KeyMappingHelper.registerKeyMapping(openConfigKeybind);
        KeyMappingHelper.registerKeyMapping(toggleIgnoreRenderKeybind);
        KeyMappingHelper.registerKeyMapping(toggleIgnoreChatKeybind);
        KeyMappingHelper.registerKeyMapping(toggleIgnoreTablistKeybind);
        KeyMappingHelper.registerKeyMapping(toggleInteractionThroughIgnoredPlayerKeybind);
        ClientTickEvents.END_CLIENT_TICK.register(org.stellium.ignoring.compat.VoiceChatCompat::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKeybind.consumeClick()) {
                client.setScreenAndShow(AutoConfigClient.getConfigScreen(IgnoringConfig.class, client.gui.screen()).get());
            }

            if (toggleIgnoreRenderKeybind.consumeClick()) {
                boolean before = IgnoringConfig.get().ignoreRender;
                IgnoringConfig.get().ignoreRender = !before;
                AutoConfig.getConfigHolder(IgnoringConfig.class).save();

                client.gui.hud.setOverlayMessage(
                    getToggleText("ignoreRender", before),
                    false
                );
            }

            if (toggleIgnoreChatKeybind.consumeClick()) {
                boolean before = IgnoringConfig.get().ignoreChat;
                IgnoringConfig.get().ignoreChat = !before;
                AutoConfig.getConfigHolder(IgnoringConfig.class).save();

                client.gui.hud.setOverlayMessage(
                    getToggleText("ignoreChat", before),
                    false
                );
            }

            if (toggleIgnoreTablistKeybind.consumeClick()) {
                boolean before = IgnoringConfig.get().ignoreTablist;
                IgnoringConfig.get().ignoreTablist = !before;
                AutoConfig.getConfigHolder(IgnoringConfig.class).save();

                client.gui.hud.setOverlayMessage(
                    getToggleText("ignoreTablist", before),
                    false
                );
            }

            if (toggleInteractionThroughIgnoredPlayerKeybind.consumeClick()) {
                boolean before = IgnoringConfig.get().interactionThroughIgnoredPlayer;
                IgnoringConfig.get().interactionThroughIgnoredPlayer = !before;
                AutoConfig.getConfigHolder(IgnoringConfig.class).save();

                client.gui.hud.setOverlayMessage(
                    getToggleText("interactionThroughIgnoredPlayer", before),
                    false
                );
            }
        });

    }

    private Component getToggleText(String optionName, boolean original) {
        Component name = Component.translatable("text.ignoring.toggle." + optionName);
        Component status = Component.translatable(
                original
                    ? "text.ignoring.status.disabled"
                    : "text.ignoring.status.enabled"
            )
            .withStyle(original ? ChatFormatting.RED : ChatFormatting.GREEN);

        return Component.empty()
                   .append(name)
                   .append(Component.literal(": "))
                   .append(status);
    }

}
