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
 * Implements the Lambertian diffuse lighting model.
 * 
 * @author snowjak88
 *
 */
public class LambertianDiffuseLightingModel implements LightingModel {

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		return Optional.of(lightIntersection(intersections.stream()
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0)
				.findFirst()
				.get()));
	}

	private RawColor lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		Vector3D normal = intersection.getNormal();
		RawColor totalLightAtPoint = new RawColor();

		for (Light light : World.getSingleton().getLights()) {

			Ray toLightRay = new Ray(point, light.getLocation().subtract(point));

			boolean isOccludingIntersections = World.getSingleton()
					.getShapeIntersections(toLightRay)
					.parallelStream()
					.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), light.getLocation().distance(point)) < 0)
					.anyMatch(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0);
			if (isOccludingIntersections)
				continue;

			double exposure = light.getExposure(point, normal);

			if (Double.compare(exposure, 0d) <= 0)
				continue;

			totalLightAtPoint = totalLightAtPoint.add(light.getDiffuseIntensity(toLightRay).multiplyScalar(exposure));

		}

		RawColor pointColor = intersection.getEnteringMaterial().getColor(point);

		return totalLightAtPoint.multiply(pointColor);
	}

}
