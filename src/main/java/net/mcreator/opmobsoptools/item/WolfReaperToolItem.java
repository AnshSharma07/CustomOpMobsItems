package net.mcreator.opmobsoptools.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;

import net.mcreator.opmobsoptools.OpMobsOpToolsMod;

public class WolfReaperToolItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 4f, 0, 2, TagKey.create(Registries.ITEM, ResourceLocation.parse("op_mobs_op_tools:wolf_reaper_tool_repair_items")));

	public WolfReaperToolItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 3f, -2f).fireResistant());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel serverLevel))
			return InteractionResultHolder.success(stack);
		if (player.getCooldowns().isOnCooldown(this))
			return InteractionResultHolder.fail(stack);

		player.getCooldowns().addCooldown(this, 600);
		Wolf wolf = EntityType.WOLF.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
		if (wolf != null) {
			wolf.setPos(player.getX() + 1, player.getY(), player.getZ() + 1);
			wolf.setOwner(player);
			wolf.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(wolf.getMaxHealth() * 5.0);
			wolf.setHealth((float) wolf.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH));
			wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
			serverLevel.addFreshEntity(wolf);
			serverLevel.playSound(null, player.blockPosition(), BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.wolf.growl")), SoundSource.PLAYERS, 1.0f, 0.9f);
			OpMobsOpToolsMod.queueServerWork(300, () -> {
				if (wolf.isAlive())
					wolf.discard();
			});
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker.level() instanceof ServerLevel serverLevel && attacker instanceof Player player) {
			for (int i = 0; i < 3; i++) {
				Wolf wolf = EntityType.WOLF.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
				if (wolf == null)
					continue;
				wolf.setPos(target.getX() + (serverLevel.random.nextDouble() - 0.5) * 2.0, target.getY(), target.getZ() + (serverLevel.random.nextDouble() - 0.5) * 2.0);
				wolf.setOwner(player);
				wolf.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 4));
				wolf.setTarget(target);
				serverLevel.addFreshEntity(wolf);
				OpMobsOpToolsMod.queueServerWork(40, () -> {
					if (wolf.isAlive())
						wolf.discard();
				});
			}
			serverLevel.playSound(null, attacker.blockPosition(), BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.wolf.growl")), SoundSource.PLAYERS, 1.0f, 1.0f);
		}
		return super.hurtEnemy(stack, target, attacker);
	}
}
