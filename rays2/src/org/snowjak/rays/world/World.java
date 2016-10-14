package org.snowjak.rays.world;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.snowjak.rays.Ray;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.shape.Shape;

/**
 * <p>
 * Represents the entire 3D context in which the raytracer operates.
 * </p>
 * <p>
 * A <strong>singleton</strong>, there will only be 1 instance of this object in
 * the JVM.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class World {

	/**
	 * Double values smaller than this will be considered to be "close enough"
	 * to zero.
	 */
	public static final double NEARLY_ZERO = 1e-10;

	/**
	 * The distance from (0,0,0) after which points will be said to be "too far
	 * away". Establishes "finite infinite distance".
	 */
	public static final double FAR_AWAY = 1e10;

	private Camera camera = null;

	private final List<Shape> shapes = new LinkedList<>();

	private final List<Light> lights = new LinkedList<>();

	/**
	 * Create a new (empty) {@link World} instance.
	 */
	public World() {

	}

	/**
	 * Check every single {@link Shape} in this world and return the closest
	 * resulting {@link Intersection} the given {@link Ray} produces.
	 * 
	 * @param ray
	 *            the ray to use, expressed in global coordinates
	 * @return the closest Intersection the given Ray produces, according to the
	 *         distance from the Ray's origin
	 */
	public Optional<Intersection<Shape>> getClosestShapeIntersection(Ray ray) {

		return getShapes().parallelStream()
				.map(s -> s.getIntersection(ray))
				.filter(oi -> oi.isPresent())
				.map(o -> o.get())
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.findFirst();
	}

	/**
	 * Check every single {@link Shape} in this world and return a list of every
	 * single {@link Intersection} the given {@link Ray} produces.
	 * 
	 * @param ray
	 *            the ray to use, expressed in global coordinates
	 * @return every single Intersection the given Ray produces, sorted by
	 *         distance from the Ray's origin
	 */
	public List<Intersection<Shape>> getShapeIntersections(Ray ray) {

		List<Intersection<Shape>> intersections = new LinkedList<>();
		for (Shape shape : getShapes())
			intersections.addAll(shape.getIntersections(ray));

		return intersections.stream()
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * @return the world's active {@link Camera}
	 */
	public Camera getCamera() {

		return camera;
	}

	/**
	 * Set the currently-active Camera.
	 * 
	 * @param camera
	 */
	public void setCamera(Camera camera) {

		this.camera = camera;
	}

	/**
	 * @return the world's current set of {@link Shape}s
	 */
	public List<Shape> getShapes() {

		return shapes;
	}

	/**
	 * @return the world's current set of {@link Light}s
	 */
	public List<Light> getLights() {

		return lights;
	}

}
