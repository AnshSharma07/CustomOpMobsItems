/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.opmobsoptools.init;

import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import net.mcreator.opmobsoptools.item.WolfReaperToolItem;
import net.mcreator.opmobsoptools.item.ShadowstepDaggerToolItem;
import net.mcreator.opmobsoptools.item.ReaperScytheToolItem;
import net.mcreator.opmobsoptools.item.EarthBreakerHammerToolItem;
import net.mcreator.opmobsoptools.item.BlazingTuskBladeToolItem;
import net.mcreator.opmobsoptools.OpMobsOpToolsMod;

import java.util.function.Function;

public class OpMobsOpToolsModItems {
	public static Item INFERNO_HOG_SPAWN_EGG;
	public static Item BLAZING_TUSK_BLADE_TOOL;
	public static Item WOLF_REAPER_TOOL;
	public static Item EARTH_BREAKER_HAMMER_TOOL;
	public static Item SHADOWSTEP_DAGGER_TOOL;
	public static Item REAPER_SCYTHE_TOOL;
	public static Item ENDER_SPIDER_SPAWN_EGG;
	public static Item HELL_JAMMER_COW_SPAWN_EGG;
	public static Item REAPER_ZOMBIE_SPAWN_EGG;
	public static Item MYTHIC_WOLF_SPAWN_EGG;

	public static void load() {
		INFERNO_HOG_SPAWN_EGG = register("inferno_hog_spawn_egg", properties -> new SpawnEggItem(OpMobsOpToolsModEntities.INFERNO_HOG, properties));
		BLAZING_TUSK_BLADE_TOOL = register("blazing_tusk_blade_tool", BlazingTuskBladeToolItem::new);
		WOLF_REAPER_TOOL = register("wolf_reaper_tool", WolfReaperToolItem::new);
		EARTH_BREAKER_HAMMER_TOOL = register("earth_breaker_hammer_tool", EarthBreakerHammerToolItem::new);
		SHADOWSTEP_DAGGER_TOOL = register("shadowstep_dagger_tool", ShadowstepDaggerToolItem::new);
		REAPER_SCYTHE_TOOL = register("reaper_scythe_tool", ReaperScytheToolItem::new);
		ENDER_SPIDER_SPAWN_EGG = register("ender_spider_spawn_egg", properties -> new SpawnEggItem(OpMobsOpToolsModEntities.ENDER_SPIDER, properties));
		HELL_JAMMER_COW_SPAWN_EGG = register("hell_jammer_cow_spawn_egg", properties -> new SpawnEggItem(OpMobsOpToolsModEntities.HELL_JAMMER_COW, properties));
		REAPER_ZOMBIE_SPAWN_EGG = register("reaper_zombie_spawn_egg", properties -> new SpawnEggItem(OpMobsOpToolsModEntities.REAPER_ZOMBIE, properties));
		MYTHIC_WOLF_SPAWN_EGG = register("mythic_wolf_spawn_egg", properties -> new SpawnEggItem(OpMobsOpToolsModEntities.MYTHIC_WOLF, properties));
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> I register(String name, Function<Item.Properties, ? extends I> supplier) {
		return (I) Items.registerItem(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(OpMobsOpToolsMod.MODID, name)), (Function<Item.Properties, Item>) supplier);
	}
}