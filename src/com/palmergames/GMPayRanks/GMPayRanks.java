package com.palmergames.GMPayRanks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class GMPayRanks extends JavaPlugin
{
	protected boolean UsePermissions;
    protected final Logger logger = Logger.getLogger("Minecraft");
	protected File RankFile;
	protected FileConfiguration Rank;
	HashMap<String, String> ranks = new HashMap<String, String>();
	protected int Hashlen;
	
	protected WorldsHolder permission = null;
    
	public void onEnable()
	{
		if (EconomyHandler.setupEconomy() && setupPermission()) {
		
		    PluginDescriptionFile pdfFile = this.getDescription();
		    this.logger.info("[" + pdfFile.getName() + "] Economy handler type set to " + EconomyHandler.getType().toString());
			this.logger.info("[" + pdfFile.getName() + "] v" + pdfFile.getVersion() + " has been enabled.");
			RankFile = new File(getDataFolder(), "rankprices.yml");
		    try {
		        firstRun();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    Rank = new YamlConfiguration();
		    loadYamls();
		    setupHash();
		} else {
			this.getServer().getPluginManager().disablePlugin(this);
		}

	}
    
    private Boolean setupPermission()
    {
        RegisteredServiceProvider<WorldsHolder> permissionProvider = getServer().getServicesManager().getRegistration(org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return(permission != null);
    }
	public void onDisable()
	{
		// Release all handles.
		Rank = null;
		ranks = null;
		permission = null;
		
	    PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("[" + pdfFile.getName() + "] has been disabled.");
	}
	
	public void setupHash()
	{
	    List<String> ranklist = Rank.getStringList("groupslist");
	    String[] rankarray = ranklist.toArray(new String[]{});
	    Integer i = 0;
	    Hashlen = rankarray.length;
	    while(i < rankarray.length)
	    {
	    	String rnum = i.toString();
	    	String key = ("rank" + rnum);
	    	ranks.put(key, rankarray[i]);
	    	i++;
	    }
	}
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args)
	{
		readCommand((Player) sender, CommandLabel, args);
		return false;
	}
	
	public void readCommand(final Player sender, String command, String[] args)
	{
		if(command.equalsIgnoreCase("rankup"))
		{
			List<String> rlist= Rank.getStringList("groupslist");
		    String[] rarr = rlist.toArray(new String[]{});
		    Integer Len = rarr.length;
			Integer i = Len;
			Integer nextr = (Len + 1);
			while(i > -1)
			{
				String rnum = i.toString();
		    	String key = ("rank" + rnum);
		    	String rank = ranks.get(key);
		    	if(permission.getWorldData(sender).getPermissionsHandler().inGroup(sender.getName(), rank))
		    	{
		    		if(i == (Len - 1))
		    		{
		    			sender.sendMessage(ChatColor.GOLD + "You are at the highest possible paid rank!");
		    			return;
		    		}
		    		else if(i < Len)
		    		{
		    			String rnumx = nextr.toString();
		    			String keyx = ("rank" + rnumx);
		    			String rankx = ranks.get(keyx);
		    			if(EconomyHandler.hasEnough(sender, Rank.getDouble("groups." + rankx)))
		    			{
		    				Group newGroup = permission.getWorldData(sender).getGroup(rankx);
		    				permission.getWorldData(sender).getUser(sender.getName()).setGroup(newGroup);
		    				EconomyHandler.subtract(sender,Rank.getDouble("groups." + rankx));
		    				//sender.sendMessage(ChatColor.GREEN + "You have been promoted to the rank of: " + ChatColor.BLUE + rankx);
		    				Bukkit.broadcastMessage(ChatColor.AQUA + sender.getName() + ChatColor.GREEN + " has been promoted to the rank of: " + ChatColor.BLUE + rankx);
		    				return;
		    			}
		    			else
		    			{
		    				Double price = Rank.getDouble("groups." + rankx);
		    				String pricex = price.toString();
		    				sender.sendMessage(ChatColor.RED + "You need " + ChatColor.BLUE + pricex + ChatColor.RED + " to purchase the rank of: " + ChatColor.BLUE + rankx);
		    				return;
		    			}
		    		}
		    	}
		    	nextr--;
		    	i--;
			}
		}
	}
	
	public void saveYamls() {
	    try {
	        Rank.save(RankFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	public void loadYamls() {
	    try {
	        Rank.load(RankFile);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	private void firstRun() throws Exception
	{
	    if(!RankFile.exists()){
	        RankFile.getParentFile().mkdirs();
	        copy(getResource("rankprices.yml"), RankFile);
	    }
	}

	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
