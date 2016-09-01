package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * Represents an algorithm which performs lighting-calculations to determine the
 * color resulting from a single Ray. Depending on the complexity of the
 * underlying algorithm, this LightingModel may be recursive, spawning
 * additional Rays to compute reflection, refraction, etc.
 * 
 * @author rr247200
 *
 */
public interface LightingModel {

	/**
	 * Determine the color resulting from a {@link Ray} and a given set of
	 * {@link Intersection}s produced by it.
	 * 
	 * @param ray
	 * @param intersections
	 * @return the resulting Color, if any
	 */
	public Optional<RawColor> determineRayColor(Ray ray,
			List<Intersection<Shape>> intersections);
}
