/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.opmobsoptools.init;

import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class OpMobsOpToolsModTabs {
	public static void load() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(tabData -> {
			tabData.accept(OpMobsOpToolsModItems.INFERNO_HOG_SPAWN_EGG);
			tabData.accept(OpMobsOpToolsModItems.ENDER_SPIDER_SPAWN_EGG);
			tabData.accept(OpMobsOpToolsModItems.HELL_JAMMER_COW_SPAWN_EGG);
			tabData.accept(OpMobsOpToolsModItems.REAPER_ZOMBIE_SPAWN_EGG);
			tabData.accept(OpMobsOpToolsModItems.MYTHIC_WOLF_SPAWN_EGG);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tabData -> {
			tabData.accept(OpMobsOpToolsModItems.BLAZING_TUSK_BLADE_TOOL);
			tabData.accept(OpMobsOpToolsModItems.WOLF_REAPER_TOOL);
			tabData.accept(OpMobsOpToolsModItems.EARTH_BREAKER_HAMMER_TOOL);
			tabData.accept(OpMobsOpToolsModItems.SHADOWSTEP_DAGGER_TOOL);
			tabData.accept(OpMobsOpToolsModItems.REAPER_SCYTHE_TOOL);
		});
	}
}