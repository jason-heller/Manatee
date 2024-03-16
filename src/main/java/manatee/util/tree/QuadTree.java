package manatee.util.tree;

public class QuadTree<T> implements BroadPhase<T>
{
	private final QuadTreeNode<T> node;

	// define a quadtree extends as width and height, define quadtree depth.
	public QuadTree(final float size)
	{
		node = new QuadTreeNode<T>(0, 0, size, 4);
	}

	// insert a GameObject at the quadtree
	public void insert(T obj, SpatialCollider collider)
	{
		node.insert(obj, collider);
	}

	// clean the quadtree
	public void clean()
	{
		node.clean();
	}
}