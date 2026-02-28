/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.opmobsoptools.init;

import net.mcreator.opmobsoptools.client.renderer.ReaperZombieRenderer;
import net.mcreator.opmobsoptools.client.renderer.MythicWolfRenderer;
import net.mcreator.opmobsoptools.client.renderer.InfernoHogRenderer;
import net.mcreator.opmobsoptools.client.renderer.HellJammerCowRenderer;
import net.mcreator.opmobsoptools.client.renderer.EnderSpiderRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class OpMobsOpToolsModEntityRenderers {
	public static void clientLoad() {
		EntityRendererRegistry.register(OpMobsOpToolsModEntities.INFERNO_HOG, InfernoHogRenderer::new);
		EntityRendererRegistry.register(OpMobsOpToolsModEntities.ENDER_SPIDER, EnderSpiderRenderer::new);
		EntityRendererRegistry.register(OpMobsOpToolsModEntities.HELL_JAMMER_COW, HellJammerCowRenderer::new);
		EntityRendererRegistry.register(OpMobsOpToolsModEntities.REAPER_ZOMBIE, ReaperZombieRenderer::new);
		EntityRendererRegistry.register(OpMobsOpToolsModEntities.MYTHIC_WOLF, MythicWolfRenderer::new);
	}
}