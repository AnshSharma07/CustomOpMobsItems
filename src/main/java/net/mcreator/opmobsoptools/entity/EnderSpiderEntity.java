package net.mcreator.opmobsoptools.entity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import net.mcreator.opmobsoptools.init.OpMobsOpToolsModEntities;

public class EnderSpiderEntity extends Monster {
	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PINK, ServerBossEvent.BossBarOverlay.PROGRESS);
	private int teleportCooldown;
	private int cloneCooldown;
	private int webCooldown;
	private boolean phaseTriggered;
	private boolean isClone;

	public EnderSpiderEntity(EntityType<EnderSpiderEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
		setPersistenceRequired();
		refreshDimensions();
		this.teleportCooldown = 100 + this.getRandom().nextInt(61);
		this.cloneCooldown = 300;
		this.webCooldown = 160;
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
		return this.isClone;
	}

	@Override
	public SoundEvent getAmbientSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.parrot.imitate.spider"));
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.warden.step")), 0.15f, 1);
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
		if (this.isClone)
			return false;
		return super.doHurtTarget(serverLevel, entity);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource damagesource, float amount) {
		if (this.isClone) {
			this.discard();
			return false;
		}
		return super.hurtServer(level, damagesource, amount);
	}

	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHitIn) {
		if (this.isClone)
			return;
		super.dropCustomDeathLoot(serverLevel, source, recentlyHitIn);
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (!this.isClone)
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
		if (!this.isClone)
			this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());

		if (this.isClone) {
			if (this.cloneCooldown > 0)
				this.cloneCooldown--;
			if (this.cloneCooldown <= 0)
				this.discard();
			return;
		}

		if (this.teleportCooldown > 0)
			this.teleportCooldown--;
		if (this.cloneCooldown > 0)
			this.cloneCooldown--;
		if (this.webCooldown > 0)
			this.webCooldown--;

		LivingEntity target = this.getTarget();
		if (target != null && target.isAlive() && this.teleportCooldown == 0) {
			this.tryRandomTeleportAroundTarget(serverLevel, target);
			this.teleportCooldown = 100 + this.getRandom().nextInt(61);
		}

		if (this.cloneCooldown == 0) {
			this.spawnIllusionClones(serverLevel);
			this.cloneCooldown = 300;
		}

		if (this.webCooldown == 0) {
			this.placeWebs(serverLevel);
			this.webCooldown = 160;
		}

		if (!this.phaseTriggered && this.getHealth() <= this.getMaxHealth() * 0.5f) {
			this.phaseTriggered = true;
			this.summonPhaseTwoMinions(serverLevel);
		}
	}

	private void spawnIllusionClones(ServerLevel serverLevel) {
		int cloneCount = 2 + this.getRandom().nextInt(2);
		for (int i = 0; i < cloneCount; i++) {
			EnderSpiderEntity clone = new EnderSpiderEntity(OpMobsOpToolsModEntities.ENDER_SPIDER, serverLevel);
			Vec3 offset = new Vec3((this.getRandom().nextDouble() - 0.5) * 4.0, 0, (this.getRandom().nextDouble() - 0.5) * 4.0);
			BlockPos clonePos = this.blockPosition().offset((int) Math.round(offset.x), 0, (int) Math.round(offset.z));
			BlockPos safePos = this.findSafeStandPos(serverLevel, clonePos);
			if (safePos == null)
				safePos = this.blockPosition();
			clone.moveTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, this.getYRot(), this.getXRot());
			clone.isClone = true;
			clone.phaseTriggered = true;
			clone.cloneCooldown = 200;
			clone.teleportCooldown = Integer.MAX_VALUE;
			clone.webCooldown = Integer.MAX_VALUE;
			clone.setHealth(1.0f);
			serverLevel.addFreshEntity(clone);
		}
	}

	private void tryRandomTeleportAroundTarget(ServerLevel serverLevel, LivingEntity target) {
		for (int attempts = 0; attempts < 12; attempts++) {
			double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
			double radius = 6.0 + this.getRandom().nextDouble() * 4.0;
			int targetX = (int) Math.floor(target.getX() + Math.cos(angle) * radius);
			int targetZ = (int) Math.floor(target.getZ() + Math.sin(angle) * radius);
			BlockPos safePos = this.findSafeStandPos(serverLevel, new BlockPos(targetX, (int) target.getY(), targetZ));
			if (safePos != null) {
				this.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
				serverLevel.broadcastEntityEvent(this, (byte) 46);
				return;
			}
		}
	}

	private BlockPos findSafeStandPos(ServerLevel serverLevel, BlockPos around) {
		int minY = serverLevel.getMinBuildHeight();
		int maxY = serverLevel.getMaxBuildHeight() - 2;
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

	private void placeWebs(ServerLevel serverLevel) {
		int placed = 0;
		for (int attempts = 0; attempts < 12 && placed < 3; attempts++) {
			int x = this.blockPosition().getX() + this.getRandom().nextInt(7) - 3;
			int y = this.blockPosition().getY();
			int z = this.blockPosition().getZ() + this.getRandom().nextInt(7) - 3;
			BlockPos placePos = new BlockPos(x, y, z);
			if (serverLevel.isEmptyBlock(placePos) && serverLevel.getBlockState(placePos.below()).isSolid()) {
				serverLevel.setBlock(placePos, Blocks.COBWEB.defaultBlockState(), 3);
				placed++;
			}
		}
	}

	private void summonPhaseTwoMinions(ServerLevel serverLevel) {
		for (int i = 0; i < 4; i++) {
			CaveSpider caveSpider = EntityType.CAVE_SPIDER.create(serverLevel);
			if (caveSpider != null) {
				caveSpider.moveTo(this.getX() + this.getRandom().nextDouble() * 4.0 - 2.0, this.getY(), this.getZ() + this.getRandom().nextDouble() * 4.0 - 2.0, this.getYRot(), 0.0f);
				serverLevel.addFreshEntity(caveSpider);
			}
			Spider spider = EntityType.SPIDER.create(serverLevel);
			if (spider != null) {
				spider.moveTo(this.getX() + this.getRandom().nextDouble() * 4.0 - 2.0, this.getY(), this.getZ() + this.getRandom().nextDouble() * 4.0 - 2.0, this.getYRot(), 0.0f);
				serverLevel.addFreshEntity(spider);
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
		builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
		return builder;
	}
}
