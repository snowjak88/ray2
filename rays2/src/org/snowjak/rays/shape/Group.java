package org.snowjak.rays.shape;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;

/**
 * Allows you to group several Shapes together under a single set of
 * transformations.
 * 
 * @author snowjak88
 *
 */
public class Group extends Shape {

	private Collection<Shape> children = new LinkedList<>();

	/**
	 * Create a new (empty) Group
	 */
	public Group() {
		this(Collections.emptyList());
	}

	/**
	 * Create a Group with a given set of children.
	 * 
	 * @param children
	 */
	public Group(Shape... children) {
		this(Arrays.asList(children));
	}

	/**
	 * Create a Group with a given set of children.
	 * 
	 * @param children
	 */
	public Group(Collection<Shape> children) {
		super();
		this.children.addAll(children);
	}

	/**
	 * Get this Group's set of children.
	 * 
	 * @return this Group's children
	 */
	public Collection<Shape> getChildren() {

		return children;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		List<Intersection<Shape>> results = new LinkedList<>();
		for (Shape child : children)
			results.addAll(child.getIntersectionsIncludingBehind(transformedRay));

		results = results.stream().map(i -> localToWorld(i)).collect(LinkedList::new, LinkedList::add,
				LinkedList::addAll);

		return results;
	}

	@Override
	public Group copy() {

		Group newGroup = new Group(
				this.getChildren().stream().map(s -> s.copy()).collect(Collectors.toCollection(LinkedList::new)));
		newGroup = configureCopy(newGroup);

		return newGroup;
	}

}
