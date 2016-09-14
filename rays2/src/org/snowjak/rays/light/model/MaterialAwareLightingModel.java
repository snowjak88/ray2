package org.snowjak.rays.light.model;

import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Shape;

/**
 * A {@link LightingModel} that implements the Schlick approximation to the
 * Fresnel equations.
 * <p>
 * The Fresnel equations model the relative proportion of reflected vs.
 * refracted light between two materials of differing indices of refraction. The
 * Schlick approximation simplifies these equations by ignoring the change in
 * phase-angle that reflected and refracted light undergoes.
 * </p>
 * <p>
 * This LightingModel does not model sub-surface scattering, nor does it do an
 * especially rigorous job of handling surface colors. It ignores the effect of
 * intervening materials (i.e., the Material sitting between the ray's origin
 * and the first intersection), and relies on a {@link PhongLightingModel} to
 * derive the lighting at a point on an object's surface.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class MaterialAwareLightingModel implements LightingModel {

	private LightingModel child = new PhongLightingModel();

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		//
		//
		//
		Intersection<Shape> intersect = intersections.get(0);
		Vector3D point = intersect.getPoint();
		Vector3D i = intersect.getRay().getVector();
		Vector3D n = intersect.getNormal();
		double theta_i = Vector3D.angle(i.negate(), n);

		if (ray.getRecursiveLevel() > World.getSingleton().getMaxRayRecursion()) {
			return child.determineRayColor(ray, intersections);
		}

		//
		//
		//
		double n1 = intersect.getLeavingMaterial().getRefractiveIndex(point),
				n2 = intersect.getEnteringMaterial().getRefractiveIndex(point);

		//
		//
		// Determine reflected ray
		Vector3D reflectedVector = getTangentPart(i, n).subtract(getNormalPart(i, n));
		Ray reflectedRay = new Ray(point, reflectedVector, ray.getRecursiveLevel() + 1);

		//
		//
		// Determine refracted ray
		double sin2_theta_t = pow(n1 / n2, 2d) * (1d - pow(cos(theta_i), 2d));
		Vector3D refractedVector = i.scalarMultiply(n1 / n2)
				.add(n.scalarMultiply((n1 / n2) * cos(theta_i) - sqrt(1d - sin2_theta_t)));
		Ray refractedRay = new Ray(point, refractedVector, ray.getRecursiveLevel() + 1);

		//
		//
		// Calculate reflectance and transmittance fractions
		double reflectance = 1d;
		//
		//
		// Is this *not* a case of Total-Internal Reflection?
		if (sin2_theta_t <= 1d) {
			//
			double cos_theta_t = sqrt(1d - sin2_theta_t);
			double r_normal = pow((n1 * cos(theta_i) - n2 * cos_theta_t) / (n1 * cos(theta_i) + n2 * cos_theta_t), 2d);
			double r_tangent = pow((n2 * cos(theta_i) - n1 * cos_theta_t) / (n2 * cos(theta_i) + n1 * cos_theta_t), 2d);

			reflectance = (r_normal + r_tangent) / 2d;
		}
		double transmittance = 1d - reflectance;

		//
		//
		// Now shoot some rays!
		RawColor reflectedColor = new RawColor(), refractedColor = new RawColor();

		if (reflectance > 0d) {
			List<Intersection<Shape>> reflectedIntersections = World.getSingleton().getShapeIntersections(reflectedRay);
			reflectedColor = World.getSingleton()
					.getLightingModel()
					.determineRayColor(reflectedRay, reflectedIntersections)
					.orElse(new RawColor());

		}
		if (transmittance > 0d) {
			List<Intersection<Shape>> refractedIntersections = World.getSingleton().getShapeIntersections(refractedRay);
			refractedColor = World.getSingleton()
					.getLightingModel()
					.determineRayColor(refractedRay, refractedIntersections)
					.orElse(new RawColor());

			double materialDepth = 1d;
			if (!refractedIntersections.isEmpty()) {
				materialDepth = refractedIntersections.get(0).getDistanceFromRayOrigin();
			}
			Material scatteringMaterial = intersect.getEnteringMaterial();

			double reflectedFromInterveningScattering = max(
					min(scatteringMaterial.getDensity(intersect.getPoint()) * materialDepth, 1d), 0d);
			double transmittedThroughInterveningScattering = 1d - reflectedFromInterveningScattering;

			RawColor reflectedInterveningColor = lightIntersection(intersect);
			// child.determineRayColor(ray, intersections).get();
			refractedColor = Functions.lerp(reflectedInterveningColor, refractedColor,
					transmittedThroughInterveningScattering);

			// double surfaceReflectivity =
			// intersect.getEnteringMaterial().getReflectivity(point);
			// Optional<RawColor> surfaceColor =
			// child.determineRayColor(ray,
			// intersections);
			// if (surfaceColor.isPresent())
			// refractedColor = refractedColor.multiplyScalar(1d -
			// surfaceReflectivity)
			// .add(surfaceColor.get().multiplyScalar(surfaceReflectivity));

		}

		reflectedColor = reflectedColor.multiplyScalar(reflectance);
		refractedColor = refractedColor.multiplyScalar(transmittance);

		return Optional.of(reflectedColor.add(refractedColor));
	}

	private RawColor lightIntersection(Intersection<Shape> intersection) {

		Vector3D point = intersection.getPoint();
		Vector3D normal = intersection.getNormal();
		RawColor totalLightAtPoint = new RawColor();

		for (Light light : World.getSingleton().getLights()) {

			Ray toLightRay = new Ray(point, light.getLocation().subtract(point));

			totalLightAtPoint = totalLightAtPoint.add(light.getAmbientIntensity(toLightRay));

			boolean isOccludingIntersections = World.getSingleton()
					.getShapeIntersections(toLightRay)
					.parallelStream()
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

	private Vector3D getNormalPart(Vector3D v, Vector3D normal) {

		return normal.scalarMultiply(normal.dotProduct(v));
	}

	private Vector3D getTangentPart(Vector3D v, Vector3D normal) {

		return v.subtract(getNormalPart(v, normal));
	}

}
