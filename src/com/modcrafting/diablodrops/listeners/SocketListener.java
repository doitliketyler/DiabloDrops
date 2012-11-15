package com.modcrafting.diablodrops.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import com.modcrafting.diablodrops.DiabloDrops;
import com.modcrafting.skullapi.lib.Skull;
import com.modcrafting.toolapi.lib.Tool;
import com.stirante.ItemNamer.Namer;

public class SocketListener implements Listener
{
	DiabloDrops plugin;

	public SocketListener(DiabloDrops instance)
	{
		plugin = instance;
	}

	@EventHandler
	public void onSmeltSocket(FurnaceSmeltEvent event)
	{
		if (!plugin.furnanceMap.containsKey(event.getBlock())
				&& !plugin.drop.isTool(event.getResult().getType()))
			return;
		ItemStack is = plugin.furnanceMap.remove(event.getBlock());
		Material fuel = is.getType();
		Tool tool = new Tool(event.getResult().getType());
		Tool oldtool = new Tool(event.getSource());
		boolean namTest = false;
		for (String n : oldtool.getLoreList())
		{
			if (n.equalsIgnoreCase("(Socket)"))
				namTest = true;
		}
		if (!namTest)
		{
			event.setResult(event.getSource());
			return;
		}

		int eni = plugin.config.getInt("SocketItem.EnhanceBy", 1);
		int ene = plugin.config.getInt("SocketItem.EnhanceMax", 10);
		for (Enchantment ench : oldtool.getEnchantments().keySet())
		{
			int il = oldtool.getEnchantments().get(ench);
			if (il < ene)
				il = il + eni;
			tool.addUnsafeEnchantment(ench, il);
		}

		if (fuel.equals(Material.SKULL))
		{
			ChatColor color = this.findColor(oldtool.getName());
			String skullName = new Skull(((CraftItemStack) is).getHandle())
					.getOwner();
			tool.setName(color + skullName + "'s "
					+ ChatColor.stripColor(oldtool.getName()));
		}
		else
		{
			tool.setName(oldtool.getName());
		}
		// TODO: Find specific lore per item type. Current just random
		if (plugin.config.getBoolean("Lore.Enabled", true))
		{
			for (int i = 0; i < plugin.config.getInt("Lore.EnhanceAmount", 2); i++)
			{
				tool.setLore(plugin.lore.get(plugin.gen.nextInt(plugin.lore
						.size())));
			}
		}
		event.setResult(tool);
		return;

	}

	@EventHandler
	public void burnGem(FurnaceBurnEvent event)
	{
		for (String name : plugin.config.getStringList("SocketItem.Items"))
		{
			if (event.getFuel().getType().equals(Material.matchMaterial(name)))
			{
				if (Namer.getName(event.getFuel()) == null||!Namer.getName(event.getFuel()).contains("Socket"))
				{
					event.setCancelled(true);
					event.setBurning(false);
					event.setBurnTime(120000);
					return;
				}
				plugin.furnanceMap.put(event.getBlock(), event.getFuel());
				event.setBurnTime(240);
				event.setBurning(true);
			}
		}
	}

	public ChatColor findColor(String s)
	{
		char[] c = s.toCharArray();
		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == new Character((char) 167))
			{
				return ChatColor.getByChar(c[i + 1]);
			}
		}
		return null;
	}
}