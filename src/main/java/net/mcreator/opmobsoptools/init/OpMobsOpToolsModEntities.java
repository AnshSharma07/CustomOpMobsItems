/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.opmobsoptools.init;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

import net.mcreator.opmobsoptools.entity.ReaperZombieEntity;
import net.mcreator.opmobsoptools.entity.MythicWolfEntity;
import net.mcreator.opmobsoptools.entity.InfernoHogEntity;
import net.mcreator.opmobsoptools.entity.HellJammerCowEntity;
import net.mcreator.opmobsoptools.entity.EnderSpiderEntity;
import net.mcreator.opmobsoptools.OpMobsOpToolsMod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class OpMobsOpToolsModEntities {
	public static EntityType<InfernoHogEntity> INFERNO_HOG;
	public static EntityType<EnderSpiderEntity> ENDER_SPIDER;
	public static EntityType<HellJammerCowEntity> HELL_JAMMER_COW;
	public static EntityType<ReaperZombieEntity> REAPER_ZOMBIE;
	public static EntityType<MythicWolfEntity> MYTHIC_WOLF;

	public static void load() {
		INFERNO_HOG = register("inferno_hog", EntityType.Builder.<InfernoHogEntity>of(InfernoHogEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3).fireImmune()

				.sized(1.8f, 1.8f));
		ENDER_SPIDER = register("ender_spider", EntityType.Builder.<EnderSpiderEntity>of(EnderSpiderEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3)

				.sized(1.4f, 0.9f));
		HELL_JAMMER_COW = register("hell_jammer_cow", EntityType.Builder.<HellJammerCowEntity>of(HellJammerCowEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3)

				.sized(0.9f, 1.4f));
		REAPER_ZOMBIE = register("reaper_zombie", EntityType.Builder.<ReaperZombieEntity>of(ReaperZombieEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3)

				.ridingOffset(-0.6f).sized(0.6f, 1.8f));
		MYTHIC_WOLF = register("mythic_wolf", EntityType.Builder.<MythicWolfEntity>of(MythicWolfEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3)

				.sized(0.6f, 1.8f));
		init();
		registerAttributes();
	}

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> EntityType<T> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(OpMobsOpToolsMod.MODID, registryname),
				(EntityType<T>) entityTypeBuilder.build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(OpMobsOpToolsMod.MODID, registryname))));
	}

	public static void init() {
	}

	public static void registerAttributes() {
		FabricDefaultAttributeRegistry.register(INFERNO_HOG, InfernoHogEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ENDER_SPIDER, EnderSpiderEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(HELL_JAMMER_COW, HellJammerCowEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(REAPER_ZOMBIE, ReaperZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MYTHIC_WOLF, MythicWolfEntity.createAttributes());
	}
}