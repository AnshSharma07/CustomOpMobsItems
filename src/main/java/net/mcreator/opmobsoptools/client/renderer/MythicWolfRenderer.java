package net.mcreator.opmobsoptools.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.animation.KeyframeAnimation;

import net.mcreator.opmobsoptools.entity.MythicWolfEntity;
import net.mcreator.opmobsoptools.client.model.animations.wolfnewAnimation;
import net.mcreator.opmobsoptools.client.model.animations.wolffAnimation;
import net.mcreator.opmobsoptools.client.model.Modelwolfnew;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

@Environment(EnvType.CLIENT)
public class MythicWolfRenderer extends MobRenderer<MythicWolfEntity, LivingEntityRenderState, Modelwolfnew> {
	private MythicWolfEntity entity = null;

	public MythicWolfRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(Modelwolfnew.LAYER_LOCATION)), 0.5f);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public void extractRenderState(MythicWolfEntity entity, LivingEntityRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		this.entity = entity;
		if (this.model instanceof AnimatedModel) {
			((AnimatedModel) this.model).setEntity(entity);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
		return ResourceLocation.parse("op_mobs_op_tools:textures/entities/wolfie.png");
	}

	@Override
	protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
		poseStack.scale(2f, 2f, 2f);
	}

	private static final class AnimatedModel extends Modelwolfnew {
		private MythicWolfEntity entity = null;
		private final Supplier<KeyframeAnimation> keyframeAnimation0;
		private final Supplier<KeyframeAnimation> keyframeAnimation1;

		public AnimatedModel(ModelPart root) {
			super(root);
			this.keyframeAnimation0 = () -> wolffAnimation.idle.bake(root);
			this.keyframeAnimation1 = () -> wolfnewAnimation.walk.bake(root);
		}

		public void setEntity(MythicWolfEntity entity) {
			this.entity = entity;
		}

		@Override
		public void setupAnim(LivingEntityRenderState state) {
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.keyframeAnimation0.get().apply(entity.animationState0, state.ageInTicks, 1f);
			this.keyframeAnimation1.get().apply(entity.animationState1, state.ageInTicks, 1f);
			super.setupAnim(state);
		}
	}
}