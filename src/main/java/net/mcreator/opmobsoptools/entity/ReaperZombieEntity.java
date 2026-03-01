package net.mcreator.opmobsoptools.entity;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.thrown.ThrownPotion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class ReaperZombieEntity extends Monster {
	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PINK, ServerBossEvent.BossBarOverlay.PROGRESS);
	private int summonCooldown = 400;
	private int plagueCooldown = 300;
	private boolean isSpinning;
	private int spinningTicks;
	private boolean isMinion;
	private int minionLifeTicks;
	private final Map<UUID, Integer> summonedMinionLifetimes = new HashMap<>();

	public ReaperZombieEntity(EntityType<ReaperZombieEntity> type, Level world) {
		super(type, world);
		xpReward = 170;
		setNoAi(false);
		setPersistenceRequired();
		refreshDimensions();
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
			@Override
			protected boolean canPerformAttack(LivingEntity entity) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
			}
		});
		this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(5, new FloatGoal(this));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public Vec3 getPassengerRidingPosition(Entity entity) {
		return super.getPassengerRidingPosition(entity).add(0, -0.35F, 0);
	}

	@Override
	public SoundEvent getAmbientSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("op_mobs_op_tools:zombiegrowl"));
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.zombie_villager.step")), 0.15f, 1);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.elder_guardian.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("op_mobs_op_tools:infernodeath"));
	}


	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
		if (this.isMinion)
			return false;
		boolean hit = super.doHurtTarget(serverLevel, entity);
		if (hit && entity instanceof LivingEntity livingEntity && this.getRandom().nextFloat() < 0.3f) {
			MobEffectInstance current = livingEntity.getEffect(MobEffects.HUNGER);
			if (current == null || current.getDuration() < 100 || current.getAmplifier() < 0)
				livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
		}
		return hit;
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
		if (this.isSpinning || this.isMinion)
			return false;
		return super.causeFallDamage(fallDistance, multiplier, source);
	}

	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHitIn) {
		if (this.isMinion)
			return;
		super.dropCustomDeathLoot(serverLevel, source, recentlyHitIn);
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public void customServerAiStep(ServerLevel serverLevel) {
		super.customServerAiStep(serverLevel);
		if (!this.isMinion)
			this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());

		if (this.isMinion) {
			if (this.minionLifeTicks > 0)
				this.minionLifeTicks--;
			if (this.minionLifeTicks <= 0)
				this.discard();
			return;
		}

		if (this.summonCooldown > 0)
			this.summonCooldown--;
		if (this.plagueCooldown > 0)
			this.plagueCooldown--;

		if (!this.summonedMinionLifetimes.isEmpty()) {
			this.summonedMinionLifetimes.replaceAll((uuid, ticks) -> ticks - 1);
			this.summonedMinionLifetimes.entrySet().removeIf(entry -> {
				if (entry.getValue() > 0)
					return false;
				Entity entity = serverLevel.getEntity(entry.getKey());
				if (entity instanceof Zombie zombie)
					zombie.discard();
				return true;
			});
		}

		if (this.summonCooldown == 0) {
			this.spawnBabyMinions(serverLevel);
			this.summonCooldown = 400;
		}

		if (this.plagueCooldown == 0 && !this.isSpinning) {
			this.isSpinning = true;
			this.spinningTicks = 100;
			this.plagueCooldown = 300;
		}

		if (this.isSpinning) {
			this.getNavigation().stop();
			this.setDeltaMovement(0.0, 0.25, 0.0);
			float nextYaw = this.getYRot() + 32.0f;
			this.setYRot(nextYaw);
			this.setYBodyRot(nextYaw);
			this.setYHeadRot(nextYaw);
			serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 10, 0.6, 0.3, 0.6, 0.01);

			if (this.spinningTicks % 10 == 0) {
				Vec3 forward = this.getLookAngle().multiply(1.0, 0.0, 1.0);
				if (forward.lengthSqr() < 1.0E-4)
					forward = new Vec3(1, 0, 0);
				forward = forward.normalize();
				Vec3 left = new Vec3(-forward.z, 0, forward.x);
				this.spawnPlaguePotion(serverLevel, forward);
				this.spawnPlaguePotion(serverLevel, forward.scale(-1));
				this.spawnPlaguePotion(serverLevel, left);
				this.spawnPlaguePotion(serverLevel, left.scale(-1));
				this.spawnPlaguePotion(serverLevel, new Vec3(0, 1, 0));
				this.spawnPlaguePotion(serverLevel, new Vec3(0, -1, 0));
			}

			this.spinningTicks--;
			if (this.spinningTicks <= 0) {
				this.isSpinning = false;
				this.spinningTicks = 0;
				this.setDeltaMovement(Vec3.ZERO);
			}
		}
	}

	private void spawnBabyMinions(ServerLevel serverLevel) {
		int count = 5 + this.getRandom().nextInt(6);
		for (int i = 0; i < count; i++) {
			Zombie zombie = EntityType.ZOMBIE.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
			if (zombie == null)
				continue;
			double x = this.getX() + this.getRandom().nextDouble() * 8.0 - 4.0;
			double z = this.getZ() + this.getRandom().nextDouble() * 8.0 - 4.0;
			zombie.setPos(x, this.getY(), z);
			zombie.setBaby(true);
			zombie.setPersistenceRequired();
			serverLevel.addFreshEntity(zombie);
			this.summonedMinionLifetimes.put(zombie.getUUID(), 400);
		}
	}

	private void spawnPlaguePotion(ServerLevel serverLevel, Vec3 direction) {
		ThrownPotion potion = new ThrownPotion(serverLevel, this);
		ItemStack lingering = new ItemStack(Items.LINGERING_POTION);
		lingering.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
				new net.minecraft.world.item.alchemy.PotionContents(Potions.POISON).withEffectAdded(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0)));
		potion.setItem(lingering);
		potion.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
		potion.setDeltaMovement(direction.normalize().scale(0.5));
		serverLevel.addFreshEntity(potion);
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return super.getDefaultDimensions(pose).scale(1.5f);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.35);
		builder = builder.add(Attributes.MAX_HEALTH, 170);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 1.2);
		return builder;
	}
}