package org.snowjak.rays.shape;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Translation;

/**
 * Represents a cube, with edges aligned to the primary axes and opposite
 * corners at (0,0,0) and (1,1,1)
 * 
 * @author rr247200
 *
 */
public class Cube extends Shape {

	private Collection<Plane> planes;

	/**
	 * Create a new Cube of side-length 1, with edges aligned to the primary
	 * axes and opposite corners located at (0,0,0) and (1,1,1)
	 */
	public Cube() {
		super();
		this.planes = new LinkedList<>();

		Plane p = new Plane();
		p.getTransformers().add(new Rotation(180d, 0d, 0d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Translation(0d, 1d, 0d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Rotation(0d, 0d, -90d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Rotation(0d, 0d, 90d));
		p.getTransformers().add(new Translation(1d, 0d, 0d));
		planes.add(p);

		p = new Plane();
		p.getTransformers().add(new Rotation(90d, 0d, 0d));
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

		List<Intersection<Shape>> results = planes.parallelStream()
				.map(p -> p.getIntersectionsIncludingBehind(transformedRay))
				.flatMap(li -> li.parallelStream())
				.filter(i -> Double.compare(i.getPoint().getX(), -World.DOUBLE_ERROR) >= 0
						&& Double.compare(i.getPoint().getY(), -World.DOUBLE_ERROR) >= 0
						&& Double.compare(i.getPoint().getZ(), -World.DOUBLE_ERROR) >= 0)

				.filter(i -> Double.compare(i.getPoint().getX() - 1d, World.DOUBLE_ERROR) <= 0
						&& Double.compare(i.getPoint().getY() - 1d, World.DOUBLE_ERROR) <= 0
						&& Double.compare(i.getPoint().getZ() - 1d, World.DOUBLE_ERROR) <= 0)

				.map(i -> new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this,
						getAmbientColorScheme(), getDiffuseColorScheme(), getSpecularColorScheme(),
						getEmissiveColorScheme()))
				.map(i -> localToWorld(i))
				.collect(Collectors.toCollection(LinkedList::new));

		return results;
	}

	@Override
	public boolean isInside(Vector3D point) {

		Vector3D localPoint = worldToLocal(point);

		return (Double.compare(localPoint.getX(), -World.DOUBLE_ERROR) >= 0
				&& Double.compare(localPoint.getY(), -World.DOUBLE_ERROR) >= 0
				&& Double.compare(localPoint.getZ(), -World.DOUBLE_ERROR) >= 0)
				&& (Double.compare(localPoint.getX() - 1d, World.DOUBLE_ERROR) <= 0
						&& Double.compare(localPoint.getY() - 1d, World.DOUBLE_ERROR) <= 0
						&& Double.compare(localPoint.getZ() - 1d, World.DOUBLE_ERROR) <= 0);
	}

	@Override
	public Vector3D getLocation() {

		return localToWorld(new Vector3D(0.5, 0.5, 0.5));
	}

	@Override
	public Cube copy() {

		Cube newCube = new Cube();
		newCube = configureCopy(newCube);

		return newCube;
	}

}
