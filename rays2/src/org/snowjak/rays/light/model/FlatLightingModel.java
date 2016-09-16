package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.snowjak.rays.Ray;
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
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		Intersection<Shape> intersect = intersections.get(0);

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setNormal(intersect.getNormal());
		result.setPoint(intersect.getPoint());
		result.setRadiance(intersect.getDiffuse(intersect.getPoint()));

		return Optional.of(result);
	}

}
