package org.snowjak.rays.shape;

import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for building Shape {@link Group}s
 * 
 * @author snowjak88
 *
 */
@HasName("group")
public class GroupBuilder extends ShapeBuilder<Group> {

	private List<Shape> children = new LinkedList<>();

	/**
	 * @return a new GroupBuilder instance
	 */
	public static GroupBuilder builder() {

		return new GroupBuilder();
	}

	protected GroupBuilder() {

	}

	/**
	 * Add a child Shape to this in-progress Group.
	 * 
	 * @param child
	 * @return this Builder, for method-chaining
	 */
	@HasName("child")
	public GroupBuilder child(Shape child) {

		this.children.add(child);
		return this;
	}

	@Override
	protected Group createNewShapeInstance() {

		return new Group();
	}

	@Override
	protected Group performTypeSpecificInitialization(Group newShapeInstance) {

		newShapeInstance.getChildren().addAll(children);

		return newShapeInstance;
	}

}
