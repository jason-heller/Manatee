package manatee.util.tree;

import java.util.ArrayList;

public class QuadTreeNode<T> {
	private final int currDepth; // the current depth of this node
	private final float[] center; // the center of this node
	private final QuadTreeNode<T>[] nodes; // the child nodes
	
	private final ArrayList<T> objects; // the objects stored at this node
	
	public QuadTreeNode(float centerX, float centerY, float halfWidth, int stopDepth) {
		this.currDepth = stopDepth;
		
		// set Vector3f to current x-y-z values
		this.center = new float[] {centerX, centerY, 0.0f};
		
		this.objects = new ArrayList<T>();
		
		float offsetX = 0.0f;
		float offsetY = 0.0f;
		
		if (stopDepth > 0) {
			// create 4 child nodes as long as depth is still greater than 0
			this.nodes = new QuadTreeNode[4];
			
			// halve child nodes size
			float step = halfWidth * 0.5f;
			
			// loop through and create new child nodes
			for (int i = 0; i < 4; i++) {
				
				// compute the offsets of the child nodes
				offsetX = (((i & 1) == 0) ? step : -step);
				offsetY = (((i & 2) == 0) ? step : -step);
				
				nodes[i] = new QuadTreeNode<T>(centerX + offsetX, centerY + offsetY, step, stopDepth - 1);
			}	
		}
		else {
			this.nodes = null;
		}
	}
	
	public void insert(final T obj, final SpatialCollider collider) {
		int index = 0; // get child node index as 0 initially
		boolean straddle = false; // set straddling to false
		float delta;
		
		// get the raw arrays, makes it easier to run these in a loop
		final float[] objPos = collider.getCenter();
		final float[] nodePos = center;
		
		for (int i = 0; i < 2; i++) {
			// compute the delta, nodePos Vector3f index - objPos Vector3f
			delta = nodePos[i] - objPos[i];
			
			// if the absolute of delta is less than or equal to radius object straddling, break
			if (Math.abs(delta) <= collider.getBounds()[i]) {
				straddle = true;
				break;
			}
			
			// compute the index to isnert to child node
			if (delta > 0.0f) {
				index |= (1 << i);
			}
		}
		
		if (!straddle && currDepth > 0) {
			// not straddling, insert to child at index
			nodes[index].insert(obj, collider);
		}
		else {
			// straddling, insert to this node
			objects.add(obj);
		}
	}
	
	public void clean() {
		objects.clear();
		
		// clean children if available
		if (currDepth > 0) {
			for (int i = 0; i < 4; i++) {
				nodes[i].clean();
			}
		}
	}
}