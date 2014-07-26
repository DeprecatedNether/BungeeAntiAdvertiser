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

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import pw.deprecatednether.antiadvertiser.bungee.AntiAdveritser;

public class ReloadCommand extends Command {
    private AntiAdveritser main;

    public ReloadCommand(AntiAdveritser main) {
        super("aareload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
