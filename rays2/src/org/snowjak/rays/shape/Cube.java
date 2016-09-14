package org.snowjak.rays.shape;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Translation;

/**
 * Represents a cube, with edges aligned to the primary axes and opposite
 * corners at (-1,-1,-1) and (1,1,1)
 * 
 * @author snowjak88
 *
 */
public class Cube extends Shape {

	private Collection<Plane> planes;

	/**
	 * Create a new Cube of side-length 1, with edges aligned to the primary
	 * axes and opposite corners located at (-1,-1,-1) and (1,1,1)
	 */
	public Cube() {
		super();
		this.planes = new LinkedList<>();

		Plane p = new Plane();
		p.getTransformers().add(new Translation(0d, -1d, 0d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Translation(0d, 1d, 0d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Rotation(0d, 0d, -90d));
		p.getTransformers().add(new Translation(-1d, 0d, 0d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Rotation(0d, 0d, 90d));
		p.getTransformers().add(new Translation(1d, 0d, 0d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Rotation(90d, 0d, 0d));
		p.getTransformers().add(new Translation(0d, 0d, -1d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Rotation(-90d, 0d, 0d));
		p.getTransformers().add(new Translation(0d, 0d, 1d));
		planes.add(p);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		if (!isIntersectWithBoundingSphere(transformedRay, 3d))
			return Collections.emptyList();

		List<Intersection<Shape>> results = planes.parallelStream()
				.map(p -> p.getIntersectionsIncludingBehind(transformedRay))
				.flatMap(li -> li.parallelStream())
				.filter(i -> Double.compare(FastMath.abs(i.getPoint().getX()) - 1d, World.DOUBLE_ERROR) <= 0
						&& Double.compare(FastMath.abs(i.getPoint().getY()) - 1d, World.DOUBLE_ERROR) <= 0
						&& Double.compare(FastMath.abs(i.getPoint().getZ()) - 1d, World.DOUBLE_ERROR) <= 0)

				.map(i -> new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this,
						getDiffuseColorScheme(), getSpecularColorScheme(), getEmissiveColorScheme(), getMaterial(),
						getMaterial()))
				.map(i -> localToWorld(i))
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.collect(Collectors.toCollection(LinkedList::new));

		return results.parallelStream().map(i -> {
			if (i.getDistanceFromRayOrigin() == results.stream()
					.map(ii -> ii.getDistanceFromRayOrigin())
					.min(Double::compare)
					.get())
				return new Intersection<>(i.getPoint(), i.getNormal(), i.getRay(), i.getIntersected(),
						i.getDistanceFromRayOrigin(), i.getDiffuseColorScheme(), i.getSpecularColorScheme(),
						i.getEmissiveColorScheme(), Material.AIR, i.getEnteringMaterial());
			else
				return i;
		}).map(i -> {
			if (i.getDistanceFromRayOrigin() == results.stream()
					.map(ii -> ii.getDistanceFromRayOrigin())
					.max(Double::compare)
					.get())
				return new Intersection<>(i.getPoint(), i.getNormal(), i.getRay(), i.getIntersected(),
						i.getDistanceFromRayOrigin(), i.getDiffuseColorScheme(), i.getSpecularColorScheme(),
						i.getEmissiveColorScheme(), i.getLeavingMaterial(), Material.AIR);
			else
				return i;
		}).collect(Collectors.toCollection(LinkedList::new));
	}

	@Override
	public boolean isInside(Vector3D point) {

		Vector3D localPoint = worldToLocal(point);

		return (Double.compare(FastMath.abs(localPoint.getX()) - 1d, World.DOUBLE_ERROR) <= 0
				&& Double.compare(FastMath.abs(localPoint.getY()) - 1d, World.DOUBLE_ERROR) <= 0
				&& Double.compare(FastMath.abs(localPoint.getZ()) - 1d, World.DOUBLE_ERROR) <= 0);
	}

	@Override
	public Cube copy() {

		Cube newCube = new Cube();
		newCube = configureCopy(newCube);

		return newCube;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		Vector3D normal = localPoint.normalize();
		Pair<String, Double> biggestAxis = Arrays
				.asList(new Pair<>("x", normal.getX()), new Pair<>("y", normal.getY()), new Pair<>("z", normal.getZ()))
				.stream()
				.sorted((p1, p2) -> Double.compare(FastMath.abs(p1.getValue()), FastMath.abs(p2.getValue())))
				.findFirst()
				.get();

		switch (biggestAxis.getKey()) {
		case "x":
			normal = new Vector3D(normal.getX(), 0d, 0d).normalize();
			break;
		case "y":
			normal = new Vector3D(0d, normal.getY(), 0d).normalize();
			break;
		case "z":
			normal = new Vector3D(0d, 0d, normal.getZ()).normalize();
		}

		return normal;
	}

}
