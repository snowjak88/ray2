package org.snowjak.rays.world;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
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

	private List<Shape> emissiveShapes = null;

	private long lastShapesHashWhenGeneratedEmissiveShapes = -1;

	private RawColor ambientRadiance = new RawColor();

	/**
	 * Create a new (empty) {@link World} instance.
	 */
	public World() {

	}

	/**
	 * Determine if the given {@code eyePoint} can see the given {@code point}
	 * -- i.e., if there are no Shapes between the two points. Ignore any Shapes
	 * included in {@code ignoreShapes}.
	 * 
	 * @param point
	 * @param eyePoint
	 * @param ignoreShapes
	 * @return <code>true</code> if the two points are not occluded from each
	 *         other by any Shape (not including those Shapes in
	 *         {@code ignoreShapes})
	 */
	public boolean isPointVisibleFromEye(Vector3D point, Vector3D eyePoint, Shape... ignoreShapes) {

		List<Shape> ignoreShapesList = Arrays.asList(ignoreShapes);
		List<Intersection<Shape>> occludingIntersections = RaytracerContext.getSingleton()
				.getCurrentWorld()
				.getShapeIntersections(new Ray(eyePoint, point.subtract(eyePoint)));

		double pointDistanceFromEye = eyePoint.distance(point);

		if (!occludingIntersections.isEmpty() && occludingIntersections.parallelStream()
				.filter(i -> !ignoreShapesList.contains(i.getIntersected()))
				.anyMatch(i -> Double.compare(i.getDistanceFromRayOrigin(), pointDistanceFromEye) < 0))
			return false;

		return true;
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
	 * @return the world's set of Shapes that are emissive
	 * @see Shape#isEmissive()
	 */
	public List<Shape> getEmissiveShapes() {

		if (emissiveShapes == null || lastShapesHashWhenGeneratedEmissiveShapes != shapes.hashCode()) {
			lastShapesHashWhenGeneratedEmissiveShapes = shapes.hashCode();
			emissiveShapes = shapes.parallelStream()
					.filter(s -> s.isEmissive())
					.collect(Collectors.toCollection(LinkedList::new));
		}

		return emissiveShapes;
	}

	/**
	 * Set the ambient radiance to use in this world
	 * 
	 * @param ambient
	 */
	public void setAmbientRadiance(RawColor ambient) {

		this.ambientRadiance = ambient;
	}

	/**
	 * @return the ambient radiance present in this world
	 */
	public RawColor getAmbientRadiance() {

		return ambientRadiance;
	}

}
