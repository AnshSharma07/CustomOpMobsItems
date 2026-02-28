/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.opmobsoptools.init;

import net.mcreator.opmobsoptools.client.model.Modelwolfnew;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class OpMobsOpToolsModModels {
	public static void clientLoad() {
		EntityModelLayerRegistry.registerModelLayer(Modelwolfnew.LAYER_LOCATION, Modelwolfnew::createBodyLayer);
	}
}