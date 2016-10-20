package org.snowjak.rays.light;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for building {@link DirectionalLight} instances.
 * 
 * @author snowjak88
 *
 */
@HasName("directional-light")
public class DirectionalLightBuilder implements Builder<DirectionalLight> {

	private Vector3D direction = DirectionalLight.DEFAULT_DIRECTION;

	private RawColor radiance = DirectionalLight.DEFAULT_RADIANCE;

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

	/**
	 * Configure this in-progress {@link DirectionalLight} to afford the given
	 * radiance.
	 * 
	 * @param radiance
	 * @return this Builder, for method-chaining
	 */
	@HasName("radiance")
	public DirectionalLightBuilder radiance(RawColor radiance) {

		this.radiance = radiance;
		return this;
	}

	@Override
	public DirectionalLight build() {

		DirectionalLight light = new DirectionalLight();

		light.setDirection(direction);
		light.setRadiance(radiance);

		return light;
	}

}
