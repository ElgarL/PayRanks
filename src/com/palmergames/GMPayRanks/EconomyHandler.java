package com.palmergames.GMPayRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.nijikokun.register.payment.Methods;
import com.nijikokun.register.payment.Method.MethodAccount;

/**
 * @author ElgarL
 *
 */
public class EconomyHandler {
	
	private static Economy vaultEconomy = null;
	
	private static EcoType Type = EcoType.NONE;
	
	public enum EcoType {
		NONE,
		ICO5,
		REGISTER,
		VAULT
	}
	
	/**
	 * @return the type
	 */
	public static EcoType getType() {
		return Type;
	}

	public static Boolean setupEconomy() {
		Plugin economyProvider = null;
		
		/*
		 * Test for native iCo5 support
		 */
    	economyProvider = Bukkit.getPluginManager().getPlugin("iConomy");

        if (economyProvider != null) {
        	/*
        	 * Flag as using iCo5 hooks
        	 */
        	if (economyProvider.getDescription().getVersion().matches("5.01")) {
        		Type = EcoType.ICO5;
        		return true;
        	}
        }
        
        /*
         * Attempt to hook Register
         */
        economyProvider = Bukkit.getPluginManager().getPlugin("Register");

        if (economyProvider != null) {
        	/*
        	 * Flag as using Register hooks
        	 */
        	Type = EcoType.REGISTER;
        	return true;
        }
        
        
        /*
         * Attempt to find Vault for Economy handling
         */
        RegisteredServiceProvider<Economy> vaultEcoProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (vaultEcoProvider != null) {
        	/*
        	 * Flag as using Vault hooks
        	 */
        	vaultEconomy = vaultEcoProvider.getProvider();
        	Type = EcoType.VAULT;
        	return true;
        }
        
        /*
         * No compatible Economy system found.
         */
        return false;
    }
	
	/**
	 * Returns the relevant players economy account
	 * 
	 * @param player
	 * @return
	 */
	private static Object getEconomyAccount(Player player) {
		
		switch (Type) {
		
		case ICO5:
			return iConomy.getAccount(player.getName());

		case REGISTER:
			if (!Methods.getMethod().hasAccount(player.getName()))
				Methods.getMethod().createAccount(player.getName());

			return Methods.getMethod().getAccount(player.getName());

		}
		
		return null;
	}
	
	/**
	 * Returns the accounts current balance
	 * 
	 * @param name
	 * @return
	 */
	public static double getBalance(Player player) {
		
		switch (Type) {
		
		case ICO5:
			Account icoAccount = (Account) getEconomyAccount(player);
			if (icoAccount != null)
				return icoAccount.getHoldings().balance();
			break;
			
		case REGISTER:
			MethodAccount registerAccount = (MethodAccount) getEconomyAccount(player);
			if (registerAccount != null)
				return registerAccount.balance(player.getWorld());

			break;
			
		case VAULT:
			if (!vaultEconomy.hasAccount(player.getName()))
				vaultEconomy.createPlayerAccount(player.getName());
			
			return vaultEconomy.getBalance(player.getName());
			
		}

		return 0.0;
	}
	
	/**
	 * Returns true if the account has enough money
	 * 
	 * @param name
	 * @param amount
	 * @return
	 */
	public static boolean hasEnough(Player player, Double amount) {
		
		if (getBalance(player) >= amount)
			return true;
		
		return false;
	}
	
	/**
	 * Attempts to remove an amount from an account
	 * 
	 * @param name
	 * @param amount
	 * @return
	 */
	public static boolean subtract(Player player, Double amount) {
		
		switch (Type) {
		
		case ICO5:
			Account icoAccount = (Account) getEconomyAccount(player);
			if (icoAccount != null) {
				icoAccount.getHoldings().subtract(amount);
				return true;
			}
			break;
			
		case REGISTER:
			MethodAccount registerAccount = (MethodAccount) getEconomyAccount(player);
			if (registerAccount != null)
				return registerAccount.subtract(amount, player.getWorld());
			break;
			
		case VAULT:
			if (!vaultEconomy.hasAccount(player.getName()))
				vaultEconomy.createPlayerAccount(player.getName());
			
			return vaultEconomy.withdrawPlayer(player.getName(), amount).type == EconomyResponse.ResponseType.SUCCESS;
			
		}
		
		return false;
	}

}
