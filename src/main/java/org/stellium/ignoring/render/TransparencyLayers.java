package org.stellium.ignoring.render;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.entity.EntityCaptures;
import org.stellium.ignoring.mixin.accessor.RenderLayerAccessor;
import org.stellium.ignoring.mixin.accessor.RenderSetupAccessor;
import org.stellium.ignoring.mixin.accessor.RenderSetupTextureSpecAccessor;

import java.util.Map;
import java.util.function.Supplier;


/*
 * This file is part of Transparent-Entities(https://github.com/LopyMine/Transparent-Entities)
 * Copyright (C) LopyMine(https://github.com/LopyMine)
 *
 * Modified by stellium1 in Ignoring(https://github.com/stellium1/Ignoring).
 * Licensed under the GNU Lesser General Public License v3.0
*/
public class TransparencyLayers {

	static {
		IgnoringConfig.init();
	}

    public static RenderType getArmorLayer(boolean cull, Identifier texture, Supplier<RenderType> original) {
        if (canReplaceRenderLayer()) {
            if (cull) {
                return RenderTypes.entityTranslucentCullItemTarget(texture);
            } else {
                return RenderTypes.entityTranslucent(texture);
            }
        }
        return original.get();
    }

	public static RenderType getLayer(Identifier texture, Supplier<RenderType> original) {
		if (canReplaceRenderLayer()) {
			return RenderTypes.entityTranslucentCullItemTarget(texture);
		}
		return original.get();
	}

	public static RenderType getItemLayer(RenderType original) {
		if (!canReplaceRenderLayer()) {
			return original;
		}

		Identifier texture = getTextureLocation(original);
		if (TextureAtlas.LOCATION_BLOCKS.equals(texture)) {
			return Sheets.translucentBlockItemSheet();
		}

		return Sheets.translucentItemSheet();
	}

	    public static RenderType getItemLayer(Supplier<RenderType> original) {
	        return getItemLayer(original.get());
	    }

    private static Identifier getTextureLocation(RenderType layer) {
        try {
            Map<String, Object> textures = ((RenderSetupAccessor) (Object) ((RenderLayerAccessor) (Object) layer).ignoring$getRenderSetup()).ignoring$getTextures();
            if (textures == null || textures.isEmpty()) {
                return null;
            }
            Object textureSpec = textures.values().iterator().next();
            return ((RenderSetupTextureSpecAccessor) textureSpec).ignoring$getLocation();
        } catch (Exception ignored) {
            return null;
        }
    }

	private static boolean canReplaceRenderLayer() {
		IgnoringConfig config;
		try {
			config = IgnoringConfig.get();
		} catch (Exception e) {
			return false;
		}
		config = IgnoringConfig.get();
		if (config == null) {
			return false;
		}
		if (!config.ignoreRender) {
			return false;
		}
		Entity entity = EntityCaptures.MAIN.getEntity();
		if (entity == null) {
			return false;
		}
		return config.shouldIgnorePlayer(entity);
	}

}
