package org.snowjak.rays.light.model;

import java.util.List;
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
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		return Optional.of(lightIntersection(intersections.stream()
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0).findFirst().get()));
	}

	private LightingResult lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		RawColor totalLightAtPoint = new RawColor();

		for (Light light : World.getSingleton().getLights()) {

			Ray toLightRay = new Ray(point, light.getLocation().subtract(point));

			totalLightAtPoint = totalLightAtPoint.add(light.getAmbientIntensity(toLightRay));

		}

		RawColor pointColor = intersection.getEnteringMaterial().getColor(point);

		LightingResult result = new LightingResult();
		result.setEye(intersection.getRay().getVector());
		result.setNormal(intersection.getNormal());
		result.setPoint(point);
		result.setRadiance(totalLightAtPoint.multiply(pointColor));
		result.getVisibleLights().addAll(World.getSingleton().getLights());

		return result;
	}

}
