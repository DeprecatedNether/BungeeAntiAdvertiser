/*
 * AntiAdvertiser
 * Copyright (C) 2014 DeprecatedNether
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package pw.deprecatednether.antiadvertiser.bungee.listeners;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pw.deprecatednether.antiadvertiser.bungee.AntiAdveritser;
import pw.deprecatednether.antiadvertiser.bungee.api.PlayerAdvertiseEvent;
import pw.deprecatednether.antiadvertiser.bungee.util.AdvertiserMethods;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdvertiseListener implements Listener {
    private AntiAdveritser main;
    private AdvertiserMethods methods;

    public AdvertiseListener(AntiAdveritser main) {
        this.main = main;
        this.methods = new AdvertiserMethods(main);
    }

    @EventHandler
    public void chat(ChatEvent e) {
        if (!main.getConfig().getBoolean("monitor.chat")) return;
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        if (!methods.safeChat(player, e.getMessage())) {
            PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(player, e.getMessage());
            main.getProxy().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void advertise(PlayerAdvertiseEvent e) {
        if (e.isCancelled()) return;
        main.getLogger().info(e.getPlayer().getName() + " tried advertising: " + e.getMessage());
        try {
            FileWriter writer = new FileWriter(main.getDetectionsFile(), true);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            writer.write("[" + format.format(new Date()) + "] " + e.getPlayer().getName() + " (" + e.getPlayer().getUniqueId() + "): " + e.getMessage() + "\n");
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
