package org.snowjak.rays.shape;

/**
 * A convenient interface for building {@link Cylinder} instances
 * 
 * @author snowjak88
 *
 */
public class CylinderBuilder extends ShapeBuilder<Cylinder> {

	/**
	 * @return a new CylinderBuilder instance
	 */
	public static CylinderBuilder builder() {

		return new CylinderBuilder();
	}

	protected CylinderBuilder() {

	}

	@Override
	protected Cylinder createNewShapeInstance() {

		return new Cylinder();
	}

	@Override
	protected Cylinder performTypeSpecificInitialization(Cylinder newShapeInstance) {

		return newShapeInstance;
	}

}
