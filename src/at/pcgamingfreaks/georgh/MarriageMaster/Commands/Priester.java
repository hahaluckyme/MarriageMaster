/*
 *   Copyright (C) 2014 GeorgH93
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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.georgh.MarriageMaster.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;
import at.pcgamingfreaks.georgh.MarriageMaster.Marry_Requests;

public class Priester 
{
	private MarriageMaster marriageMaster;
	
	public Priester(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
	}
	
	public void Marry(CommandSender priester, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player == priester || otherPlayer == priester)
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotYourSelf"));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.NotWithHimself"),player.getName()));
			return;
		}
		String a1 = marriageMaster.DB.GetPartner(player.getName());
		String a2 = marriageMaster.DB.GetPartner(otherPlayer.getName());
		if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyMarried"));
			return;
		}
		if(marriageMaster.config.UseEconomy())
		{
			if(marriageMaster.economy.Marry(player, otherPlayer, marriageMaster.config.GetEconomyMarry()))
			{
				marriageMaster.DB.MarryPlayers(player.getName(), otherPlayer.getName(), "Console");
				priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Married"), player.getName(), otherPlayer.getName()));
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", otherPlayer.getName()));
				otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", player.getName()));
				if(marriageMaster.config.GetAnnouncementEnabled())
				{
					marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.BroadcastMarriage"), "Console", player.getName(), otherPlayer.getName()));
				}
			}
		}
		else
		{
			marriageMaster.DB.MarryPlayers(player.getName(), otherPlayer.getName(), "Console");
			priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Married"), player.getName(), otherPlayer.getName()));
			player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", otherPlayer.getName()));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", player.getName()));
			if(marriageMaster.config.GetAnnouncementEnabled())
			{
				marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.BroadcastMarriage"), "Console", player.getName(), otherPlayer.getName()));
			}
		}
	}

	public void Marry(Player priester, String[] args) 
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.NotWithHimself"),player.getName()));
		}
		else
		{
			if(InRadius(player, otherPlayer, priester))
			{
				String a1 = marriageMaster.DB.GetPartner(player.getName());
				String a2 = marriageMaster.DB.GetPartner(otherPlayer.getName());
				if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
				{
					priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyMarried"));
					return;
				}
				if(marriageMaster.config.UseEconomy())
				{
					if(marriageMaster.economy.Marry(player, otherPlayer, marriageMaster.config.GetEconomyMarry()))
					{
						MarryPlayer(priester, player, otherPlayer);
					}
				}
				else
				{
					MarryPlayer(priester, player, otherPlayer);
				}
			}
			else
			{
				priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
			}
		}
	}
	
	public void AcceptMarriage(Player player)
	{
		for (Marry_Requests m : marriageMaster.mr)
		{
    		if(m.p1 == player || m.p2 == player)
    		{
    			if(!m.HasAccepted(player))
    			{
    				marriageMaster.mr.remove(m);
    				m.Accept(player);
    				if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog())
					{
    					player.chat(marriageMaster.lang.Get("Dialog.YesIWant"));
					}
    				if(m.BothAcceoted(player))
    				{
    					SaveMarry(m.priest, m.p1, m.p2);
    				}
    				else
    				{
    					if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog())
    					{
    						m.priest.chat(String.format(marriageMaster.lang.Get("Dialog.AndDoYouWant"), m.p2.getName(), m.p1.getName()));
    					}
    					m.p2.sendMessage(marriageMaster.lang.Get("Priest.Confirm"));
    					marriageMaster.mr.add(m);
    				}
    			}
    			else
    			{
    				player.sendMessage(marriageMaster.lang.Get("Priest.AlreadyAccepted"));
    			}
    			return;
    		}
    	}
    	player.sendMessage(marriageMaster.lang.Get("Priest.NoRequest"));
	}
	
	private void SaveMarry(Player priest, Player player, Player otherPlayer)
	{
		marriageMaster.DB.MarryPlayers(player.getName(), otherPlayer.getName(), priest.getName());
		priest.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Married"), player.getName(), otherPlayer.getName()));
		player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), priest.getName(), otherPlayer.getName()));
		otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), priest.getName(), player.getName()));
		if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog())
		{
			priest.chat(marriageMaster.lang.Get("Dialog.Married"));
		}
		if(marriageMaster.config.GetAnnouncementEnabled())
		{
			marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.BroadcastMarriage"), priest.getName(), player.getName(), otherPlayer.getName()));
		}
	}

	private void MarryPlayer(Player priest, Player player, Player otherPlayer) 
	{
		if(!marriageMaster.config.UseConfirmation())
		{
			SaveMarry(priest, player, otherPlayer);
		}
		else
		{
			marriageMaster.mr.add(new Marry_Requests(priest, player, otherPlayer));
			if(marriageMaster.config.UseConfirmationAutoDialog())
			{
				priest.chat(String.format(marriageMaster.lang.Get("Dialog.DoYouWant"), player.getName(), otherPlayer.getName()));
			}
			player.sendMessage(marriageMaster.lang.Get("Priest.Confirm"));
		}
	}

	private boolean InRadius(Player player, Player otherPlayer, Player priest) 
	{
		return marriageMaster.InRadius(player, priest, marriageMaster.config.GetRange("Marry")) && marriageMaster.InRadius(otherPlayer, priest, marriageMaster.config.GetRange("Marry"));
	}
	
	public void setPriest(String[] args, CommandSender sender)
	{
		Player player = marriageMaster.getServer().getPlayer(args[1]);
		if(player != null && player.isOnline())
		{
			if(marriageMaster.IsPriester(player))
			{
				marriageMaster.DB.DelPriest(player.getName());
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeAPriest"), player.getName()));
			}
			else
			{
				marriageMaster.DB.SetPriest(player.getName());
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeAPriest"), player.getName()));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
		}
	}

	public void setPriest(String[] args, Player sender) 
	{
		Player player = marriageMaster.getServer().getPlayer(args[1]);
		if(player != null && player.isOnline())
		{
			if(marriageMaster.IsPriester(player))
			{
				marriageMaster.DB.DelPriest(player.getName());
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeAPriest"), player.getName()));
			}
			else
			{
				marriageMaster.DB.SetPriest(player.getName());
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeAPriest"), player.getName()));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
		}
	}
	
	public void Divorce(Player priester, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = marriageMaster.DB.GetPartner(player.getName());
		if(otP == null || otP.isEmpty())
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.PartnerOffline"), args[1],otP));
			return;
		}
		if(InRadius(player, otherPlayer, priester))
		{ 
			if(marriageMaster.config.UseEconomy())
			{
				if(marriageMaster.economy.Divorce(player, otherPlayer, marriageMaster.config.GetEconomyDivorce()))
				{
					DivorcePlayer(priester, player, otherPlayer);
				}
			}
			else
			{
				DivorcePlayer(priester, player, otherPlayer);
			}
		}
		else
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
		}
	}
	
	public void Divorce(CommandSender priester, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = marriageMaster.DB.GetPartner(player.getName());
		if(otP == null || otP.isEmpty())
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.PartnerOffline"), args[1],otP));
			return;
		}
		String p1 = marriageMaster.DB.GetPartner(otherPlayer.getName());
		String p2 = marriageMaster.DB.GetPartner(player.getName());
		if(p1.equalsIgnoreCase(player.getName()) && p2.equalsIgnoreCase(otherPlayer.getName()))
		{
			if(marriageMaster.config.UseEconomy())
			{
				if(marriageMaster.economy.Divorce(player, otherPlayer, marriageMaster.config.GetEconomyDivorce()))
				{
					DivorcePlayer(priester, player, otherPlayer);
				}
			}
			else
			{
				DivorcePlayer(priester, player, otherPlayer);
			}
		}	
		else
		{
			priester.sendMessage(ChatColor.RED + player.getName() + " is not married with " + otherPlayer.getName());
		}
	}
	
	private void DivorcePlayer(CommandSender priester, Player player, Player otherPlayer) 
	{
		marriageMaster.DB.DivorcePlayer(player.getName(), otherPlayer.getName());
		priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getName(),otherPlayer.getName()));
		player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), "Console", otherPlayer.getName()));
		otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), "Console", player.getName()));
	}

	private void DivorcePlayer(Player priester, Player player, Player otherPlayer) 
	{
		marriageMaster.DB.DivorcePlayer(player.getName(), otherPlayer.getName());
		priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getName(),otherPlayer.getName()));
		player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priester.getName(), otherPlayer.getName()));
		otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priester.getName(), player.getName()));
	}
}