package manatee.maths.geom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import manatee.maths.Maths;

public class TriangleClipper
{
	private List<Triangle> triangles = new ArrayList<>();

	public List<Triangle> getTriangles()
	{
		return triangles;
	}
	
	public void addTriangle(Vector3f p1, Vector2f t1, Vector3f c1, Vector3f p2, Vector2f t2, Vector3f c2, Vector3f p3,
			Vector2f t3, Vector3f c3, Vector3f normal)
	{
		Vertex v1 = new Vertex(p1, t1, normal, c1);
		Vertex v2 = new Vertex(p2, t2, normal, c2);
		Vertex v3 = new Vertex(p3, t3, normal, c3);
		
		triangles.add(new Triangle(v1, v2, v3));
	}

	public void clip(float x, float y, float z, float nx, float ny, float nz)
	{
		clip(new Vector3f(x, y, z), new Vector3f(nx, ny, nz));
	}
	
	// Sutherland–Hodgman algorithm
	public void clip(Vector3f planeOrigin, Vector3f planeNormal)
	{
		Iterator<Triangle> iter = triangles.iterator();
		List<Triangle> newTriangles = new ArrayList<>();
		
		while(iter.hasNext())
		{
			Triangle t = iter.next();
			Vertex[] inputList = {
					t.v1, t.v2, t.v3
			};
			
			List<Vertex> vertices = new ArrayList<>();

			for(int i = 0; i < 3; i++)
			{
				Vertex currentVertex = inputList[i];
				Vertex prevVertex = inputList[Maths.mod(i - 1, 3)];
				
				Vertex intersectingVertex = vectorIntersectPlane(planeOrigin, planeNormal, prevVertex, currentVertex);
				
				float currentVertexDist = distanceFromPlane(planeOrigin, planeNormal, currentVertex.getPosition());
				float prevVertexDist = distanceFromPlane(planeOrigin, planeNormal, prevVertex.getPosition());
				
				if (currentVertexDist >= 0)
				{
					if (prevVertexDist < 0)
						vertices.add(intersectingVertex);
					
					vertices.add(currentVertex);
				}
				else if (prevVertexDist >= 0)
				{
					vertices.add(intersectingVertex);
				}
			}
			
			if (vertices.size() == 3)
			{
				newTriangles.add(new Triangle(vertices.get(0), vertices.get(1), vertices.get(2)));
			}
			else if (vertices.size() == 4)	// Working w/ tris, will never exceed 4
			{
				newTriangles.add(new Triangle(vertices.get(0), vertices.get(1), vertices.get(2)));
				newTriangles.add(new Triangle(vertices.get(2), vertices.get(3), vertices.get(0)));
			}
		}
		
		triangles = newTriangles;
	}

	private Vertex vectorIntersectPlane(Vector3f planePos, Vector3f planeNorm, Vertex lineStart, Vertex lineEnd)
	{
		Vector3f posStart = lineStart.getPosition();
		Vector3f posEnd = lineEnd.getPosition();

		Vector2f texStart = lineStart.getTexCoord();
		Vector2f texEnd = lineEnd.getTexCoord();

		Vector3f colStart = lineStart.getColor();
		Vector3f colEnd = lineEnd.getColor();

		float planeDot = planeNorm.dot(planePos);
		float startDot = posStart.dot(planeNorm);
		float endDot = posEnd.dot(planeNorm);
		float midpoint = (planeDot - startDot) / (endDot - startDot);

		Vector3f posIntersect = new Vector3f(posEnd).sub(posStart).mul(midpoint);
		Vector2f texIntersect = new Vector2f(texEnd).sub(texStart).mul(midpoint);
		Vector3f colIntersect = new Vector3f(colEnd).sub(colStart).mul(midpoint);
		
		Vector3f position = new Vector3f(posStart).add(posIntersect);
		Vector2f texCoord = new Vector2f(texStart).add(texIntersect);
		Vector3f color = new Vector3f(colStart).add(colIntersect);

		return new Vertex(position, texCoord, lineEnd.getNormal(), color);
	}

	private float distanceFromPlane(Vector3f planePos, Vector3f planeNorm, Vector3f vert)
	{
		float x = planeNorm.x * vert.x;
		float y = planeNorm.y * vert.y;
		float z = planeNorm.z * vert.z;

		return (x + y + z - (planeNorm.dot(planePos)));
	}
}
