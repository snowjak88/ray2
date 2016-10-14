package org.snowjak.rays.shape;

import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.transform.Transformer;

/**
 * Base class for all shape Builders.
 * 
 * @author snowjak88
 *
 * @param <T>
 *            the Shape subtype to build
 */
public abstract class ShapeBuilder<T extends Shape> implements Builder<Shape> {

	private ColorScheme diffuseColorScheme = Shape.DEFAULT_COLOR_SCHEME,
			emissiveColorScheme = Shape.DEFAULT_EMISSIVE_COLOR_SCHEME;

	private Material material = Material.AIR;

	private List<Transformer> transformers = new LinkedList<>();

	/**
	 * A utility method: create a new, blank instance of type T and return it --
	 * for example,
	 * 
	 * <pre>
	 * 
	 * protected Sphere createNewShapeInstance() {
	 * 
	 * 	return new Sphere();
	 * }
	 * </pre>
	 * 
	 * @return a new instance of type T
	 */
	protected abstract T createNewShapeInstance();

	/**
	 * Add a diffuse {@link ColorScheme} to this in-progress Shape
	 * 
	 * @param diffuseColor
	 * @return this ShapeBuilder, for method chaining
	 */
	public ShapeBuilder<T> diffuse(ColorScheme diffuseColor) {

		this.diffuseColorScheme = diffuseColor;
		return this;
	}

	/**
	 * Add an emissive {@link ColorScheme} to this in-progress Shape
	 * 
	 * @param emissiveColor
	 * @return this ShapeBuilder, for method chaining
	 */
	public ShapeBuilder<T> emissive(ColorScheme emissiveColor) {

		this.emissiveColorScheme = emissiveColor;
		return this;
	}

	/**
	 * Add a {@link Material} to this in-progress Shape
	 * 
	 * @param material
	 * @return this ShapeBuilder, for method chaining
	 */
	public ShapeBuilder<T> material(Material material) {

		this.material = material;
		return this;
	}

	/**
	 * Finalize this in-progress Shape and return it.
	 */
	@Override
	public T build() {

		T newShape = createNewShapeInstance();

		newShape.setDiffuseColorScheme(diffuseColorScheme);
		newShape.setEmissiveColorScheme(emissiveColorScheme);
		newShape.setMaterial(material);
		newShape.getTransformers().addAll(transformers);

		newShape = performTypeSpecificInitialization(newShape);

		return newShape;
	}

	/**
	 * This method is automatically called by {@link #build()}. Use this to
	 * implement any type-specific initialization -- e.g., a {@link Sphere}'s
	 * radius.
	 * 
	 * @param newShapeInstance
	 *            the in-progress Shape subtype
	 * @return the in-progress Shape subtype after type-specific initialization
	 */
	protected abstract T performTypeSpecificInitialization(T newShapeInstance);

}
