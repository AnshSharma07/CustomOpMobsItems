package net.mcreator.opmobsoptools.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;

public class ShadowstepDaggerToolItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 4f, 0, 2, TagKey.create(Registries.ITEM, ResourceLocation.parse("op_mobs_op_tools:shadowstep_dagger_tool_repair_items")));

	public ShadowstepDaggerToolItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 3f, -2f));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel serverLevel))
			return InteractionResult.SUCCESS;
		if (player.getCooldowns().isOnCooldown(stack))
			return InteractionResult.FAIL;

		if (player.isShiftKeyDown()) {
			player.getCooldowns().addCooldown(stack, 20);
			serverLevel.explode(null, player.getX(), player.getY(), player.getZ(), 2.0f, Level.ExplosionInteraction.BLOCK);
			for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(2.5), e -> e != player && e.isAlive())) {
				entity.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 5.0f);
			}
		} else {
			player.getCooldowns().addCooldown(stack, 60);
			Vec3 look = player.getLookAngle().normalize().scale(2.0);
			player.setDeltaMovement(look.x * 5.0, look.y * 1.2, look.z * 5.0);
			player.hurtMarked = true;
		}
		return InteractionResult.SUCCESS;
	}
}
