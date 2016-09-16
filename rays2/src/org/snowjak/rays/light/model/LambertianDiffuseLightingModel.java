package org.snowjak.rays.light.model;

import java.util.Collection;
import java.util.LinkedList;
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

	private boolean doLightOccluding;

	/**
	 * Construct a new {@link LambertianDiffuseLightingModel}.
	 */
	public LambertianDiffuseLightingModel() {
		this(true);
	}

	/**
	 * Construct a new {@link LambertianDiffuseLightingModel}, specifying
	 * whether to check for light-occlusion when lighting points.
	 * 
	 * @param doLightOccluding
	 */
	public LambertianDiffuseLightingModel(boolean doLightOccluding) {
		this.doLightOccluding = doLightOccluding;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		return Optional.of(lightIntersection(intersections.stream()
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0)
				.findFirst()
				.get()));
	}

	private LightingResult lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		Vector3D normal = intersection.getNormal();
		RawColor totalLightAtPoint = new RawColor();

		Collection<Light> visibleLights = new LinkedList<>();
		for (Light light : World.getSingleton().getLights()) {

			Ray toLightRay = new Ray(point, light.getLocation().subtract(point));

			boolean isOccludingIntersections = false;
			if (doLightOccluding)
				isOccludingIntersections = World.getSingleton()
						.getShapeIntersections(toLightRay)
						.parallelStream()
						.filter(i -> Double.compare(i.getDistanceFromRayOrigin(),
								light.getLocation().distance(point)) < 0)
						.anyMatch(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0);
			if (isOccludingIntersections)
				continue;

			visibleLights.add(light);

			double exposure = light.getExposure(point, normal);

			if (Double.compare(exposure, 0d) <= 0)
				continue;

			totalLightAtPoint = totalLightAtPoint.add(light.getDiffuseIntensity(toLightRay).multiplyScalar(exposure));

		}

		RawColor pointColor = intersection.getDiffuse(point);

		LightingResult result = new LightingResult();
		result.setPoint(point);
		result.setNormal(normal);
		result.setEye(intersection.getRay());
		result.setRadiance(pointColor);
		result.getVisibleLights().addAll(visibleLights);

		return result;
	}

}
