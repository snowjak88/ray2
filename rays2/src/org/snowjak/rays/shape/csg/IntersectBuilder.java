package org.snowjak.rays.shape.csg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.ShapeBuilder;
import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for building {@link Intersect} instances
 * 
 * @author snowjak88
 *
 */
@HasName("intersect")
public class IntersectBuilder extends ShapeBuilder<Intersect> {

	private Set<Shape> children = new HashSet<>();

	/**
	 * @return a new IntersectBuilder instance
	 */
	public static IntersectBuilder builder() {

		return new IntersectBuilder();
	}

	protected IntersectBuilder() {
	}

	/**
	 * Add a child Shape to this in-progress {@link Intersect}.
	 * 
	 * @param child
	 * @return this Builder, for method-chaining
	 */
	@HasName("child")
	public IntersectBuilder child(Shape child) {

		this.children.add(child);
		return this;
	}

	/**
	 * Add a list of child Shapes to this in-progress {@link Intersect}.
	 * 
	 * @param children
	 * @return this Builder, for method-chaining
	 */
	@HasName("children")
	public IntersectBuilder children(List<Shape> children) {

		this.children.addAll(children);
		return this;
	}

	@Override
	protected Intersect createNewShapeInstance() {

		return new Intersect();
	}

	@Override
	protected Intersect performTypeSpecificInitialization(Intersect newShapeInstance) {

		newShapeInstance.getChildren().addAll(children);

		return newShapeInstance;
	}

}
