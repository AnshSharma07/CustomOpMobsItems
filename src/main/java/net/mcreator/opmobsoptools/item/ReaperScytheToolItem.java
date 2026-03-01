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
import net.minecraft.core.particles.ParticleTypes;

public class ReaperScytheToolItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 4f, 0, 2, TagKey.create(Registries.ITEM, ResourceLocation.parse("op_mobs_op_tools:reaper_scythe_tool_repair_items")));

	public ReaperScytheToolItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 3f, -2f));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel serverLevel))
			return InteractionResultHolder.success(stack);
		if (player.getCooldowns().isOnCooldown(this))
			return InteractionResultHolder.fail(stack);

		player.getCooldowns().addCooldown(this, 200);
		serverLevel.playSound(null, player.blockPosition(), BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("block.sculk_shrieker.shriek")), SoundSource.PLAYERS, 1.3f, 1.0f);
		for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0), e -> e.isAlive() && !(e instanceof Player))) {
			entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 1));
			serverLevel.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(0.5), entity.getZ(), 8, 0.2, 0.2, 0.2, 0.01);
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof Player player) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 4));
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
		}
		return super.hurtEnemy(stack, target, attacker);
	}
}
