package org.snowjak.rays.light.model;

import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * Implements an ambient lighting model.
 * <p>
 * Simply, all points are illuminated by the sum of every light's
 * ambient-intensity.
 * </p>
 * 
 * @author snowjak
 *
 */
public class AmbientLightingModel implements LightingModel {

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		return Optional.of(lightIntersection(intersection.get()));
	}

	private RawColor lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		RawColor worldAmbientRadiance = RaytracerContext.getSingleton().getCurrentWorld().getAmbientRadiance();

		RawColor pointColor = intersection.getDiffuse(point);

		return worldAmbientRadiance.multiply(pointColor);
	}

}
