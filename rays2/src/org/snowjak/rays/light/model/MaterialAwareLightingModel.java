package org.snowjak.rays.light.model;

import static org.apache.commons.math3.util.FastMath.*;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

import javafx.scene.paint.Color;

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

			Optional<RawColor> surfaceColor = child.determineRayColor(ray, intersections);
			if (surfaceColor.isPresent())
				reflectedColor.add(surfaceColor.get().multiplyScalar(reflectance));
		}
		if (transmittance > 0d) {
			List<Intersection<Shape>> refractedIntersections = World.getSingleton().getShapeIntersections(refractedRay);
			refractedColor = World.getSingleton()
					.getLightingModel()
					.determineRayColor(refractedRay, refractedIntersections)
					.orElse(new RawColor());

			if (!refractedIntersections.isEmpty()) {

				double refractedDistance = refractedIntersections.get(0).getDistanceFromRayOrigin();
				double refractedFalloff = (1d / refractedDistance)
						* intersect.getEnteringMaterial().getTransparency(point);

				RawColor interiorColor = intersect.getEnteringMaterial().getColor(point);

				refractedColor = refractedColor.multiply(interiorColor.multiplyScalar(refractedFalloff));

			}
		}

		reflectedColor = reflectedColor.multiplyScalar(reflectance);
		refractedColor = refractedColor.multiplyScalar(transmittance);

		return Optional.of(reflectedColor.add(refractedColor));
	}

	private Vector3D getNormalPart(Vector3D v, Vector3D normal) {

		return normal.scalarMultiply(normal.dotProduct(v));
	}

	private Vector3D getTangentPart(Vector3D v, Vector3D normal) {

		return v.subtract(getNormalPart(v, normal));
	}

}
