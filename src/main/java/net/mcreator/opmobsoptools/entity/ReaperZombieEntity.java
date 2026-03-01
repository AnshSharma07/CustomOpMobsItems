package net.mcreator.opmobsoptools.entity;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
	private int screamCooldown = 360;
	private int summonCooldown = 400;
	private int teleportCooldown = 0;
	private boolean phaseTwoTriggered;
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
		boolean hit = super.doHurtTarget(serverLevel, entity);
		if (hit && entity instanceof LivingEntity livingEntity && this.getRandom().nextFloat() < 0.3f) {
			MobEffectInstance current = livingEntity.getEffect(MobEffects.HUNGER);
			if (current == null || current.getDuration() < 100 || current.getAmplifier() < 0)
				livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
		}
		return hit;
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

		if (this.screamCooldown > 0)
			this.screamCooldown--;
		if (this.summonCooldown > 0)
			this.summonCooldown--;
		if (this.teleportCooldown > 0)
			this.teleportCooldown--;

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

		if (this.screamCooldown == 0) {
			this.runInfectionScream(serverLevel);
			this.screamCooldown = 360;
		}

		if (this.summonCooldown == 0) {
			this.spawnBabyMinions(serverLevel);
			this.summonCooldown = 400;
		}

		if (!this.phaseTwoTriggered && this.getHealth() <= this.getMaxHealth() * 0.2f)
			this.phaseTwoTriggered = true;

		if (this.phaseTwoTriggered && this.teleportCooldown == 0) {
			this.tryPhaseTwoTeleport(serverLevel);
			this.teleportCooldown = 80 + this.getRandom().nextInt(41);
		}
	}

	private void runInfectionScream(ServerLevel serverLevel) {
		this.playSound(BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.zombie.ambient")), 2.2f, 0.7f);
		for (int ring = 1; ring <= 4; ring++) {
			double radius = ring * 1.5;
			for (int i = 0; i < 18; i++) {
				double angle = (Math.PI * 2.0 * i) / 18.0;
				double x = this.getX() + Math.cos(angle) * radius;
				double z = this.getZ() + Math.sin(angle) * radius;
				serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, this.getY(0.4), z, 1, 0.02, 0.02, 0.02, 0.0);
			}
		}

		AABB area = this.getBoundingBox().inflate(6.0);
		for (LivingEntity livingEntity : serverLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != null && e.isAlive() && e != this)) {
			livingEntity.hurtServer(serverLevel, this.damageSources().mobAttack(this), 6.0f);
			livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
			livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
		}
	}

	private void spawnBabyMinions(ServerLevel serverLevel) {
		int count = 3 + this.getRandom().nextInt(3);
		for (int i = 0; i < count; i++) {
			Zombie zombie = EntityType.ZOMBIE.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
			if (zombie == null)
				continue;
			double x = this.getX() + this.getRandom().nextDouble() * 8.0 - 4.0;
			double z = this.getZ() + this.getRandom().nextDouble() * 8.0 - 4.0;
			BlockPos spawnPos = this.findSafeStandPos(serverLevel, new BlockPos((int) Math.floor(x), (int) Math.floor(this.getY()), (int) Math.floor(z)));
			if (spawnPos == null)
				spawnPos = this.blockPosition();
			zombie.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
			zombie.setBaby(true);
			serverLevel.addFreshEntity(zombie);
			this.summonedMinionLifetimes.put(zombie.getUUID(), 400);
		}
	}

	private void tryPhaseTwoTeleport(ServerLevel serverLevel) {
		for (int attempts = 0; attempts < 12; attempts++) {
			double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
			double radius = 6.0 + this.getRandom().nextDouble() * 4.0;
			int targetX = (int) Math.floor(this.getX() + Math.cos(angle) * radius);
			int targetZ = (int) Math.floor(this.getZ() + Math.sin(angle) * radius);
			BlockPos safePos = this.findSafeStandPos(serverLevel, new BlockPos(targetX, (int) this.getY(), targetZ));
			if (safePos != null) {
				this.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
				serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 24, 0.7, 0.2, 0.7, 0.01);
				return;
			}
		}
	}

	private BlockPos findSafeStandPos(ServerLevel serverLevel, BlockPos around) {
		int minY = serverLevel.getMinY();
		int maxY = serverLevel.getMaxY() - 2;
		int y = Math.max(minY + 1, Math.min(maxY, around.getY()));
		for (int scan = 0; scan < 12; scan++) {
			int checkY = Math.max(minY + 1, y - scan);
			BlockPos feetPos = new BlockPos(around.getX(), checkY, around.getZ());
			BlockPos belowPos = feetPos.below();
			if (serverLevel.getBlockState(belowPos).isSolid() && serverLevel.isEmptyBlock(feetPos) && serverLevel.isEmptyBlock(feetPos.above())
					&& serverLevel.noCollision(this, this.getBoundingBox().move(feetPos.getX() + 0.5 - this.getX(), feetPos.getY() - this.getY(), feetPos.getZ() + 0.5 - this.getZ()))) {
				return feetPos;
			}
		}
		return null;
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
