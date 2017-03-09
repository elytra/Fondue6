package com.elytradev.fondue;

import java.util.Iterator;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid="fondue", name="Fondue", version="@VERSION@")
public class Fondue {
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		
		// furnace bread begin
		
		Iterator<IRecipe> iter = CraftingManager.getInstance().getRecipeList().iterator();
		while (iter.hasNext()) {
			IRecipe ir = iter.next();
			if (ir instanceof ShapedRecipes && ir.getRecipeOutput().getItem() == Items.BREAD) {
				iter.remove();
			}
		}
		
		FurnaceRecipes.instance().addSmelting(Items.WHEAT, new ItemStack(Items.BREAD), 0);
		
		// furnace bread end
	}
	
	// instant pickup begin
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onBreak(BlockEvent.BreakEvent e) {
		if (e.getWorld().restoringBlockSnapshots) return;
		if (!e.getWorld().getGameRules().getBoolean("doTileDrops")) return;
		if (e.getWorld().isRemote) return;
		if (e.getPlayer() == null) return;
		BlockPos pos = e.getPos();
		int amount = e.getExpToDrop();
		int oldCooldown = e.getPlayer().xpCooldown;
		while (amount > 0) {
			int i = EntityXPOrb.getXPSplit(amount);
			amount -= i;
			EntityXPOrb exp = new EntityXPOrb(e.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, i);
			e.getPlayer().xpCooldown = 0;
			int oldDelay = exp.delayBeforeCanPickup;
			exp.delayBeforeCanPickup = 0;
			e.getWorld().spawnEntity(exp);
			exp.onCollideWithPlayer(e.getPlayer());
			if (!exp.isDead) {
				exp.delayBeforeCanPickup = oldDelay;
			}
		}
		e.getPlayer().xpCooldown = oldCooldown;
		e.setExpToDrop(0);
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onDrops(BlockEvent.HarvestDropsEvent e) {
		if (e.getWorld().restoringBlockSnapshots) return;
		if (!e.getWorld().getGameRules().getBoolean("doTileDrops")) return;
		if (e.getWorld().isRemote) return;
		if (e.getHarvester() == null) return;
		BlockPos pos = e.getPos();
		for (ItemStack is : e.getDrops()) {
			if (is.isEmpty()) continue;
			System.out.println(is);
			if (e.getWorld().rand.nextFloat() <= e.getDropChance()) {
	            double xOfs = e.getWorld().rand.nextFloat() * 0.5 + 0.25;
	            double yOfs = e.getWorld().rand.nextFloat() * 0.5 + 0.25;
	            double zOfs = e.getWorld().rand.nextFloat() * 0.5 + 0.25;
	            EntityItem ei = new EntityItem(e.getWorld(), pos.getX()+xOfs, pos.getY()+yOfs, pos.getZ()+zOfs, is);
	            ei.setNoPickupDelay();
				e.getWorld().spawnEntity(ei);
				ei.onCollideWithPlayer(e.getHarvester());
				if (!ei.isDead) {
					ei.setDefaultPickupDelay();
				}
			}
		}
		e.getDrops().clear();
	}
	
	// instant pickup end
	
}
