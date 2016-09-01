package org.snowjak.rays.shape;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Locatable;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.HasColorScheme;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;

import javafx.scene.paint.Color;

/**
 * Represents an "object" in a 3D space. Something susceptible of being placed,
 * transformed, and intersected with {@link Ray}s.
 * 
 * @author rr247200
 *
 */
public abstract class Shape implements Transformable, Locatable, Intersectable, HasColorScheme {

	private Deque<Transformer> transformers = new LinkedList<>();

	private ColorScheme ambientColorScheme, diffuseColorScheme, specularColorScheme, emissiveColorScheme;

	private double shininess, reflectivity;

	/**
	 * Create a new Shape.
	 */
	public Shape() {
		this(new SimpleColorScheme(Color.HOTPINK));
	}

	/**
	 * Create a new Shape, setting both the ambient and diffuse color-schemes to
	 * use the same {@link ColorScheme}.
	 * 
	 * @param colorScheme
	 */
	public Shape(ColorScheme colorScheme) {
		this(colorScheme, colorScheme, colorScheme, new SimpleColorScheme(), 0d, 0d);
	}

	/**
	 * Create a new Shape, using the provided {@link ColorScheme}s.
	 * 
	 * @param ambientColorScheme
	 * @param diffuseColorScheme
	 * @param specularColorScheme
	 * @param emissiveColorScheme
	 * @param shininess
	 * @param reflectivity
	 */
	public Shape(ColorScheme ambientColorScheme, ColorScheme diffuseColorScheme, ColorScheme specularColorScheme,
			ColorScheme emissiveColorScheme, double shininess, double reflectivity) {
		this.ambientColorScheme = ambientColorScheme;
		this.diffuseColorScheme = diffuseColorScheme;
		this.specularColorScheme = specularColorScheme;
		this.emissiveColorScheme = emissiveColorScheme;
		this.shininess = shininess;
		this.reflectivity = reflectivity;
	}

	@Override
	public Deque<Transformer> getTransformers() {

		return transformers;
	}

	@Override
	public Vector3D getLocation() {

		return localToWorld(Vector3D.ZERO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public abstract List<Intersection<Shape>> getIntersections(Ray ray);

	public ColorScheme getAmbientColorScheme() {

		return ambientColorScheme;
	}

	public ColorScheme getDiffuseColorScheme() {

		return diffuseColorScheme;
	}

	public void setAmbientColorScheme(ColorScheme ambientColorScheme) {

		this.ambientColorScheme = ambientColorScheme;
	}

	public void setDiffuseColorScheme(ColorScheme diffuseColorScheme) {

		this.diffuseColorScheme = diffuseColorScheme;
	}

	@Override
	public ColorScheme getSpecularColorScheme() {

		return specularColorScheme;
	}

	@Override
	public void setSpecularColorScheme(ColorScheme specularColorScheme) {

		this.specularColorScheme = specularColorScheme;
	}

	@Override
	public double getShininess() {

		return shininess;
	}

	@Override
	public void setShininess(double shininess) {

		this.shininess = shininess;
	}

	@Override
	public ColorScheme getEmissiveColorScheme() {

		return emissiveColorScheme;
	}

	@Override
	public void setEmissiveColorScheme(ColorScheme emissiveColorScheme) {

		this.emissiveColorScheme = emissiveColorScheme;
	}

	public double getReflectivity() {

		return reflectivity;
	}

	public void setReflectivity(double reflectivity) {

		this.reflectivity = reflectivity;
	}

}
