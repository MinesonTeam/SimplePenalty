package kz.hxncus.mc.simplepenalty.listener;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private static SimplePenalty plugin;

    public PlayerListener(SimplePenalty plugin) {
        PlayerListener.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getCacheManager().unloadPlayerFromDatabase(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getCacheManager().loadPlayerIntoDatabase(event.getPlayer().getUniqueId());
    }
}
