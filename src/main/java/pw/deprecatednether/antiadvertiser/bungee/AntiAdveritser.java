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

package pw.deprecatednether.antiadvertiser.bungee;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pw.deprecatednether.antiadvertiser.bungee.commands.ReloadCommand;
import pw.deprecatednether.antiadvertiser.bungee.listeners.AdvertiseListener;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class AntiAdveritser extends Plugin {
    private File detections;
    private Configuration tlds;
    public Configuration config;
    private String tldRegex;

    @Override
    public void onEnable() {
        this.getProxy().getPluginManager().registerListener(this, new AdvertiseListener(this));
        this.getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        detections = new File(getDataFolder(), "detections.txt");
        File tldsFile = new File(getDataFolder(), "tlds.yml");
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            if (!tldsFile.exists()) {
                tldsFile.createNewFile();
                InputStream in = getResourceAsStream("tlds.yml");
                OutputStream out = new FileOutputStream(tldsFile);
                ByteStreams.copy(in, out);
            }
            tlds = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "tlds.yml"));
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream in = getResourceAsStream("config.yml");
                OutputStream out = new FileOutputStream(configFile);
                ByteStreams.copy(in, out);
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException ioe) {
            getLogger().severe("An error occurred trying to load the configuration file!");
            ioe.printStackTrace();
        }
        loadTLDs();
    }

    private void loadTLDs() {
        if (tlds.get("last-check") == null || tlds.getLong("last-check") < (System.currentTimeMillis() / 1000 - (7*24*60*60))) {
            if (config.getBoolean("update-tld-list")) {
                getProxy().getScheduler().runAsync(this, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://data.iana.org/TLD/tlds-alpha-by-domain.txt");
                            URLConnection connection = url.openConnection();
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String ln;
                            List<String> list = new ArrayList<String>();
                            while ((ln = in.readLine()) != null) {
                                if (!ln.startsWith("#") && !ln.equals("")) {
                                    list.add(ln);
                                }
                            }
                            in.close();
                            tlds.set("last-check", System.currentTimeMillis() / 1000);
                            tlds.set("tlds", list);
                            ConfigurationProvider.getProvider(YamlConfiguration.class).save(tlds, new File(getDataFolder(), "tlds.yml"));
                            loadTLDs(); // call this again so they're loaded into memory
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return;
            }
        }
        List<String> list = tlds.getStringList("tlds");
        String regex = "";
        for (String tld : list) {
            if (regex.length() != 0) regex = regex + "|";
            regex = regex + tld;
        }
        tldRegex = regex;
    }

    public Configuration getConfig() {
        return config;
    }

    public File getDetectionsFile() {
        return detections;
    }

    public String getTldRegex() {
        return tldRegex;
    }

    public void sendDebug(String message) {
        if (config.getBoolean("debug")) {
            getLogger().info("[AntiAdvertiser Debug] " + message);
        }
    }

    /**
     * Colours the string and turns variables into what they represent.
     * @param string The string to prepare.
     * @param player The instance of ProxiedPlayer whose name is represented by {player}
     * @param message The message, represented by {message}, sent by the player
     * @return The prepared string
     */
    public static String prepareString(String string, ProxiedPlayer player, String message) {
        return ChatColor.translateAlternateColorCodes('&', string).replace("{player}", player.getName()).replace("{display}", player.getDisplayName()).replace("{message}", message);
    }
}
