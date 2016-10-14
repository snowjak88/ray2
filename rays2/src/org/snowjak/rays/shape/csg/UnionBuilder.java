package org.snowjak.rays.shape.csg;

import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.ShapeBuilder;
import org.snowjak.rays.world.HasName;

/**
 * A convenient interface for building {@link Union}s.
 * 
 * @author snowjak88
 *
 */
@HasName("union")
public class UnionBuilder extends ShapeBuilder<Union> {

	private List<Shape> children = new LinkedList<>();

	/**
	 * @return a new UnionBuilder instance
	 */
	public static UnionBuilder builder() {

		return new UnionBuilder();
	}

	protected UnionBuilder() {

	}

	/**
	 * Add a child Shape to this in-progress Union.
	 * 
	 * @param child
	 * @return this Builder, for method-chaining
	 */
	@HasName("child")
	public UnionBuilder child(Shape child) {

		children.add(child);
		return this;
	}

	/**
	 * Add a list of child Shapes to this in-progrss Union.
	 * 
	 * @param children
	 * @return this Builder, for method-chaining
	 */
	@HasName("children")
	public UnionBuilder child(List<Shape> children) {

		this.children.addAll(children);
		return this;
	}

	@Override
	protected Union createNewShapeInstance() {

		return new Union(new LinkedList<>());
	}

	@Override
	protected Union performTypeSpecificInitialization(Union newShapeInstance) {

		newShapeInstance.getChildren().addAll(children);

		return newShapeInstance;
	}

}
