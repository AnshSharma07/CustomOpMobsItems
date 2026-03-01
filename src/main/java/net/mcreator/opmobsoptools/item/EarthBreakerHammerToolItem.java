package net.mcreator.opmobsoptools.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

public class EarthBreakerHammerToolItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 4f, 0, 2, TagKey.create(Registries.ITEM, ResourceLocation.parse("op_mobs_op_tools:earth_breaker_hammer_tool_repair_items")));

	public EarthBreakerHammerToolItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 8f, -2.4f).fireResistant());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel serverLevel))
			return InteractionResultHolder.success(stack);
		if (player.getCooldowns().isOnCooldown(this))
			return InteractionResultHolder.fail(stack);

		player.getCooldowns().addCooldown(this, 600);
		serverLevel.playSound(null, player.blockPosition(), BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.warden.sonic_boom")), SoundSource.PLAYERS, 1.4f, 1.0f);
		player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 4));

		for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0), e -> e != player && e.isAlive())) {
			entity.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 16.0f);
		}

		BlockPos center = player.blockPosition();
		for (int x = -4; x <= 4; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -4; z <= 4; z++) {
					if (x * x + z * z > 16)
						continue;
					BlockPos breakPos = center.offset(x, y, z);
					if (!serverLevel.getBlockState(breakPos).isAir())
						serverLevel.destroyBlock(breakPos, true, player);
				}
			}
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker.level() instanceof ServerLevel serverLevel)
			serverLevel.playSound(null, attacker.blockPosition(), BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("block.anvil.place")), SoundSource.PLAYERS, 1.0f, 1.0f);
		return super.hurtEnemy(stack, target, attacker);
	}
}
