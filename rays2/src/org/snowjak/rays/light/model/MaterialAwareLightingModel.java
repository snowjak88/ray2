package org.snowjak.rays.light.model;

import static org.apache.commons.math3.util.FastMath.*;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Shape;

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

		if (ray.getRecursiveLevel() > World.MAX_RAY_RECURSION) {
			return child.determineRayColor(ray, intersections);
		}

		//
		//
		// Is this a case of Total-Internal Reflection?
		double criticalAngle = 0d;
		double n1 = intersect.getLeavingMaterial().getRefractiveIndex(point),
				n2 = intersect.getEnteringMaterial().getRefractiveIndex(point);
		if (Double.compare(n1, n2) > 0)
			criticalAngle = asin(n2 / n1);

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
		// If Total-Internal-Reflection does NOT hold ...
		if (sin2_theta_t <= 1d) {
			double cos_theta_t = sqrt(1d - sin2_theta_t);
			double r_normal = pow((n1 * cos(theta_i) - n2 * cos_theta_t) / (n1 * cos(theta_i) + n2 * cos_theta_t), 2d);
			double r_tangent = pow((n2 * cos(theta_i) - n1 * cos_theta_t) / (n2 * cos(theta_i) + n1 * cos_theta_t), 2d);

			reflectance = (r_normal + r_tangent) / 2d;
		}
		double transmittance = 1d - reflectance;

		//
		//
		// Now shoot some rays!
		RawColor reflectedColor = new RawColor(), refractedColor = new RawColor(), interveningColor = new RawColor(),
				surfaceColor = new RawColor();

		double interveningTransparency = max(
				(1d - intersect.getLeavingMaterial().getTransparency(point)) * intersect.getDistanceFromRayOrigin(),
				1d);
		interveningColor = intersect.getLeavingMaterial().getSurfaceColor(point);

		double surfaceTransparency = intersect.getEnteringMaterial().getTransparency(point);
		surfaceColor = child.determineRayColor(ray, intersections).orElse(surfaceColor);

		if (reflectance > 0d) {
			List<Intersection<Shape>> reflectedIntersections = World.getSingleton().getShapeIntersections(reflectedRay);
			reflectedColor = World.getSingleton()
					.getLightingModel()
					.determineRayColor(reflectedRay, reflectedIntersections)
					.orElse(new RawColor())
					.multiplyScalar(reflectance);
		}
		if (transmittance > 0d) {
			List<Intersection<Shape>> refractedIntersections = World.getSingleton().getShapeIntersections(refractedRay);
			refractedColor = World.getSingleton()
					.getLightingModel()
					.determineRayColor(refractedRay, refractedIntersections)
					.orElse(new RawColor())
					.multiplyScalar(transmittance);
		}

		surfaceColor = surfaceColor.multiplyScalar(1d - surfaceTransparency);
		interveningColor = interveningColor.multiplyScalar(1d - interveningTransparency);
		reflectedColor = reflectedColor.multiplyScalar(reflectance)
				.multiplyScalar(interveningTransparency)
				.multiplyScalar(surfaceTransparency);
		refractedColor = refractedColor.multiplyScalar(transmittance)
				.multiplyScalar(interveningTransparency)
				.multiplyScalar(surfaceTransparency);

		return Optional.of(surfaceColor.add(interveningColor).add(reflectedColor).add(refractedColor));
	}

	private RawColor getInterveningColor(Ray ray, Intersection<Shape> firstIntersection) {

		RawColor totalColor = new RawColor();
		Vector3D origin = ray.getOrigin();

		for (double t = 0d; t <= 1d; t += 0.1) {

			Vector3D point = origin.add(firstIntersection.getPoint().subtract(origin).scalarMultiply(t));
			Material material = firstIntersection.getLeavingMaterial();
			double visibleFraction = (1d - material.getTransparency(point))
					* firstIntersection.getDistanceFromRayOrigin();

			totalColor = totalColor
					.add(material.getSurfaceColor(point).multiplyScalar(visibleFraction).multiplyScalar(0.1));
		}

		return totalColor;
	}

	private Vector3D getNormalPart(Vector3D v, Vector3D normal) {

		return normal.scalarMultiply(normal.dotProduct(v));
	}

	private Vector3D getTangentPart(Vector3D v, Vector3D normal) {

		return v.subtract(getNormalPart(v, normal));
	}

}
