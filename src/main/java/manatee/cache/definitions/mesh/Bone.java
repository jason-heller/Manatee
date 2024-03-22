package manatee.cache.definitions.mesh;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AINode;

public class Bone
{
	public String name;
	public Matrix4f inverseBindMatrix;
	public Matrix4f finalTransformation;
	//public int id;
	public AINode aiNode;
	public int id;
}