package org.snowjak.rays.light;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.world.HasName;

/**
 * A convenient interface for building {@link DirectionalLight} instances.
 * 
 * @author snowjak88
 *
 */
@HasName("directional-light")
public class DirectionalLightBuilder extends LightBuilder<DirectionalLight> {

	private Vector3D direction = DirectionalLight.DEFAULT_DIRECTION;

	/**
	 * @return a new DirectionalLightBuilder instance
	 */
	public static DirectionalLightBuilder builder() {

		return new DirectionalLightBuilder();
	}

	protected DirectionalLightBuilder() {

	}

	/**
	 * Configure this in-progress {@link DirectionalLight} to point in the given
	 * direction.
	 * 
	 * @param direction
	 * @return this Builder, for method-chaining
	 */
	@HasName("direction")
	public DirectionalLightBuilder direction(Vector3D direction) {

		this.direction = direction.normalize();
		return this;
	}

	@Override
	protected DirectionalLight createNewLightInstance() {

		return new DirectionalLight();
	}

	@Override
	protected DirectionalLight performTypeSpecificInitialization(DirectionalLight newLightInstance) {

		newLightInstance.setDirection(direction);

		return newLightInstance;
	}

}
