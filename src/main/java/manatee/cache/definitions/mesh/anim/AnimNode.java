package manatee.cache.definitions.mesh.anim;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

public class AnimNode
{
	public final String name;
	public final AnimNode parent;
	public final Matrix4f matrix;
	
	public List<AnimNode> children = new ArrayList<>();

	public AnimNode(String name, AnimNode parent, Matrix4f matrix)
	{
		this.name = name;
		this.parent = parent;
		this.matrix = matrix;
	}

	public void addChild(AnimNode child)
	{
		children.add(child);
	}

}
