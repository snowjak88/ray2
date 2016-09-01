package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.shape.Shape;

public class PhongReflectionLightingModel implements LightingModel {

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		Intersection<Shape> intersect = intersections.get(0);
		Shape shape = intersect.getIntersected();
		RawColor shapeAmbientColor = shape.getAmbient(shape.worldToLocal(intersect.getPoint()));
		RawColor shapeDiffuseColor = getReflectiveShapeDiffuseColor(intersect);
		RawColor shapeSpecularColor = shape.getSpecular(shape.worldToLocal(intersect.getPoint()));
		RawColor shapeEmissiveColor = shape.getEmissive(shape.worldToLocal(intersect.getPoint()));

		RawColor totalAmbient = new RawColor(), totalDiffuse = new RawColor(), totalSpecular = new RawColor(),
				totalEmissive = new RawColor();

		// totalDiffuse.add(shapeReflectedDiffuseColor);

		for (Light light : World.getSingleton().getLights()) {

			Vector3D lightLocation = light.getLocation();
			Vector3D toLightVector = lightLocation.subtract(intersect.getPoint());
			Ray toLightRay = new Ray(intersect.getPoint(), toLightVector.normalize());

			Vector3D toEyeVector = intersect.getRay().getVector();

			boolean lightIsVisible = true;
			List<Intersection<Shape>> toLightIntersections = World.getSingleton().getShapeIntersections(toLightRay);
			if (toLightIntersections.stream()
					.anyMatch(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) > 0))
				lightIsVisible = false;

			//
			//
			// Calculate the ambient light the current Light contributes to this
			// ray
			RawColor lightAmbientIntensity = light.getAmbientIntensity(toLightRay);

			totalAmbient = totalAmbient.add(lightAmbientIntensity.multiply(shapeAmbientColor));

			if (lightIsVisible) {
				// if (true) {

				//
				//
				// Calculate the diffuse light the current Light contributes to
				// this ray
				double lightExposure = light.getExposure(intersect);
				if (Double.compare(lightExposure, 0d) > 0) {
					RawColor lightDiffuseIntensity = light.getDiffuseIntensity(toLightRay);
					totalDiffuse = totalDiffuse
							.add(lightDiffuseIntensity.multiply(shapeDiffuseColor).multiplyScalar(lightExposure));

					//
					//
					// Calculate the specular light the current Light
					// contributes to
					// this ray
					Vector3D reflectedLightVector = getReflection(toLightVector, intersect.getNormal()).normalize();
					double specularDotProduct = reflectedLightVector.dotProduct(toEyeVector.normalize());

					if (Double.compare(specularDotProduct, 0d) > 0) {
						double specularIntensity = FastMath.pow(specularDotProduct, shape.getShininess());
						RawColor lightSpecularIntensity = light.getSpecularIntensity(toLightRay)
								.multiplyScalar(specularIntensity);

						totalSpecular = totalSpecular.add(shapeSpecularColor.multiply(lightSpecularIntensity));
					}
				}

			}

		}

		totalEmissive = shapeEmissiveColor;

		RawColor totalColor = totalAmbient.add(totalDiffuse).add(totalSpecular).add(totalEmissive);

		return Optional.of(totalColor);
	}

	private RawColor getReflectiveShapeDiffuseColor(Intersection<Shape> intersection) {

		Shape shape = intersection.getIntersected();
		RawColor shapeColor = shape.getDiffuse(shape.worldToLocal(intersection.getPoint()));
		double shapeReflectivity = shape.getReflectivity();

		Ray originalRay = intersection.getRay();

		if (originalRay.getRecursiveLevel() >= World.MAX_RAY_RECURSION || Double.compare(shapeReflectivity, 0d) <= 0)
			return new RawColor();

		Vector3D intersectPoint = intersection.getPoint();
		Vector3D eyeVector = intersection.getRay().getOrigin().subtract(intersectPoint);
		Vector3D reflectedEyeVector = getReflection(eyeVector.negate().normalize(), intersection.getNormal())
				.normalize();

		Ray reflectedRay = new Ray(intersectPoint, reflectedEyeVector, originalRay.getRecursiveLevel() + 1);
		List<Intersection<Shape>> intersections = World.getSingleton().getShapeIntersections(reflectedRay);
		Optional<RawColor> reflection = World.getSingleton().getLightingModel().determineRayColor(reflectedRay,
				intersections);

		RawColor reflectedColor = new RawColor();
		if (reflection.isPresent())
			reflectedColor = reflection.get().multiplyScalar(shapeReflectivity);

		return reflectedColor.add(shapeColor.multiplyScalar(1d - shapeReflectivity));
	}

	private Vector3D getReflection(Vector3D v, Vector3D normal) {

		return v.subtract(normal.scalarMultiply(2d * v.dotProduct(normal)));
	}

}
