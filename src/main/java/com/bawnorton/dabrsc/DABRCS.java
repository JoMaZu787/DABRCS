package com.bawnorton.dabrsc;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;

public final class DABRCS extends JavaPlugin implements Listener {
    private static final String HANDSHAKE_CHANNEL = "do_a_barrel_roll:handshake";

    @Override
    public void onEnable() {
        getLogger().info("Loaded DABRCS");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, HANDSHAKE_CHANNEL);
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloaded DABRCS");
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getServer().getScheduler().runTaskLater(this, () -> sendConfig(event.getPlayer()), 20L);

    }

    private void writeVarInt(ByteArrayDataOutput out, int value) {
        while ((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        out.writeByte(value);
    }

    private void sendConfig(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        String config = String.format("{\"allowThrusting\": %s, \"forceEnabled\": %s}", player.hasPermission("dabr.thrust"), player.hasPermission("dabr.force"));

        out.writeInt(3);
        writeVarInt(out, config.length());
        out.write(config.getBytes(StandardCharsets.UTF_8));
        out.writeBoolean(true);

        DABRCS plugin = JavaPlugin.getPlugin(DABRCS.class);
        player.sendPluginMessage(plugin, HANDSHAKE_CHANNEL, out.toByteArray());
    }
}
