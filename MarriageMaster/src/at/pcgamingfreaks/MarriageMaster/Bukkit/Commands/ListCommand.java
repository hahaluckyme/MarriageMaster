/*
 *   Copyright (C) 2022 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListCommand extends MarryCommand
{
	private final int entriesPerPage;
	private final Message messageHeadlineMain, messageFooter, messageListFormat, messageNoMarriedPlayers;
	private final boolean useFooter;

	public ListCommand(final @NotNull MarriageMaster plugin)
	{
		super(plugin, "list", plugin.getLanguage().getTranslated("Commands.Description.List"), Permissions.LIST, plugin.getLanguage().getCommandAliases("List"));

		useFooter        = plugin.getConfiguration().useListFooter();
		entriesPerPage   = plugin.getConfiguration().getListEntriesPerPage();

		messageListFormat         = plugin.getLanguage().getMessage("Ingame.List.Format").replaceAll("\\{Player1Name}", "%1\\$s").replaceAll("\\{Player2Name}", "%2\\$s").replaceAll("\\{Player1DisplayName}", "%3\\$s").replaceAll("\\{Player2DisplayName}", "%4\\$s").replaceAll("\\{Surname}", "%5\\$s").replaceAll("\\{MagicHeart}", "%6\\$s");
		messageHeadlineMain       = plugin.getLanguage().getMessage("Ingame.List.Headline").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s").replaceAll("\\{PrevPage}", "%5\\$d").replaceAll("\\{NextPage}", "%6\\$d");
		messageFooter             = plugin.getLanguage().getMessage("Ingame.List.Footer").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s").replaceAll("\\{PrevPage}", "%5\\$d").replaceAll("\\{NextPage}", "%6\\$d");
		messageNoMarriedPlayers   = plugin.getLanguage().getMessage("Ingame.List.NoMarriedPlayers");
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		Collection<? extends Marriage> couples = getMarriagePlugin().getMarriages();
		if(!couples.isEmpty()) // There are married couples
		{
			int page = 0;
			if(args.length == 1)
			{
				try
				{
					page = StringUtils.parsePageNumber(args[0]);
				}
				catch(NumberFormatException ignored)
				{
					((MarriageMaster) getMarriagePlugin()).messageNotANumber.send(sender);
					return;
				}
			}
			int c = entriesPerPage, availablePages = (int) Math.ceil(couples.size() / (float)entriesPerPage);
			page = Math.min(page, availablePages - 1);
			messageHeadlineMain.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);
			Iterator<? extends Marriage> couplesIterator = couples.iterator();
			for(int i = 0; couplesIterator.hasNext() && i < page * entriesPerPage; i++)
			{
				couplesIterator.next();
			}
			final boolean isPlayer = sender instanceof Player;
			while(couplesIterator.hasNext() && --c >= 0)
			{
				Marriage couple = couplesIterator.next();
				MarriagePlayer p1 = couple.getPartner1(), p2 = couple.getPartner2();
				String p1DName = isPlayer ? p1.getDisplayNameCheckVanished((Player) sender) : p1.getDisplayName();
				String p2DName = isPlayer ? p2.getDisplayNameCheckVanished((Player) sender) : p2.getDisplayName();
				messageListFormat.send(sender, p1.getName(), p2.getName(), p1DName, p2DName, couple.getSurnameString(), couple.getMagicHeart());
			}
			if(useFooter)
			{
				messageFooter.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);
			}
		}
		else
		{
			messageNoMarriedPlayers.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		return null;
	}
}