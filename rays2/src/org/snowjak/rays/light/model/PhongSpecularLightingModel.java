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
 * Implements the Phong specular-reflection model.
 * 
 * @author snowjak88
 *
 */
public class PhongSpecularLightingModel implements LightingModel {

	private SuperSamplingAntialiaser<Vector3D, RawColor, RawColor> sampler = new SuperSamplingAntialiaser<>();

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		//
		//
		// We assume that the list of intersections is sorted by ascending
		// distance.
		// And we only care about the first (closest) intersection that isn't
		// too close.
		Intersection<Shape> intersect = intersection.get();
		Vector3D point = intersect.getPoint();

		//
		// totalX = total light of type X seen by this Ray
		RawColor totalSpecular = new RawColor();

		//
		//
		// The total light received by this ray is partially a function of all
		// the lights in the world ...
		int rayCount = RaytracerContext.getSingleton().getSettings().getDistributedRayCount();

		for (Shape emissiveShape : RaytracerContext.getSingleton().getCurrentWorld().getEmissiveShapes()) {

			RawColor specularLightFromThisEmissive = sampler.execute(emissiveShape.getLocation(), (v) -> {
				Collection<Vector3D> results = new LinkedList<>();
				results.add(v);
				results.addAll(emissiveShape.selectPointsWithin(rayCount));
				return results;

			}, (v) -> {
				if (RaytracerContext.getSingleton().getCurrentWorld().isPointVisibleFromEye(v, point, emissiveShape))
					return calculatePhongSpecularity(intersect, emissiveShape);
				else
					return new RawColor();

			}, (cp) -> cp.parallelStream().map(p -> p.getValue()).reduce(new RawColor(), (c1, c2) -> c1.add(c2)));

			totalSpecular = totalSpecular.add(specularLightFromThisEmissive);
		}

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setNormal(intersect.getNormal());
		result.setPoint(intersect.getPoint());
		result.setRadiance(totalSpecular);

		return Optional.of(result);
	}

	private Vector3D getReflection(Vector3D v, Vector3D normal) {

		return v.subtract(normal.scalarMultiply(2d * v.dotProduct(normal)));
	}

	private RawColor calculatePhongSpecularity(Intersection<Shape> intersect, Shape emissiveShape) {

		Vector3D emissiveLocation = emissiveShape.getLocation();
		Vector3D toEmissiveVector = emissiveLocation.subtract(intersect.getPoint());
		//
		// toLightRay == ray from the intersection-point to the light.
		Ray toLightRay = new Ray(intersect.getPoint(), toEmissiveVector.normalize());

		Optional<Intersection<Shape>> emissiveSurfaceIntersection = RaytracerContext.getSingleton()
				.getCurrentWorld()
				.getShapeIntersections(toLightRay)
				.parallelStream()
				.filter(i -> i.getIntersected() == emissiveShape)
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.findFirst();

		if (emissiveSurfaceIntersection.isPresent()) {
			//
			//
			// Calculate the diffuse light the current Light contributes
			// to
			// this ray
			double emissiveExposure = emissiveSurfaceIntersection.get()
					.getPoint()
					.subtract(intersect.getPoint())
					.normalize()
					.dotProduct(intersect.getNormal());
			if (Double.compare(emissiveExposure, 0d) > 0) {

				//
				//
				// Calculate the specular light the current Light
				// contributes to this ray

				//
				// toEyeVector == the direction from the eye to the
				// intersection-point.
				Vector3D fromEyeVector = intersect.getRay().getVector();

				//
				// reflectedLightVector = the vector from the light,
				// reflecting off the surface at the intersection-point
				Vector3D reflectedLightVector = getReflection(toEmissiveVector, intersect.getNormal()).normalize();

				double specularDotProduct = reflectedLightVector.dotProduct(fromEyeVector.normalize());

				if (Double.compare(specularDotProduct, 0d) > 0) {

					//
					// What are the configured colors for this shape?
					RawColor intersectSpecularColor = intersect.getSpecular(intersect.getPoint());
					double shininess = intersect.getSpecularColorScheme().getShininess(intersect.getPoint());

					double specularIntensity = FastMath.pow(specularDotProduct, shininess);

					double falloff = 1d / (4d * FastMath.PI
							* intersect.getPoint().distance(emissiveSurfaceIntersection.get().getPoint()));

					RawColor lightSpecularIntensity = emissiveSurfaceIntersection.get()
							.getEmissive(emissiveSurfaceIntersection.get().getPoint())
							.orElse(new RawColor())
							.multiplyScalar(specularIntensity)
							.multiplyScalar(falloff);

					return intersectSpecularColor.multiply(lightSpecularIntensity);
				}
			}
		}

		return new RawColor();
	}

}
