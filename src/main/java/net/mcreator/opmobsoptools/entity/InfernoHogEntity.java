package net.mcreator.opmobsoptools.entity;

import net.mcreator.opmobsoptools.init.OpMobsOpToolsModItems;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

public class InfernoHogEntity extends Monster {
	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PINK, ServerBossEvent.BossBarOverlay.PROGRESS);
	private int fireballCooldown;
	private int specialCooldown;
	private int spinningTicks;
	private boolean isSpinning;

	public InfernoHogEntity(EntityType<InfernoHogEntity> type, Level world) {
		super(type, world);
		xpReward = 150;
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

	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHitIn) {
		super.dropCustomDeathLoot(serverLevel, source, recentlyHitIn);
		this.spawnAtLocation(serverLevel, new ItemStack(OpMobsOpToolsModItems.BLAZING_TUSK_BLADE_TOOL));
	}

	@Override
	public SoundEvent getAmbientSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.pig.ambient"));
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.pig.step")), 0.15f, 1);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.pig.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("op_mobs_op_tools:infernodeath"));
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource damagesource, float amount) {
		if (damagesource.is(DamageTypes.IN_FIRE))
			return false;
		if (damagesource.is(DamageTypes.ON_FIRE))
			return false;
		if (damagesource.is(DamageTypes.LAVA))
			return false;
		if (damagesource.is(DamageTypes.FALL))
			return false;
		if (damagesource.is(DamageTypes.EXPLOSION) || damagesource.is(DamageTypes.PLAYER_EXPLOSION))
			return false;
		return super.hurtServer(level, damagesource, amount);
	}

	@Override
	public boolean fireImmune() {
		return true;
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

		if (this.fireballCooldown > 0)
			this.fireballCooldown--;
		if (this.specialCooldown > 0)
			this.specialCooldown--;

		LivingEntity target = this.getTarget();
		if (target != null && target.isAlive() && this.fireballCooldown == 0) {
			Vec3 direction = target.getEyePosition().subtract(this.getX(), this.getEyeY(), this.getZ()).normalize();
			SmallFireball smallFireball = new SmallFireball(serverLevel, this, direction);
			smallFireball.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
			serverLevel.addFreshEntity(smallFireball);
			target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 100));
			this.fireballCooldown = 300;
		}

		if (this.specialCooldown == 0 && !this.isSpinning) {
			this.isSpinning = true;
			this.spinningTicks = 100;
			this.specialCooldown = 400;
		}

		if (this.isSpinning) {
			this.setDeltaMovement(Vec3.ZERO);
			this.getNavigation().stop();
			float nextYaw = this.getYRot() + 35.0f;
			this.setYRot(nextYaw);
			this.setYBodyRot(nextYaw);
			this.setYHeadRot(nextYaw);
			serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 12, 0.8, 0.2, 0.8, 0.01);

			this.spinningTicks--;
			if (this.spinningTicks <= 0) {
				AABB blastArea = this.getBoundingBox().inflate(8.0);
				for (LivingEntity livingEntity : serverLevel.getEntitiesOfClass(LivingEntity.class, blastArea,
						entity -> entity != null && entity.isAlive() && entity != this)) {
					livingEntity.hurtServer(serverLevel, this.damageSources().explosion(this, this), 16.0f);
				}

				serverLevel.explode(this, this.getX(), this.getY(0.5), this.getZ(), 8.0f, Level.ExplosionInteraction.NONE);
				serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 80, 1.8, 0.7, 1.8, 0.02);
				this.isSpinning = false;
				this.spinningTicks = 0;
			}
		}
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return super.getDefaultDimensions(pose).scale(2f);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.4);
		builder = builder.add(Attributes.MAX_HEALTH, 150);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 1.2);
		return builder;
	}
}
