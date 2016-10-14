package org.snowjak.rays.light;

/**
 * A convenient interface for building {@link PointLight} instances.
 * 
 * @author snowjak88
 *
 */
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
