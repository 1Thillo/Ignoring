package org.stellium.ignoring.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.stellium.ignoring.config.IgnoringConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Turns an ignored player down to silence in Simple Voice Chat, and back up when they
 * leave the ignore list again.
 *
 * Only players this mod muted itself are ever turned back up, so a volume somebody set
 * by hand in the voice chat screen is left alone. Turning them back up restores full
 * volume rather than whatever they were on before being ignored.
 */
public final class VoiceChatCompat {

    private static final String VOICECHAT_MOD_ID = "voicechat";
    private static final double MUTED = 0.0D;
    private static final double FULL = 1.0D;

    private static final Set<UUID> mutedByUs = new HashSet<>();
    private static Boolean installed;

    private VoiceChatCompat() {
    }

    public static boolean isInstalled() {
        if (installed == null) {
            installed = FabricLoader.getInstance().isModLoaded(VOICECHAT_MOD_ID);
        }

        return installed;
    }

    public static void tick(Minecraft client) {
        if (!isInstalled()) {
            return;
        }

        if (client.level == null) {
            unmuteAll();
            return;
        }

        IgnoringConfig config;
        try {
            config = IgnoringConfig.get();
        } catch (Exception notReadyYet) {
            // The config registers itself lazily, and this runs on every tick from
            // the moment a level exists, which can be earlier than that.
            return;
        }

        if (!config.muteVoiceChat) {
            unmuteAll();
            return;
        }

        for (AbstractClientPlayer player : client.level.players()) {
            UUID uuid = player.getUUID();

            if (config.shouldIgnorePlayer(player)) {
                if (mutedByUs.add(uuid)) {
                    VoiceChatBridge.setVolume(uuid, MUTED);
                }
            } else if (mutedByUs.remove(uuid)) {
                VoiceChatBridge.setVolume(uuid, FULL);
            }
        }
    }

    /** Leaving someone silenced after the option is off would be a trap. */
    public static void unmuteAll() {
        if (!isInstalled() || mutedByUs.isEmpty()) {
            return;
        }

        for (UUID uuid : mutedByUs) {
            VoiceChatBridge.setVolume(uuid, FULL);
        }

        mutedByUs.clear();
    }
}
