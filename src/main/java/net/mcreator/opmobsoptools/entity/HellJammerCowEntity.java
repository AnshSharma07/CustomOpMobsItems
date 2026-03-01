package net.mcreator.opmobsoptools.entity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HellJammerCowEntity extends Monster {
	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PINK, ServerBossEvent.BossBarOverlay.PROGRESS);
	private int specialCooldown;
	private boolean isLeaping;
	private int leapTicks;

	public HellJammerCowEntity(EntityType<HellJammerCowEntity> type, Level world) {
		super(type, world);
		xpReward = 40;
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
	public SoundEvent getAmbientSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("block.sculk_shrieker.shriek"));
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.cow.step")), 0.15f, 1);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.warden.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("op_mobs_op_tools:infernodeath"));
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
		boolean hit = super.doHurtTarget(serverLevel, entity);
		if (hit && entity instanceof LivingEntity livingEntity) {
			double deltaX = livingEntity.getX() - this.getX();
			double deltaZ = livingEntity.getZ() - this.getZ();
			double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
			if (horizontal > 1.0E-4) {
				double strength = 0.65;
				livingEntity.push(deltaX / horizontal * strength, 0.25, deltaZ / horizontal * strength);
				livingEntity.hurtMarked = true;
			}
		}
		return hit;
	}

	public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
		return false;
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
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());

		if (this.specialCooldown > 0)
			this.specialCooldown--;

		if (this.specialCooldown == 0 && !this.isLeaping) {
			this.isLeaping = true;
			this.leapTicks = 130;
			this.specialCooldown = 400;
			this.getNavigation().stop();
			this.setDeltaMovement(this.getDeltaMovement().x, 1.35, this.getDeltaMovement().z);
		}

		if (this.isLeaping) {
			this.getNavigation().stop();
			float nextYaw = this.getYRot() + 28.0f;
			this.setYRot(nextYaw);
			this.setYBodyRot(nextYaw);
			this.setYHeadRot(nextYaw);
			serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 8, 0.5, 0.2, 0.5, 0.01);

			if (this.leapTicks <= 115 && this.leapTicks > 100) {
				this.setDeltaMovement(Vec3.ZERO);
			} else if (this.leapTicks <= 100) {
				this.setDeltaMovement(this.getDeltaMovement().x * 0.1, -1.25, this.getDeltaMovement().z * 0.1);
			}

			this.leapTicks--;
			if ((this.leapTicks <= 100 && this.onGround()) || this.leapTicks <= 0) {
				this.performLeapSlam(serverLevel);
			}
		}
	}

	private void performLeapSlam(ServerLevel serverLevel) {
		double attackDamage = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float slamDamage = (float) (attackDamage * 0.75);
		AABB hitBox = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
		for (LivingEntity livingEntity : serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != null && entity.isAlive() && entity != this)) {
			livingEntity.hurtServer(serverLevel, this.damageSources().mobAttack(this), slamDamage);
			double deltaX = livingEntity.getX() - this.getX();
			double deltaZ = livingEntity.getZ() - this.getZ();
			double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
			if (horizontal > 1.0E-4) {
				double strength = 1.1;
				livingEntity.push(deltaX / horizontal * strength, 0.45, deltaZ / horizontal * strength);
				livingEntity.hurtMarked = true;
			}
		}

		BlockPos center = this.blockPosition();
		for (int x = -3; x <= 3; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -3; z <= 3; z++) {
					if (x * x + z * z > 9)
						continue;
					BlockPos breakPos = center.offset(x, y, z);
					BlockState state = serverLevel.getBlockState(breakPos);
					if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.OBSIDIAN))
						continue;
					float hardness = state.getDestroySpeed(serverLevel, breakPos);
					if (hardness >= 0.0f && hardness < 10.0f) {
						serverLevel.destroyBlock(breakPos, true, this);
					}
				}
			}
		}

		serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(0.1), this.getZ(), 80, 2.5, 0.4, 2.5, 0.04);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.1), this.getZ(), 50, 2.0, 0.3, 2.0, 0.02);
		this.isLeaping = false;
		this.leapTicks = 0;
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return super.getDefaultDimensions(pose).scale(1.5f);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.4);
		builder = builder.add(Attributes.MAX_HEALTH, 150);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
		return builder;
	}
}
