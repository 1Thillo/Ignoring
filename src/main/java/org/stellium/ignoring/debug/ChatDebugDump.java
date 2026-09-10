package org.stellium.ignoring.debug;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes incoming chat to the log exactly as the server sent it.
 *
 * <p>Chat plugins format messages server side and ship the result as plain system chat,
 * which strips the chat type the client would normally use to tell a public message from
 * a private one. What does survive is the component structure: hover and click events the
 * plugin attached to the sender's name. This dump makes that structure visible so the
 * filter can be built against what a server really sends instead of a guess.
 *
 * <p>Session only. Never written to the config, always off again after a restart.
 */
public final class ChatDebugDump {

    private static final Logger LOGGER = LoggerFactory.getLogger("Ignoring/ChatDebug");
    private static final Gson GSON = new Gson();

    private static boolean enabled = false;

    private ChatDebugDump() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    /**
     * @param source which packet handler this message arrived through, so the log shows
     *               whether the server uses system chat, signed player chat or disguised chat
     */
    public static void dump(String source, Component component) {
        if (!enabled || component == null) {
            return;
        }

        try {
            LOGGER.info("[chat-debug] --- {} ---", source);
            LOGGER.info("[chat-debug] plain   : {}", component.getString());
            describe(component, 0);

            String json = toJson(component);
            if (json != null) {
                LOGGER.info("[chat-debug] json    : {}", json);
            }
        } catch (Exception e) {
            // A diagnostic tool must never be the reason a chat message goes missing.
            LOGGER.warn("[chat-debug] failed to dump a message", e);
        }
    }

    public static void dumpLine(String line) {
        if (enabled) {
            LOGGER.info("[chat-debug] {}", line);
        }
    }

    /** Walks the component tree and reports the parts that could identify a sender. */
    private static void describe(Component node, int depth) {
        String indent = "  ".repeat(depth);
        Style style = node.getStyle();

        StringBuilder line = new StringBuilder();
        line.append(indent).append("part    : ").append(node.getContents());

        ClickEvent click = style.getClickEvent();
        if (click != null) {
            line.append(" | click=").append(describeClick(click));
        }

        HoverEvent hover = style.getHoverEvent();
        if (hover != null) {
            line.append(" | hover=").append(describeHover(hover));
        }

        LOGGER.info("[chat-debug] {}", line);

        for (Component sibling : node.getSiblings()) {
            describe(sibling, depth + 1);
        }
    }

    private static String describeClick(ClickEvent click) {
        return switch (click) {
            case ClickEvent.SuggestCommand suggest -> "suggest_command(" + suggest.command() + ")";
            case ClickEvent.RunCommand run -> "run_command(" + run.command() + ")";
            default -> click.action().getSerializedName();
        };
    }

    private static String describeHover(HoverEvent hover) {
        return switch (hover) {
            case HoverEvent.ShowText text -> "show_text(" + text.value().getString().replace('\n', '¶') + ")";
            case HoverEvent.ShowEntity entity -> "show_entity(uuid=" + entity.entity().uuid
                + ", name=" + entity.entity().name.map(Component::getString).orElse("-") + ")";
            default -> hover.action().getSerializedName();
        };
    }

    /** The complete message, so nothing this dump does not know about gets lost. */
    private static String toJson(Component component) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return null;
        }

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, connection.registryAccess());
        return ComponentSerialization.CODEC.encodeStart(ops, component)
            .result()
            .map(GSON::toJson)
            .orElse(null);
    }
}
