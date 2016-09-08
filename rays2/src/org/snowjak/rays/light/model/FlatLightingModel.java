package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * Implements a "flat" lighting model -- i.e., a LightingModel which does no
 * lighting! Instead, every ray simply yields the color of the closest
 * intersected object.
 * 
 * @author snowjak88
 *
 */
public class FlatLightingModel implements LightingModel {

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		return intersections.stream()
				.map(i -> i.getIntersected().getDiffuse(i.getIntersected().worldToLocal(i.getPoint())))
				.findFirst();
	}

}
