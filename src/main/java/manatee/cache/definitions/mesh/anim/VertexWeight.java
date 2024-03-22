package manatee.cache.definitions.mesh.anim;

public class VertexWeight
{
	public int id, vertexId;
	public float weight;

	public VertexWeight(int id, int vertexId, float weight)
	{
		this.id = id;
		this.vertexId = vertexId;
		this.weight = weight;
	}

}
