package org.snowjak.rays.shape;

/**
 * A convenient interface for building {@link Cube} instances.
 * 
 * @author snowjak88
 *
 */
public class CubeBuilder extends ShapeBuilder<Cube> {

	/**
	 * @return a new CubeBuilder instance
	 */
	public static CubeBuilder builder() {

		return new CubeBuilder();
	}

	protected CubeBuilder() {

	}

	@Override
	protected Cube createNewShapeInstance() {

		return new Cube();
	}

	@Override
	protected Cube performTypeSpecificInitialization(Cube newShapeInstance) {

		return newShapeInstance;
	}

}
