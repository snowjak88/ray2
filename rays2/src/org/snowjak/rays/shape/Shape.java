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

	/**
	 * By default, the ambient and diffuse color-schemes will take this value.
	 */
	public static final ColorScheme DEFAULT_COLOR_SCHEME = new SimpleColorScheme(Color.HOTPINK);
	/**
	 * By default, the specular color-scheme will take this value.
	 */
	public static final ColorScheme DEFAULT_SPECULAR_COLOR_SCHEME = new SimpleColorScheme(Color.WHITE);
	/**
	 * By default, the emissive color-scheme will take this value.
	 */
	public static final ColorScheme DEFAULT_EMISSIVE_COLOR_SCHEME = new SimpleColorScheme(Color.BLACK);

	private Deque<Transformer> transformers = new LinkedList<>();

	private ColorScheme ambientColorScheme = DEFAULT_COLOR_SCHEME, diffuseColorScheme = DEFAULT_COLOR_SCHEME,
			specularColorScheme = DEFAULT_SPECULAR_COLOR_SCHEME, emissiveColorScheme = DEFAULT_EMISSIVE_COLOR_SCHEME;

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
	public List<Intersection<Shape>> getIntersections(Ray ray) {
		return getIntersectionsIncludingBehind(ray).stream().sequential()
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), 0d) >= 0)
				.collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
	}

	@Override
	public abstract List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray);

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
	public ColorScheme getEmissiveColorScheme() {

		return emissiveColorScheme;
	}

	@Override
	public void setEmissiveColorScheme(ColorScheme emissiveColorScheme) {

		this.emissiveColorScheme = emissiveColorScheme;
	}

}
