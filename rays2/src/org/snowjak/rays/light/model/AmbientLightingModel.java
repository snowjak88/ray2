package org.snowjak.rays.light.model;

import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
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
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		return Optional.of(lightIntersection(intersection.get()));
	}

	private LightingResult lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		RawColor totalLightAtPoint = new RawColor();

		for (Light light : World.getSingleton().getLights())
			totalLightAtPoint = totalLightAtPoint.add(light.getAmbientColor());

		RawColor pointColor = intersection.getEnteringMaterial().getColor(point);

		LightingResult result = new LightingResult();
		result.setEye(intersection.getRay());
		result.setNormal(intersection.getNormal());
		result.setPoint(point);
		result.setRadiance(totalLightAtPoint.multiply(pointColor));
		result.getVisibleLights().addAll(World.getSingleton().getLights());

		return result;
	}

}
