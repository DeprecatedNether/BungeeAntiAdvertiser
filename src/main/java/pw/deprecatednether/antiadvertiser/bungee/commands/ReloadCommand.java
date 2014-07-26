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

package pw.deprecatednether.antiadvertiser.bungee.commands;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pw.deprecatednether.antiadvertiser.bungee.AntiAdveritser;

import java.io.*;

public class ReloadCommand extends Command {
    private AntiAdveritser main;

    public ReloadCommand(AntiAdveritser main) {
        super("aareload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("antiadvertiser.reload")) {
            TextComponent message = new TextComponent("You do not have access to this command.");
            message.setColor(ChatColor.RED);
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("You need the permission node antiadvertiser.reload").color(ChatColor.GREEN).create()));
            sender.sendMessage(message);
            return;
        }
        try {
            File configFile = new File(main.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream in = main.getResourceAsStream("config.yml");
                OutputStream out = new FileOutputStream(configFile);
                ByteStreams.copy(in, out);
            }
            main.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(main.getDataFolder(), "config.yml"));
            sender.sendMessage(new ComponentBuilder("AntiAdvertiser configuration on the BungeeCord server has been reloaded successfully.").color(ChatColor.DARK_GREEN).create());
            sender.sendMessage(new ComponentBuilder("If you're trying to reload the AntiAdvertiser configuration for the Bukkit server, try '/antiadvertiser:aareload'.").color(ChatColor.GREEN).create());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
