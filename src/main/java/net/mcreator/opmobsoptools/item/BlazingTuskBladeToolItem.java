package net.mcreator.opmobsoptools.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;

import net.mcreator.opmobsoptools.OpMobsOpToolsMod;

public class BlazingTuskBladeToolItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 4f, 0, 2, TagKey.create(Registries.ITEM, ResourceLocation.parse("op_mobs_op_tools:blazing_tusk_blade_tool_repair_items")));

	public BlazingTuskBladeToolItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 3f, -2f).fireResistant());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel serverLevel))
			return InteractionResultHolder.success(stack);
		if (player.getCooldowns().isOnCooldown(this))
			return InteractionResultHolder.fail(stack);

		if (player.isShiftKeyDown()) {
			player.getCooldowns().addCooldown(this, 100);
			serverLevel.playSound(null, player.blockPosition(), BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.warden.sonic_charge")), SoundSource.PLAYERS, 1.3f, 1.0f);
			Vec3 origin = player.position();
			OpMobsOpToolsMod.queueServerWork(10, () -> {
				for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(origin, origin).inflate(4.0), e -> e != player && e.isAlive())) {
					entity.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 12.0f);
				}
				serverLevel.sendParticles(ParticleTypes.FLAME, origin.x, origin.y + 0.5, origin.z, 40, 0.9, 0.5, 0.9, 0.02);
			});
		} else {
			player.getCooldowns().addCooldown(this, 20);
			Vec3 look = player.getLookAngle().normalize();
			SmallFireball fireball = new SmallFireball(serverLevel, player, look);
			fireball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
			serverLevel.addFreshEntity(fireball);
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 160));
		return super.hurtEnemy(stack, target, attacker);
	}
}
