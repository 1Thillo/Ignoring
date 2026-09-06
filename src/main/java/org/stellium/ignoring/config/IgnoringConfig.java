package org.stellium.ignoring.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.Minecraft;
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
