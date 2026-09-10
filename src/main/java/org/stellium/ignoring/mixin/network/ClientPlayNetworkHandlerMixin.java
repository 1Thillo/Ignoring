package org.stellium.ignoring.mixin.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.stellium.ignoring.chat.ChatSender;
import org.stellium.ignoring.chat.ChatSenderResolver;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.debug.ChatDebugDump;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow
    @Mutable
    @Final
    private Set<PlayerInfo> listedPlayers;

    @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
    private void ignoring$hideIgnoredParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        if (IgnoringConfig.get().shouldIgnoreParticleAt(packet.getX(), packet.getY(), packet.getZ())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        IgnoringConfig config = IgnoringConfig.get();

        Component original = packet.content();
        ChatDebugDump.dump("system chat", original);

        String raw = original.getString();

        if (shouldHide(config, original, raw)) {
            ci.cancel();
            return;
        }

        if (!config.ignoreSpecialCharacter) return;

        Component modified = trimTailOne(original);
        if (modified == null) return;

        ci.cancel();
        Minecraft.getInstance().gui.hud.getChat().addServerSystemMessage(modified);
    }

    /**
     * Public chat and whispers are filtered by their own switch, so someone can be silenced in
     * the channel everyone reads while still being able to reach you privately, or the other
     * way round.
     *
     * <p>A message the server made clickable tells us who wrote it, so an ignored player's chat
     * can be dropped without dropping every message that merely mentions their name. Messages
     * carrying no such marker, such as join announcements, count as public and fall back to a
     * plain text match so that they keep disappearing the way they always have.
     */
    private static boolean shouldHide(IgnoringConfig config, Component message, String raw) {
        ChatSender sender = ChatSenderResolver.resolve(message).orElse(null);

        if (sender == null) {
            if (!config.ignoreChat) {
                return false;
            }
            if (config.ignoreEveryone) {
                return true;
            }
            // Without case, because a server may write GFiti where the account is Gfiti.
            String haystack = raw.toLowerCase(Locale.ROOT);
            for (String playerName : config.ignoredPlayerList) {
                if (haystack.contains(playerName.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
            return false;
        }

        boolean filtered = sender.privateMessage() ? config.ignorePrivateMessages : config.ignoreChat;
        if (!filtered) {
            return false;
        }

        return config.ignoreEveryone || isIgnored(config, sender.name());
    }

    private static boolean isIgnored(IgnoringConfig config, String playerName) {
        for (String ignored : config.ignoredPlayerList) {
            if (ignored.equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Signed chat carries the chat type the client can read directly, so dumping it shows
     * whether a server uses this path at all instead of formatting everything itself.
     */
    @Inject(method = "handlePlayerChat", at = @At("HEAD"))
    private void ignoring$dumpPlayerChat(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        if (!ChatDebugDump.isEnabled()) {
            return;
        }

        ChatDebugDump.dumpLine("--- player chat ---");
        ChatDebugDump.dumpLine("sender  : " + packet.sender());
        ChatDebugDump.dumpLine("type    : " + packet.chatType().chatType().unwrapKey()
            .map(key -> key.identifier().toString()).orElse("unbound"));
        ChatDebugDump.dumpLine("signed  : " + packet.body().content());

        Component unsigned = packet.unsignedContent();
        if (unsigned != null) {
            ChatDebugDump.dump("player chat (unsigned content)", unsigned);
        }
    }

    @Inject(method = "handleDisguisedChat", at = @At("HEAD"))
    private void ignoring$dumpDisguisedChat(ClientboundDisguisedChatPacket packet, CallbackInfo ci) {
        if (!ChatDebugDump.isEnabled()) {
            return;
        }

        ChatDebugDump.dumpLine("--- disguised chat ---");
        ChatDebugDump.dumpLine("type    : " + packet.chatType().chatType().unwrapKey()
            .map(key -> key.identifier().toString()).orElse("unbound"));
        ChatDebugDump.dump("disguised chat", packet.message());
    }

    private static Component trimTailOne(Component original) {
        List<Style> styles = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        original.visit((style, string) -> {
            styles.add(style);
            strings.add(string);
            return Optional.empty();
        }, Style.EMPTY);

        int pi = strings.size() - 1;
        int off = (pi >= 0 && strings.get(pi) != null) ? strings.get(pi).length() : 0;
        if (pi < 0) return null;

        int[] pos = prevNonWs(strings, pi, off);
        pi = pos[0];
        off = pos[1];
        if (pi < 0) return null;

        String s = strings.get(pi);
        int cp = s.codePointBefore(off);
        if (!isRemovable(cp)) return null;

        int start = s.offsetByCodePoints(off, -1);
        strings.set(pi, s.substring(0, start) + s.substring(off));

        MutableComponent out = Component.empty();
        for (int k = 0; k < strings.size(); k++) {
            String part = strings.get(k);
            if (part == null || part.isEmpty()) continue;
            out.append(Component.literal(part).setStyle(styles.get(k)));
        }
        return out;
    }

    private static int[] prevNonWs(List<String> strings, int pi, int off) {
        int p = pi;
        int o = off;
        while (p >= 0) {
            String s = strings.get(p);
            if (s == null || o <= 0) {
                p--;
                o = (p >= 0 && strings.get(p) != null) ? strings.get(p).length() : 0;
                continue;
            }
            int cp = s.codePointBefore(o);
            if (!Character.isWhitespace(cp)) return new int[]{p, o};
            o = s.offsetByCodePoints(o, -1);
        }
        return new int[]{-1, 0};
    }

    private static boolean isRemovable(int cp) {
        if (cp >= 0x30 && cp <= 0x39) return false;
        if (cp >= 0x41 && cp <= 0x5A) return false;
        if (cp >= 0x61 && cp <= 0x7A) return false;
        if (cp >= 0xAC00 && cp <= 0xD7A3) return false;

        if (cp == '[' || cp == ']' || cp == '(' || cp == ')' || cp == '{' || cp == '}' || cp == '<' || cp == '>') return false;
        if (cp == '.' || cp == ',' || cp == '!' || cp == '?' || cp == ':' || cp == ';') return false;
        if (cp == '\'' || cp == '"' || cp == '-' || cp == '_' || cp == '~') return false;

        int type = Character.getType(cp);
        return type == Character.OTHER_SYMBOL || type == Character.MATH_SYMBOL || type == Character.MODIFIER_SYMBOL;
    }
}
