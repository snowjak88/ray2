package org.snowjak.rays.shape.csg;

import java.util.HashSet;
import java.util.Set;

import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.ShapeBuilder;

/**
 * A convenient interface for building {@link Intersect} instances
 * 
 * @author snowjak88
 *
 */
public class IntersectBuilder extends ShapeBuilder<Intersect> {

	private Set<Shape> children = new HashSet<>();

	/**
	 * @return a new IntersectBuilder instance
	 */
	public IntersectBuilder builder() {

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
	public IntersectBuilder child(Shape child) {

		this.children.add(child);
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
