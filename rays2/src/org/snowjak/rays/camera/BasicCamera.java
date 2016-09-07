package org.snowjak.rays.camera;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * A basic {@link Camera} implementation. Does not perform any processing
 * (antialiasing, etc.).
 * 
 * @author rr247200
 *
 */
public class BasicCamera extends Camera {

	/**
	 * Construct a new BasicCamera.
	 * 
	 * @param cameraFrameWidth
	 * @param fieldOfView
	 */
	public BasicCamera(double cameraFrameWidth, double fieldOfView) {
		super(cameraFrameWidth, fieldOfView);
	}

	@Override
	public Optional<RawColor> shootRay(double cameraX, double cameraY) {

		Vector3D location = new Vector3D(cameraX, cameraY, 0.0);
		Vector3D direction = location.subtract(getEyeLocation()).normalize();

		Ray ray = localToWorld(new Ray(location, direction));
		List<Intersection<Shape>> intersections = World.getSingleton().getShapeIntersections(ray);

		return World.getSingleton().getLightingModel().determineRayColor(ray, intersections);

	}

}
