package org.snowjak.rays.light.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.antialias.SuperSamplingAntialiaser;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.DirectionalLight;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.util.ExecutionTimeTracker;
import org.snowjak.rays.world.World;

/**
 * Implements the Lambertian diffuse lighting model.
 * 
 * @author snowjak88
 *
 */
public class LambertianDiffuseLightingModel implements LightingModel {

	private final boolean doLightOccluding;

	private final SuperSamplingAntialiaser<Vector3D, RawColor, RawColor> lightAntialiaser = new SuperSamplingAntialiaser<>();

	/**
	 * Construct a new {@link LambertianDiffuseLightingModel}.
	 */
	public LambertianDiffuseLightingModel() {
		this(true);
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
	public LambertianDiffuseLightingModel(boolean doLightOccluding) {
		this.doLightOccluding = doLightOccluding;
	}

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		Instant start = Instant.now();

		if (!intersection.isPresent())
			return Optional.empty();

		RawColor result = lightIntersection(intersection.get());

		ExecutionTimeTracker.logExecutionRecord("LambertianDiffuseLightingModel", start, Instant.now(), null);

		return Optional.of(result);
	}

	private RawColor lightIntersection(final Intersection<Shape> intersection) {

		final Vector3D point = intersection.getPoint();
		final Vector3D normal = intersection.getNormal();
		RawColor totalLightAtPoint = new RawColor();

		RawColor totalLightFromEmissives = RaytracerContext.getSingleton()
				.getCurrentWorld()
				.getEmissiveShapes()
				.parallelStream()
				.filter(s -> s != intersection.getIntersected())
				.map(s -> {
					int rayCount = RaytracerContext.getSingleton().getSettings().getDistributedRayCount();

					return lightAntialiaser.execute(s.getLocation(), (v) -> {
						List<Pair<Vector3D, Double>> sampledPointsOnEmissive = s.selectPointsWithin(2 * rayCount, true)
								.parallelStream()
								.map(p -> new Pair<>(p, p.subtract(point).normalize().dotProduct(normal)))
								.filter(p -> Double.compare(p.getValue(), 0d) >= 0)
								.collect(Collectors.toCollection(LinkedList::new));

						if (sampledPointsOnEmissive.isEmpty())
							return Arrays.asList(v);

						EnumeratedDistribution<Vector3D> samplePointPicker = new EnumeratedDistribution<>(
								sampledPointsOnEmissive);

						return IntStream.range(0, rayCount).mapToObj(i -> samplePointPicker.sample()).collect(
								Collectors.toCollection(LinkedList::new));
					}, (v) -> {
						if (!doLightOccluding || RaytracerContext.getSingleton()
								.getCurrentWorld()
								.isPointVisibleFromEye(v, point, s, intersection.getIntersected())) {

							Ray toEmissiveRay = new Ray(point, v.subtract(point));
							Optional<Intersection<Shape>> emissiveIntersection = s
									.getIntersections(toEmissiveRay, false, true).stream().findFirst();
							if (!emissiveIntersection.isPresent())
								return new RawColor();

							Vector3D emissiveSurfacePoint = emissiveIntersection.get().getPoint();
							RawColor emissiveSurfaceRadiance = emissiveIntersection.get()
									.getEmissive(emissiveSurfacePoint)
									.orElse(new RawColor());
							//
							// Calculate the received radiance for this
							// sample ray using both exposure (via
							// Lambert's Law) and falloff (== 1 / (4 *
							// PI * d))
							double exposure = FastMath
									.max(emissiveSurfacePoint.subtract(point).normalize().dotProduct(normal), 0d);
							double falloff = 1d / (4d * FastMath.PI * point.distance(emissiveSurfacePoint));

							return emissiveSurfaceRadiance.multiplyScalar(exposure * falloff);
						} else
							return new RawColor();

					}, (cp) -> {
						return cp.parallelStream()
								.map(p -> p.getValue())
								.reduce(new RawColor(), (c1, c2) -> (c1.add(c2)))
								.multiplyScalar(1d / cp.size());
					});
				})
				.reduce(new RawColor(), (c1, c2) -> c1.add(c2));

		totalLightAtPoint = totalLightAtPoint.add(totalLightFromEmissives);

		for (DirectionalLight light : RaytracerContext.getSingleton().getCurrentWorld().getDirectionalLights()) {

			Ray toLightRay = new Ray(intersection.getPoint(), light.getDirection().negate());
			if (!doLightOccluding || !RaytracerContext.getSingleton()
					.getCurrentWorld()
					.getShapeIntersections(toLightRay)
					.parallelStream()
					.anyMatch(i -> Double.compare(i.getDistanceFromRayOrigin(), World.NEARLY_ZERO) > 0)) {

				//
				// Calculate the received radiance for this sample ray using
				// both exposure (via Lambert's Law) and falloff ( == 1 / (4
				// * PI * d))
				double exposure = FastMath.max(light.getDirection().negate().normalize().dotProduct(normal), 0d);

				totalLightAtPoint = totalLightAtPoint.add(light.getRadiance().multiplyScalar(exposure));
			}
		}

		RawColor pointColor = intersection.getDiffuse(point);

		return pointColor.multiply(totalLightAtPoint);
	}

}
