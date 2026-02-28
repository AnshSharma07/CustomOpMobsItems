package net.mcreator.opmobsoptools.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.SpiderModel;

import net.mcreator.opmobsoptools.entity.EnderSpiderEntity;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

import com.mojang.blaze3d.vertex.PoseStack;

@Environment(EnvType.CLIENT)
public class EnderSpiderRenderer extends MobRenderer<EnderSpiderEntity, LivingEntityRenderState, SpiderModel> {
	private EnderSpiderEntity entity = null;

	public EnderSpiderRenderer(EntityRendererProvider.Context context) {
		super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.5f);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public void extractRenderState(EnderSpiderEntity entity, LivingEntityRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		this.entity = entity;
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
		return ResourceLocation.parse("op_mobs_op_tools:textures/entities/ender-spider.png");
	}

	@Override
	protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
		poseStack.scale(2f, 2f, 2f);
	}
}