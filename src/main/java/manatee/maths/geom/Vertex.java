package manatee.maths.geom;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex
{
	private Vector3f position;
	private Vector2f texCoord;
	private Vector3f normal;
	private Vector3f color;
	
	public Vertex(Vector3f position, Vector2f texCoord, Vector3f normal, Vector3f color)
	{
		this.position = position;
		this.texCoord = texCoord;
		this.normal = normal;
		this.color = color;
	}
	
	public Vertex(Vector3f position)
	{
		this.position = position;
	}

	public Vertex(Vertex v)
	{
		this.setPosition(v.getPosition());
		this.setTexCoord(v.getTexCoord());
		this.setNormal(v.getNormal());
		this.setColor(v.getColor());
	}

	public Vector3f getPosition()
	{
		return position;
	}

	public Vector2f getTexCoord()
	{
		return texCoord;
	}

	public Vector3f getNormal()
	{
		return normal;
	}

	public Vector3f getColor()
	{
		return color;
	}

	public void setColor(Vector3f color)
	{
		this.color = color;
	}

	public void setPosition(Vector3f position)
	{
		this.position = position;
	}

	public void setTexCoord(Vector2f texCoord)
	{
		this.texCoord = texCoord;
	}

	public void setNormal(Vector3f normal)
	{
		this.normal = normal;
	}
	
	
}
