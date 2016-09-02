package org.snowjak.rays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.transform.Translation;

public class LightBlockingTestbed {

	public static void main(String[] args) {

		World world = World.getSingleton();

		Sphere sphere = new Sphere();
		// sphere.getTransformers().add(new Translation(-1d, 0d, -1d));
		world.getShapes().add(sphere);

		Plane plane = new Plane();
		plane.getTransformers().add(new Translation(0d, -6d, 0d));
		world.getShapes().add(plane);

		// sphere = new Sphere();
		// sphere.getTransformers().add(new Translation(-1d, 0d, 1d));
		// world.getShapes().add(sphere);
		// sphere = new Sphere();
		// sphere.getTransformers().add(new Translation(1d, 0d, -1d));
		// world.getShapes().add(sphere);
		// sphere = new Sphere();
		// sphere.getTransformers().add(new Translation(1d, 0d, 1d));
		// world.getShapes().add(sphere);

		Light worldLight = new Light(new RawColor(), new RawColor(), new RawColor());
		worldLight.getTransformers().add(new Translation(0d, -1d, -4d));
		world.getLights().add(worldLight);

		Ray ray = new Ray(new Vector3D(0d, 0d, -5d), new Vector3D(0d, 0d, 1d));

		for (Intersection<Shape> intersection : world.getShapeIntersections(ray)) {

			System.out.println("\n\n-=-=- Shape Intersection -=-=-");
			System.out.println("Intersecting: " + intersection.getIntersected().toString() + " (distance = "
					+ intersection.getIntersected().getLocation().distance(ray.getOrigin()) + ")");
			System.out.println("Intersection Point: " + intersection.getPoint().toString());
			System.out.println("Intersection Normal: " + intersection.getNormal().toString());
			System.out.println("Distance to Intersection: " + intersection.getDistanceFromRayOrigin());

			for (Light light : world.getLights()) {
				Vector3D toLight = light.getLocation().subtract(intersection.getPoint());

				System.out.println("\nLight: " + light.toString());
				System.out.println("Light location: " + light.getLocation().toString());
				System.out.println("Light distance: " + toLight.getNorm());
				System.out.println("Vector from intersection-point to light: " + toLight.toString()
						+ " -- (normalized): " + toLight.normalize().toString());

				Ray toLightRay = new Ray(intersection.getPoint(), toLight.normalize(), 1);
				System.out.println("Ray for detecting light-blockers: " + toLightRay.toString());

				System.out.println("Looking for light occluders ...");
				for (Intersection<Shape> lightBlocker : world.getShapeIntersections(toLightRay)) {

					System.out.println(
							"\nObject occluding light from intersection: " + lightBlocker.getIntersected().toString());
					System.out.println("Light-blocker intersection point: " + lightBlocker.getPoint().toString());
					System.out.println("Light-blocker intersection normal: " + lightBlocker.getNormal().toString());
					System.out.println("Light-blocker intersection distance from original intersection: "
							+ lightBlocker.getDistanceFromRayOrigin());
					System.out.println("Direction from light-blocker point to original point: "
							+ lightBlocker.getPoint().subtract(intersection.getPoint()).normalize().toString());

				}
			}

		}

	}

}
