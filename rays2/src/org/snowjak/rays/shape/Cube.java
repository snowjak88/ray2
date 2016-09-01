package org.snowjak.rays.shape;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
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
	public List<Intersection<Shape>> getIntersections(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		List<Intersection<Shape>> results = planes.parallelStream()
				.map(p -> p.getIntersections(transformedRay))
				.flatMap(li -> li.parallelStream())
				.filter(i -> i.getPoint().getX() >= 0d && i.getPoint().getY() >= 0d && i.getPoint().getZ() >= 0d)
				.filter(i -> i.getPoint().getX() <= 1d && i.getPoint().getY() <= 1d && i.getPoint().getZ() <= 1d)
				.map(i -> new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this))
				.map(i -> localToWorld(i))
				.collect(LinkedList::new, LinkedList::add, LinkedList::addAll);

		return results;
	}

	@Override
	public Vector3D getLocation() {

		return localToWorld(new Vector3D(0.5, 0.5, 0.5));
	}

}
