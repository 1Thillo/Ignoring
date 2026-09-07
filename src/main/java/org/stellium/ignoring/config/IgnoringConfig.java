package org.stellium.ignoring.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

@Config(name = "ignoring")
public class IgnoringConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.TransitiveObject
    public boolean ignoreChat = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.TransitiveObject
    public boolean ignoreRender = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.TransitiveObject
    public boolean ignoreNameplates = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.TransitiveObject
    public boolean ignoreParticles = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.TransitiveObject
    public boolean ignoreTablist = false;

    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.TransitiveObject
    public boolean interactionThroughIgnoredPlayer = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.TransitiveObject
    public boolean ignoreEveryone = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.TransitiveObject
    public boolean ignoreSpecialCharacter = false;


    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 255)
    public int transparency = 255;

    @ConfigEntry.Gui.Tooltip
    public List<String> ignoredPlayerList = new ArrayList<>();



    public IgnoringConfig() {
        if (ignoredPlayerList.isEmpty()) {
            ignoredPlayerList.add("Insert name");
        }
    }

    static void validate(IgnoringConfig config) {
        if (config.ignoredPlayerList != null) {
            config.ignoredPlayerList.removeIf(name -> name == null || name.isBlank());
        }
        if (config.transparency < 0) config.transparency = 0;
        if (config.transparency > 255) config.transparency = 255;
    }

    public static void init() {
        AutoConfig.register(IgnoringConfig.class, GsonConfigSerializer::new);
    }

    public boolean shouldIgnorePlayer(Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        if (ignoreEveryone) {
            return !isLocalPlayer(player);
        }

        return isListedName(player.getScoreboardName())
            || isListedName(player.getName().getString())
            || isListedName(player.getGameProfile().name());
    }

    /**
     * Some servers draw their own name plates as separate entities above a player
     * rather than using the vanilla name tag, which is why those survive
     * {@link #shouldIgnorePlayer}: they are not players. This matches such an
     * entity by the name it displays. Only the explicit ignore list is consulted;
     * ignoreEveryone would take every hologram and named armour stand on the
     * server with it.
     */
    public boolean shouldIgnoreNameplate(Entity entity) {
        if (!ignoreNameplates || entity instanceof Player) {
            return false;
        }

        String text = nameplateText(entity);
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String ignored : ignoredPlayerList) {
            if (ignored != null && !ignored.isBlank() && text.contains(ignored)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Particle plugins spawn their effects server side at the player's feet. They
     * arrive as their own packets and are not part of the player's entity
     * rendering, so nothing in the render path can reach them. Matching them by
     * how close they land to an ignored player is the only handle available.
     */
    public boolean shouldIgnoreParticleAt(double x, double y, double z) {
        if (!ignoreParticles) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null) {
            return false;
        }

        // Kept as a local: AutoConfig walks every field of this class when it
        // saves and tries to write each one back, which blows up on a static
        // final and takes the whole save with it.
        final double radius = 3.0D;

        for (Player player : client.level.players()) {
            if (shouldIgnorePlayer(player) && player.distanceToSqr(x, y, z) <= radius * radius) {
                return true;
            }
        }

        return false;
    }

    private static String nameplateText(Entity entity) {
        if (entity instanceof Display.TextDisplay display) {
            Display.TextDisplay.TextRenderState state = display.textRenderState();
            return state == null || state.text() == null ? null : state.text().getString();
        }

        Component customName = entity.getCustomName();
        return customName == null ? null : customName.getString();
    }

    public boolean isPlayerIgnored(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return false;
        }

        if (ignoreEveryone) {
            return !isLocalPlayerName(playerName);
        }

        return ignoredPlayerList.contains(playerName);
    }

    private boolean isListedName(String value) {
        return value != null && ignoredPlayerList.contains(value);
    }

    private boolean isLocalPlayer(Player player) {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.player != null && client.player.getUUID().equals(player.getUUID());
    }

    private boolean isLocalPlayerName(String playerName) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return false;
        }

        if (playerName.equals(client.player.getScoreboardName())) {
            return true;
        }

        if (playerName.equals(client.player.getGameProfile().name())) {
            return true;
        }

        return playerName.equals(client.player.getName().getString());
    }

    public static IgnoringConfig get() {
        IgnoringConfig config = AutoConfig.getConfigHolder(IgnoringConfig.class).getConfig();
        validate(config);
        return config;
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        validate(this);
    }
}
