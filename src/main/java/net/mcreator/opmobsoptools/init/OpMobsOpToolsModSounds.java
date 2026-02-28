/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.opmobsoptools.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

import net.mcreator.opmobsoptools.OpMobsOpToolsMod;

public class OpMobsOpToolsModSounds {
	public static SoundEvent INFERNODEATH;
	public static SoundEvent ZOMBIEGROWL;

	public static void load() {
		INFERNODEATH = register("infernodeath", SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("op_mobs_op_tools", "infernodeath")));
		ZOMBIEGROWL = register("zombiegrowl", SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("op_mobs_op_tools", "zombiegrowl")));
	}

	private static SoundEvent register(String registryname, SoundEvent element) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(OpMobsOpToolsMod.MODID, registryname), element);
	}
}