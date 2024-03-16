package manatee.util.tree;

public interface BroadPhase<T>
{
	public void insert(final T obj, SpatialCollider collider);

	public void clean();
}