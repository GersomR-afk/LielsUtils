package com.lielamar.lielsutils.update;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private static final String UPDATE_MSG = ChatColor.translateAlternateColorCodes('&',
            "&b&l&s &rhas an update available on &eSpigot&r!"
                + "\n&rYour version, &ev%s, &ris &e%s &rversions behind!");
    private static final String SPIGOT_API = "https://api.spigotmc.org/legacy/update.php?resource=";

    private final JavaPlugin plugin;
    private final int resourceId;
    private final String currentVersion;
    private String spigotVersion;

    private int versionsBehind;

    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(SPIGOT_API + resourceId).openConnection();
                connection.setRequestMethod("GET");
                spigotVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if(currentVersion.equals(spigotVersion)) return;

            versionsBehind = 0;
            versionsBehind += Integer.parseInt(spigotVersion.split("\\.")[1])-Integer.parseInt(currentVersion.split("\\.")[1]);
            if(versionsBehind == 0)
                versionsBehind += Integer.parseInt(spigotVersion.split("\\.")[2])-Integer.parseInt(currentVersion.split("\\.")[2]);

            sendUpdateMessage(Bukkit.getServer().getConsoleSender());

            for(Player pl : Bukkit.getOnlinePlayers())
                sendUpdateMessage(pl);

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler (priority = EventPriority.MONITOR)
                public void onPlayerJoin(PlayerJoinEvent event) {
                    sendUpdateMessage(event.getPlayer());
                }
            }, plugin);
        });
    }

    public void sendUpdateMessage(CommandSender cs) {
        if(cs.isOp())
            cs.sendMessage(String.format(UPDATE_MSG, plugin.getName(), currentVersion, versionsBehind));
    }
}