package org.snowjak.rays.shape;

import org.snowjak.rays.material.Material;
import org.snowjak.rays.world.HasName;

/**
 * Provides a convenient interface for building {@link Plane}s.
 * 
 * @author snowjak88
 *
 */
@HasName("plane")
public class PlaneBuilder extends ShapeBuilder<Plane> {

	private Material minusMaterial = Shape.DEFAULT_MATERIAL, plusMaterial = Shape.DEFAULT_MATERIAL;

	/**
	 * @return a new PlaneBuilder instance
	 */
	public static PlaneBuilder builder() {

		return new PlaneBuilder();
	}

	protected PlaneBuilder() {

	}

	@Override
	protected Plane createNewShapeInstance() {

		return new Plane();
	}

	/**
	 * Set's this {@link Plane}'s "minus material" -- the {@link Material} that
	 * lies on the (local) Y- side of this plane.
	 * 
	 * @param minusMaterial
	 * @return this PlaneBuilder, for method-chaining
	 */
	@HasName("minus-material")
	public PlaneBuilder minusMaterial(Material minusMaterial) {

		this.minusMaterial = minusMaterial;
		return this;
	}

	/**
	 * Set's this {@link Plane}'s "plus material" -- the {@link Material} that
	 * lies on the (local) Y+ side of this plane.
	 * 
	 * @param plusMaterial
	 * @return this PlaneBuilder, for method-chaining
	 */
	@HasName("plus-material")
	public PlaneBuilder plusMaterial(Material plusMaterial) {

		this.plusMaterial = plusMaterial;
		return this;
	}

	@Override
	protected Plane performTypeSpecificInitialization(Plane newShapeInstance) {

		newShapeInstance.setMinusMaterial(minusMaterial);
		newShapeInstance.setPlusMaterial(plusMaterial);

		return newShapeInstance;
	}

}
