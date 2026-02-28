package net.mcreator.opmobsoptools;

import net.mcreator.opmobsoptools.init.OpMobsOpToolsModModels;
import net.mcreator.opmobsoptools.init.OpMobsOpToolsModEntityRenderers;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class OpMobsOpToolsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Start of user code block mod constructor
		// End of user code block mod constructor
		OpMobsOpToolsModModels.clientLoad();
		OpMobsOpToolsModEntityRenderers.clientLoad();
		// Start of user code block mod init
		// End of user code block mod init
	}
	// Start of user code block mod methods
	// End of user code block mod methods
}