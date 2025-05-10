package com.artillexstudios.axcoins.listener;

import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {


    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        User user = AxCoinsAPI.instance().getUserIfLoadedImmediately(event.getPlayer());

    }
}
