package org.snowjak.rays.shape;

/**
 * Provides a convenient interface for building {@link Sphere}s.
 * 
 * @author snowjak88
 *
 */
public class SphereBuilder extends ShapeBuilder<Sphere> {

	/**
	 * @return a new SphereBuilder instance
	 */
	public static SphereBuilder builder() {

		return new SphereBuilder();
	}

	protected SphereBuilder() {

	}

	@Override
	protected Sphere createNewShapeInstance() {

		return new Sphere();
	}

	@Override
	protected Sphere performTypeSpecificInitialization(Sphere newShapeInstance) {

		return newShapeInstance;
	}

}
