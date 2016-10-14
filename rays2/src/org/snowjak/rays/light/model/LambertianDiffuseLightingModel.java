package org.snowjak.rays.light.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.World;
import org.snowjak.rays.antialias.SuperSamplingAntialiaser;
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

	private final boolean doLightOccluding;

	private final int softShadowRayCount;

	private final SuperSamplingAntialiaser<Vector3D, Double, Double> lightAntialiaser = new SuperSamplingAntialiaser<>();

	private final Random rnd = new Random();

	/**
	 * Construct a new {@link LambertianDiffuseLightingModel}.
	 */
	public LambertianDiffuseLightingModel() {
		this(16);
	}

	/**
	 * Construct a new {@link LambertianDiffuseLightingModel}, specifying the
	 * number of shadow-rays to use when calculating soft-shadows.
	 * 
	 * @param softShadowRayCount
	 */
	public LambertianDiffuseLightingModel(int softShadowRayCount) {
		this(true, softShadowRayCount);
	}

	/**
	 * Construct a new {@link LambertianDiffuseLightingModel}, specifying
	 * whether to check for light-occlusion when illuminating surfaces, and
	 * whether to perform soft-shadowing.
	 * 
	 * @param doLightOccluding
	 * @param doSoftShadows
	 * @param softShadowRayCount
	 */
	public LambertianDiffuseLightingModel(boolean doLightOccluding, int softShadowRayCount) {
		this.doLightOccluding = doLightOccluding;
		this.softShadowRayCount = softShadowRayCount;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		return Optional.of(lightIntersection(intersection.get()));
	}

	private LightingResult lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		Vector3D normal = intersection.getNormal();
		RawColor totalLightAtPoint = new RawColor();

		Collection<Light> visibleLights = new LinkedList<>();
		for (Light light : RaytracerContext.getSingleton().getCurrentWorld().getLights()) {

			// If this light has a radius, then we're doing soft shadows and
			// therefore shooting many shadow-rays.
			// Otherwise, we're only doing 1 shadow-ray.
			int rayCount = (light.getRadius().isPresent() ? softShadowRayCount : 1);

			double totalLightFraction = lightAntialiaser.execute(light.getLocation(), (v) -> {
				Collection<Vector3D> samples = new LinkedList<>();
				samples.add(v);

				for (int i = 0; i < rayCount; i++) {
					double theta = rnd.nextDouble() * FastMath.PI;
					double phi = 2d * rnd.nextDouble() * FastMath.PI;
					double r = rnd.nextDouble() * light.getRadius().orElse(0d);
					samples.add(v.add(new Vector3D(r * FastMath.sin(theta) * FastMath.cos(phi),
							r * FastMath.sin(theta) * FastMath.cos(theta), r * FastMath.cos(theta))));
				}
				return samples;

			}, (v) -> {
				if (isPointVisibleFromPoint(point, v))
					return 1d;
				else
					return 0d;

			}, (cp) -> {
				return cp.parallelStream().map(p -> p.getValue()).reduce(0d, (d1, d2) -> d1 + d2) / (double) cp.size();
			});

			if (Double.compare(totalLightFraction, World.NEARLY_ZERO) < 0)
				continue;

			visibleLights.add(light);

			double exposure = light.getExposure(point, normal);

			if (Double.compare(exposure, 0d) <= 0)
				continue;

			double intensity = exposure * light.getIntensity(point) * light.getFalloff(point) * totalLightFraction;

			totalLightAtPoint = totalLightAtPoint.add(light.getDiffuseColor().multiplyScalar(intensity));

		}

		RawColor pointColor = intersection.getDiffuse(point);

		LightingResult result = new LightingResult();
		result.setPoint(point);
		result.setNormal(normal);
		result.setEye(intersection.getRay());
		result.setRadiance(pointColor.multiply(totalLightAtPoint));
		result.getVisibleLights().addAll(visibleLights);

		return result;
	}

	private boolean isPointVisibleFromPoint(Vector3D observingPoint, Vector3D observedPoint) {

		Optional<Intersection<Shape>> occludingIntersection = RaytracerContext.getSingleton()
				.getCurrentWorld()
				.getClosestShapeIntersection(new Ray(observingPoint, observedPoint.subtract(observingPoint)));

		if (doLightOccluding) {
			if (occludingIntersection.isPresent()
					&& Double.compare(occludingIntersection.get().getDistanceFromRayOrigin(),
							observingPoint.distance(observedPoint)) < 0)
				return false;
		}

		return true;
	}

}
