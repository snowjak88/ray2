package org.snowjak.rays.light;

import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for building {@link PointLight} instances.
 * 
 * @author snowjak88
 *
 */
@HasName("point-light")
@Deprecated
public class PointLightBuilder extends LightBuilder<PointLight> {

	/**
	 * @return a new PointLightBuilder instance
	 */
	public static PointLightBuilder builder() {

		return new PointLightBuilder();
	}

	protected PointLightBuilder() {

	}

	@Override
	protected PointLight createNewLightInstance() {

		return new PointLight();
	}

	@Override
	protected PointLight performTypeSpecificInitialization(PointLight newLightInstance) {

		return newLightInstance;
	}

}
