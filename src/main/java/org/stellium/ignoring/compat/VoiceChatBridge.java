package org.stellium.ignoring.compat;

import de.maxhenkel.voicechat.VoicechatClient;

import java.util.UUID;

/**
 * Everything that touches Simple Voice Chat lives here, so the class is only ever
 * loaded once {@link VoiceChatCompat} has established that the mod is installed.
 *
 * Simple Voice Chat's public API can only read volumes ({@code VolumeConfigAccessor}
 * has no setter), so this goes through the client's volume config directly.
 */
final class VoiceChatBridge {

    private VoiceChatBridge() {
    }

    static void setVolume(UUID player, double volume) {
        if (VoicechatClient.PLAYER_VOLUME_CONFIG == null) {
            return;
        }

        VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(player, volume);
        VoicechatClient.PLAYER_VOLUME_CONFIG.save();
    }
}
