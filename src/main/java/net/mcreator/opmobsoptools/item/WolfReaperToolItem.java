package net.mcreator.opmobsoptools.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

public class WolfReaperToolItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 4f, 0, 2, TagKey.create(Registries.ITEM, ResourceLocation.parse("op_mobs_op_tools:wolf_reaper_tool_repair_items")));

	public WolfReaperToolItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 3f, -2f).fireResistant());
	}
}