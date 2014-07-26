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

package pw.deprecatednether.antiadvertiser.bungee.util;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import pw.deprecatednether.antiadvertiser.bungee.AntiAdvertiser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvertiserMethods {
    private AntiAdvertiser main;

    public AdvertiserMethods(AntiAdvertiser main) {
        this.main = main;
    }

    /**
     * Checks if the message contains an IP address.
     * @param str The message to check
     * @return True if IP found, false if not
     */
    public boolean checkForIp(String str) {
        if (!main.getConfig().getBoolean("checks.ips")) {
            return false;
        }
        String ipPattern = "([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})";
        if (Pattern.compile(ipPattern).matcher(str).find()) {
            main.sendDebug("The received message contained an IP.");
            return true;
        }
        main.sendDebug("The received message did NOT contain an IP.");
        return false;
    }

    /**
     * Checks if the message contains a domain name.
     * @param str The message to check
     * @return True if domain name found, false if not
     */
    public boolean checkForDomain(String str) {
        if (!main.getConfig().getBoolean("checks.domains")) {
            return false;
        }
        String domainPattern = "([a-z-0-9]{1,50})\\.(" + main.getTldRegex() + ")(?![a-z0-9])";
        if (Pattern.compile(domainPattern).matcher(str).find()) {
            main.sendDebug("The received message contained a website. Matched regex '" + domainPattern + "'");
            return true;
        }
        main.sendDebug("The received message did NOT contain a website.");
        return false;
    }

    /**
     * Removes all whitelisted words from the message.
     * For example, "bukkit.org" is whitelisted and str is "go to dev.bukkit.org". This will return "go to dev." which won't be detected as advertising.
     * If str was "go to mc.someminecraftserver.com.bukkit.org", it would return "go to mc.someminecraftserver.com." which would be detected as advertising.
     * This way, we prevent whitelisted domains from being picked up as an ad but not let advertisers bypass the checks with this.
     * @param str The message to check
     * @return The stripped message to run through all other checks.
     */
    public String checkForWhitelist(String str) {
        String finish = str;
        for (String whitelist : main.getConfig().getStringList("whitelist")) {
            if (whitelist.startsWith("regex:")) {
                finish = finish.toLowerCase().replaceAll(whitelist.toLowerCase().substring(6).replace("{tld}", main.getTldRegex()), ""); // Replace regex
            } else {
                finish = finish.toLowerCase().replace(whitelist.toLowerCase(), ""); // Don't parse regex
            }
        }
        main.sendDebug("Checked for whitelist, " + finish);
        return finish;
    }

    /**
     * Checks if the message is on the absolute whitelist and no further checks should be run.
     * @param str The message to check
     * @return True if on absolute whitelist, false if not
     */
    public boolean checkForAbsoluteWhitelist(String str) {
        for (String absolute : main.getConfig().getStringList("absolute-whitelist")) {
            if (absolute.startsWith("regex:")) {
                Pattern p = Pattern.compile(absolute.substring(6).replace("{tld}", main.getTldRegex()));
                Matcher m = p.matcher(str);
                if (m.find()) {
                    main.sendDebug("Message contained absolute-whitelisted regex " + absolute);
                    return true;
                }
            }
            else if (str.contains(absolute.toLowerCase())) {
                main.sendDebug("Message contained an absolute-whitelist string " + absolute);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the message is on the blacklist. Blacklist is a custom regex/contains check defined by the administrator and if this returns true, the message should be treated as advertising.
     * @param str The message to check
     * @return True if message contains a blacklisted word (or matches a blacklist regex), false if all good.
     */
    public boolean checkForBlacklist(String str) {
        if (!main.getConfig().getBoolean("checks.blacklist")) {
            return false;
        }
        for (String blacklist : main.getConfig().getStringList("blacklist")) {
            if (blacklist.startsWith("regex:")) {
                if (Pattern.compile(blacklist.substring(6).replace("{tld}", main.getTldRegex())).matcher(str).find()) {
                    main.sendDebug("Message contained blacklisted Regular Expression " + blacklist);
                    return true;
                }
            }
            else if (str.contains(blacklist.toLowerCase())) {
                main.sendDebug("Message contained blacklisted phrase " + blacklist);
                return true;
            }
            main.sendDebug(str + " does not contain " + blacklist);
        }
        return false;
    }

    /**
     * Checks if the message is "safe", ie. doesn't contain non-whitelisted advertising.
     * @param player The player who sent the message
     * @param message The message sent by the player.
     * @return True if the message is clear, false if it contains advertising.
     */
    public boolean safeChat(ProxiedPlayer player, String message) {
        message = message.toLowerCase();
        message = message.replace("\n", "");
        if (checkForAbsoluteWhitelist(message)) {
            main.sendDebug("Message is on absolute whitelist");
            return true;
        }
        if (!player.hasPermission("antiadvertiser.bypass.blacklist") && checkForBlacklist(message)) {
            main.sendDebug("Message contains blacklisted message.");
            return false;
        }
        String whitelist = checkForWhitelist(message);
        if (!whitelist.equals(message)) {
            main.sendDebug("Message is partially whitelisted, changing " + message + " to " + whitelist);
            message = whitelist;
        }
        if (!player.hasPermission("antiadvertiser.bypass.ip") && checkForIp(message)) {
            main.sendDebug("Message contains IP");
            return false;
        }
        if (!player.hasPermission("antiadvertiser.bypass.domain") && checkForDomain(message)) {
            main.sendDebug("Message contains domain name");
            return false;
        }
        main.sendDebug("Message is good.");
        return true;
    }
}
