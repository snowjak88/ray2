package org.snowjak.rays.light.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.antialias.SuperSamplingAntialiaser;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

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
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		return Optional.of(lightIntersection(intersection.get()));
	}

	private LightingResult lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		Vector3D normal = intersection.getNormal();
		RawColor totalLightAtPoint = new RawColor();

		for (Shape emissiveShape : RaytracerContext.getSingleton().getCurrentWorld().getEmissiveShapes()) {

			if (emissiveShape == intersection.getIntersected())
				continue;

			// If this light has a radius, then we're doing soft shadows and
			// therefore shooting many shadow-rays.
			// Otherwise, we're only doing 1 shadow-ray.
			int rayCount = RaytracerContext.getSingleton().getSettings().getDistributedRayCount();

			RawColor totalLightFromEmissive = lightAntialiaser.execute(emissiveShape.getLocation(), (v) -> {
				Collection<Vector3D> samples = new LinkedList<>();
				samples.add(v);
				samples.addAll(emissiveShape.selectPointsWithin(rayCount));
				return samples;

			}, (v) -> {
				if (!doLightOccluding || RaytracerContext.getSingleton().getCurrentWorld().isPointVisibleFromEye(v,
						point, emissiveShape)) {

					Ray toEmissiveRay = new Ray(point, v.subtract(point));
					Optional<Intersection<Shape>> emissiveIntersection = emissiveShape
							.getIntersections(toEmissiveRay, false, true).stream().findFirst();
					if (!emissiveIntersection.isPresent())
						return new RawColor();

					Vector3D emissiveSurfacePoint = emissiveIntersection.get().getPoint();
					RawColor emissiveSurfaceRadiance = emissiveIntersection.get()
							.getEmissive(emissiveSurfacePoint)
							.orElse(new RawColor());
					//
					// Calculate the received radiance for this sample ray using
					// both exposure (via Lambert's Law) and falloff ( == 1 / (4
					// * PI * d))
					double exposure = FastMath.max(emissiveSurfacePoint.subtract(point).normalize().dotProduct(normal),
							0d);
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

			totalLightAtPoint = totalLightAtPoint.add(totalLightFromEmissive);

		}

		RawColor pointColor = intersection.getDiffuse(point);

		LightingResult result = new LightingResult();
		result.setPoint(point);
		result.setNormal(normal);
		result.setEye(intersection.getRay());
		result.setRadiance(pointColor.multiply(totalLightAtPoint));

		return result;
	}

}
