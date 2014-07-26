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
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pw.deprecatednether.antiadvertiser.bungee.commands.ReloadCommand;
import pw.deprecatednether.antiadvertiser.bungee.listeners.AdvertiseListener;

import java.io.*;

public class AntiAdveritser extends Plugin {
    private File detections;
    private Configuration tlds;

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
        } catch (IOException ioe) {
            getLogger().severe("An error occurred trying to load the TLDs, AntiAdvertiser will not function!");
            ioe.printStackTrace();
        }
    }
}
