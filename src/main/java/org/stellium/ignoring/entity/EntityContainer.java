package org.stellium.ignoring.entity;

import lombok.*;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class EntityContainer {

	@Nullable
	private Entity entity;
	private boolean enabled;

	public EntityContainer() {
		this.entity = null;
		this.enabled = true;
	}

	public void setEntity(@Nullable Entity entity) {
		if (!this.isEnabled()) {
			return;
		}
		this.entity = entity;
	}

	@Nullable
	public Entity getEntity() {
		return this.isEnabled() ? this.entity : null;
	}
}
