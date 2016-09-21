package org.snowjak.rays.shape;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;

/**
 * Represents a cube, with edges aligned to the primary axes and opposite
 * corners at (-1,-1,-1) and (1,1,1)
 * 
 * @author snowjak88
 *
 */
public class Cube extends Shape {

	/**
	 * Create a new Cube of side-length 1, with edges aligned to the primary
	 * axes and opposite corners located at (-1,-1,-1) and (1,1,1)
	 */
	public Cube() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin) {

		Ray transformedRay = worldToLocal(ray);

		boolean ignoreX = false, ignoreY = false, ignoreZ = false;
		double px = transformedRay.getOrigin().getX(), py = transformedRay.getOrigin().getY(),
				pz = transformedRay.getOrigin().getZ();
		double vx = transformedRay.getVector().getX(), vy = transformedRay.getVector().getY(),
				vz = transformedRay.getVector().getZ();

		if (Double.compare(vx, 0d) == 0)
			ignoreX = true;
		if (Double.compare(vy, 0d) == 0)
			ignoreY = true;
		if (Double.compare(vz, 0d) == 0)
			ignoreZ = true;

		double t_x0 = (ignoreX ? -1 : solveForT(px, vx, -1)), t_y0 = (ignoreY ? -1 : solveForT(py, vy, -1)),
				t_z0 = (ignoreZ ? -1 : solveForT(pz, vz, -1));
		Vector3D n_x0 = Vector3D.MINUS_I, n_y0 = Vector3D.MINUS_J, n_z0 = Vector3D.MINUS_K;
		double t_x1 = (ignoreX ? Double.MAX_VALUE : solveForT(px, vx, +1)),
				t_y1 = (ignoreY ? Double.MAX_VALUE : solveForT(py, vy, +1)),
				t_z1 = (ignoreZ ? Double.MAX_VALUE : solveForT(pz, vz, +1));
		Vector3D n_x1 = Vector3D.PLUS_I, n_y1 = Vector3D.PLUS_J, n_z1 = Vector3D.PLUS_K;

		if (t_x0 > t_x1) {
			double temp = t_x0;
			t_x0 = t_x1;
			t_x1 = temp;

			Vector3D tempN = n_x0;
			n_x0 = n_x1;
			n_x1 = tempN;
		}
		if (t_y0 > t_y1) {
			double temp = t_y0;
			t_y0 = t_y1;
			t_y1 = temp;

			Vector3D tempN = n_y0;
			n_y0 = n_y1;
			n_y1 = tempN;
		}
		if (t_z0 > t_z1) {
			double temp = t_z0;
			t_z0 = t_z1;
			t_z1 = temp;

			Vector3D tempN = n_z0;
			n_z0 = n_z1;
			n_z1 = tempN;
		}

		double t0 = 0d;
		Vector3D n0 = null;
		if (t_x0 >= t_y0 && t_x0 >= t_z0) {
			t0 = t_x0;
			n0 = n_x0;
		} else if (t_y0 >= t_x0 && t_y0 >= t_z0) {
			t0 = t_y0;
			n0 = n_y0;
		} else if (t_z0 >= t_x0 && t_z0 >= t_y0) {
			t0 = t_z0;
			n0 = n_z0;
		}

		double t1 = 0d;
		Vector3D n1 = null;
		if (t_x1 <= t_y1 && t_x1 <= t_z1) {
			t1 = t_x1;
			n1 = n_x1;
		} else if (t_y1 <= t_x1 && t_y1 <= t_z1) {
			t1 = t_y1;
			n1 = n_y1;
		} else if (t_z1 <= t_x1 && t_z1 <= t_y1) {
			t1 = t_z1;
			n1 = n_z1;
		}

		boolean useT0 = includeBehindRayOrigin || Double.compare(t0, World.DOUBLE_ERROR) >= 0,
				useT1 = includeBehindRayOrigin || Double.compare(t1, World.DOUBLE_ERROR) >= 0;

		List<Intersection<Shape>> results = new LinkedList<>();
		if (useT0) {
			Vector3D intersectionPoint = transformedRay.getOrigin().add(transformedRay.getVector().scalarMultiply(t0));
			if (Double.compare(FastMath.abs(intersectionPoint.getX()) - 1d, World.DOUBLE_ERROR) <= 0
					&& Double.compare(FastMath.abs(intersectionPoint.getY()) - 1d, World.DOUBLE_ERROR) <= 0
					&& Double.compare(FastMath.abs(intersectionPoint.getZ()) - 1d, World.DOUBLE_ERROR) <= 0) {
				Vector3D normal = n0;
				double normalSign = FastMath.signum(normal.negate().dotProduct(transformedRay.getVector()));
				normal = normal.scalarMultiply(normalSign);
				Material leavingMaterial = (useT1) ? Material.AIR : getMaterial(),
						enteringMaterial = (useT1) ? getMaterial() : Material.AIR;
				results.add(localToWorld(new Intersection<>(intersectionPoint, normal, transformedRay, this, t0,
						getDiffuseColorScheme(), getSpecularColorScheme(), getEmissiveColorScheme(), leavingMaterial,
						enteringMaterial)));
			}
		}
		if (useT1) {
			Vector3D intersectionPoint = transformedRay.getOrigin().add(transformedRay.getVector().scalarMultiply(t1));
			if (Double.compare(FastMath.abs(intersectionPoint.getX()) - 1d, World.DOUBLE_ERROR) <= 0
					&& Double.compare(FastMath.abs(intersectionPoint.getY()) - 1d, World.DOUBLE_ERROR) <= 0
					&& Double.compare(FastMath.abs(intersectionPoint.getZ()) - 1d, World.DOUBLE_ERROR) <= 0) {
				Vector3D normal = n1;
				double normalSign = FastMath.signum(normal.negate().dotProduct(transformedRay.getVector()));
				normal = normal.scalarMultiply(normalSign);
				results.add(localToWorld(
						new Intersection<>(intersectionPoint, normal, transformedRay, this, t1, getDiffuseColorScheme(),
								getSpecularColorScheme(), getEmissiveColorScheme(), getMaterial(), Material.AIR)));
			}
		}

		return results;
	}

	/**
	 * <p>
	 * P + Vt = solution
	 * </p>
	 * <p>
	 * therefore:
	 * </p>
	 * <p>
	 * t = (solution - P) / V
	 * </p>
	 * 
	 * @param p
	 * @param v
	 * @param sol
	 * @return
	 */
	private double solveForT(double p, double v, double sol) {

		return (sol - p) / v;
	}

	@Override
	public boolean isInside(Vector3D point) {

		return isInsideLocal(worldToLocal(point));
	}

	private boolean isInsideLocal(Vector3D localPoint) {

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
		return Arrays
				.asList(new Pair<>(Vector3D.PLUS_I.scalarMultiply(FastMath.signum(normal.getX())), normal.getX()),
						new Pair<>(Vector3D.PLUS_J.scalarMultiply(FastMath.signum(normal.getY())), normal.getY()),
						new Pair<>(Vector3D.PLUS_K.scalarMultiply(FastMath.signum(normal.getZ())), normal.getZ()))
				.stream()
				.sorted((p1, p2) -> Double.compare(FastMath.abs(p1.getValue()), FastMath.abs(p2.getValue())))
				.findFirst()
				.get()
				.getKey();
	}

}
