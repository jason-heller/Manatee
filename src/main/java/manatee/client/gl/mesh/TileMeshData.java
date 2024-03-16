package manatee.client.gl.mesh;

import java.io.File;
import java.util.ArrayList;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVector3D.Buffer;

import manatee.cache.definitions.loader.MeshLoader;
import manatee.cache.definitions.loader.component.EmptyAIBuffer3D;
import manatee.cache.definitions.mesh.AssimpModel;
import manatee.client.map.MapGeometry;
import manatee.client.map.tile.TileFlags;
import manatee.client.scene.GameMap;
import manatee.maths.Maths;
import manatee.maths.Vectors;
import manatee.maths.geom.Plane;
import manatee.maths.geom.Triangle;
import manatee.maths.geom.TriangleClipper;
import manatee.maths.geom.Vertex;

public class TileMeshData extends AssimpModel
{
	public static TileMeshData EMPTY = new TileMeshData();
	
	public static final String TILE_RESOURCE_PATH = "src/main/resources/tile/";
	
	private static final float TRIPLANAR_TEX_SCALE = 2f;
	
	public static TileMeshData create(String file)
	{
		AssimpModel aiModel = MeshLoader.getAssimpMeshes(new File(TILE_RESOURCE_PATH + file));
		
		return new TileMeshData(aiModel.getMeshes(), aiModel.isTextured());
	}
	
	public TileMeshData() {}
	
	private TileMeshData(AIMesh[] meshes, boolean isTextured)
	{
		super(meshes, isTextured);
	}
	
	public void insert(ArrayList<Float> vertices, Vector3f origin, Quaternionf rotation, int flags, Plane[] clipPlanes, MapGeometry geom)
	{
		if (isMissing())
			return;
		
		if (TileFlags.SKIPS_MESHING.isSet(flags) && !GameMap.renderSkipped)
			return;

		if (TileFlags.ROTATE_90CW.isSet(flags))
			rotation.rotateZ(Maths.HALFPI);

		if (TileFlags.VARY_ZROTATION.isSet(flags))
			rotation.rotateZ((float)Math.random() * Maths.TWOPI);

		boolean flipX = TileFlags.FLIP_X.isSet(flags);
		boolean flipY = TileFlags.FLIP_Y.isSet(flags);
		
		Vector3f scale = new Vector3f(
				
				flipX ? -1f : 1f,
				flipY ? -1f : 1f,
				1f
		);
		
		boolean flipNormals = flipX ^ flipY;
		
		for(int m = 0; m < this.getMeshCount(); m++)
		{
			final Buffer pBuf = meshes[m].mVertices();
			final Buffer tBuf = isTextured() ? meshes[m].mTextureCoords(0) : EmptyAIBuffer3D.INSTANCE;
			final Buffer nBuf = meshes[m].mNormals();
		
			final org.lwjgl.assimp.AIColor4D.Buffer cBuf = meshes[m].mColors(0);

			for(int i = 0; i < meshes[m].mNumVertices(); i++)
			{
				if (clipPlanes != null)
				{
					Vector3f p1 = getPos(i, pBuf, origin, rotation, scale);
					Vector2f t1 = getTex(i, tBuf);
					Vector3f c1 = getColor(i, cBuf);
					i++;
					
					Vector3f p2 = getPos(i, pBuf, origin, rotation, scale);
					Vector2f t2 = getTex(i, tBuf);
					Vector3f c2 = getColor(i, cBuf);
					i++;
					
					Vector3f p3 = getPos(i, pBuf, origin, rotation, scale);
					Vector2f t3 = getTex(i, tBuf);
					Vector3f c3 = getColor(i, cBuf);
					Vector3f normal = getNormal(i, nBuf, rotation, flipX, flipY);
					
					TriangleClipper tc = new TriangleClipper();
					tc.addTriangle(p1, t1, c1, p2, t2, c2, p3, t3, c3, normal);
					
					for(Plane plane : clipPlanes)
						tc.clip(plane.getOrigin(), plane.getNormal());
					
					for(Triangle tri : tc.getTriangles())
					{
						Vertex v1 = tri.v1, v2 = tri.v2, v3 = tri.v3;
						
						addVertex(vertices, v1.getPosition(), v1.getTexCoord(), normal, v1.getColor(), geom, flags);
						if (flipNormals)
						{
							addVertex(vertices, v3.getPosition(), v3.getTexCoord(), normal, v3.getColor(), geom, flags);
							addVertex(vertices, v2.getPosition(), v2.getTexCoord(), normal, v2.getColor(), geom, flags);
						}
						else
						{
							addVertex(vertices, v2.getPosition(), v2.getTexCoord(), normal, v2.getColor(), geom, flags);
							addVertex(vertices, v3.getPosition(), v3.getTexCoord(), normal, v3.getColor(), geom, flags);
						}
					}
				}
				else
				{

					addVertex(vertices, getPos(i, pBuf, origin, rotation, scale), getTex(i, tBuf),
							getNormal(i, nBuf, rotation, flipX, flipY), getColor(i, cBuf), geom, flags);
					i++;

					if (flipNormals)
					{
						addVertex(vertices, getPos(i+1, pBuf, origin, rotation, scale), getTex(i+1, tBuf),
								getNormal(i+1, nBuf, rotation, flipX, flipY), getColor(i+1, cBuf), geom, flags);

						addVertex(vertices, getPos(i, pBuf, origin, rotation, scale), getTex(i, tBuf),
								getNormal(i, nBuf, rotation, flipX, flipY), getColor(i, cBuf), geom, flags);
						
						++i;
					}
					else
					{
						addVertex(vertices, getPos(i, pBuf, origin, rotation, scale), getTex(i, tBuf),
								getNormal(i, nBuf, rotation, flipX, flipY), getColor(i, cBuf), geom, flags);
						i++;

						addVertex(vertices, getPos(i, pBuf, origin, rotation, scale), getTex(i, tBuf),
								getNormal(i, nBuf, rotation, flipX, flipY), getColor(i, cBuf), geom, flags);
					}
				}
			}
		}
	}
	
	private Vector3f getPos(int i, Buffer positionBuffer, Vector3f translation, Quaternionf rotation, Vector3f scale)
	{
		AIVector3D aiPos = positionBuffer.get(i);
		
		Vector3f position = new Vector3f(aiPos.x(), aiPos.y(), aiPos.z());
		
		position.mul(scale);
		rotation.transform(position);
		position.add(translation);
		
		return position;
	}
	
	private Vector2f getTex(int i, Buffer textureBuffer)
	{

		AIVector3D texCoord = textureBuffer.get(i);
		
		return new Vector2f(texCoord.x(), texCoord.y());
	}
	
	private Vector3f getNormal(int i, Buffer normalBuffer, Quaternionf rotation, boolean flipX, boolean flipY)
	{
		AIVector3D aiNor = normalBuffer.get(i);
		
		float x = flipX ? -aiNor.x() : aiNor.x();
		float y = flipY ? -aiNor.y() : aiNor.y();
		
		Vector3f normal =  new Vector3f(x, y, aiNor.z());
		rotation.transform(normal);
		
		return normal;
	}
	
	private Vector3f getColor(int i, org.lwjgl.assimp.AIColor4D.Buffer colorBuffer)
	{
		if (colorBuffer != null)
		{
			AIColor4D color = colorBuffer.get(i);
			return new Vector3f(color.r(), color.g(), color.b());
		}
		
		return Vectors.ONE;
	}
	
	private void addVertex(ArrayList<Float> vertices, Vector3f position, Vector2f texCoord, Vector3f normal, Vector3f color, MapGeometry geom, int flags)
	{
		float x = position.x();
		float y = position.y();
		float z = position.z();

		boolean terrainProjection = TileFlags.PROJECT_ONTO_TERRAIN.isSet(flags);
		
		if (terrainProjection && geom != null)
		{
			float sample = geom.getHeightAt(position.x, position.y);//region.getHeightData().getInterpolated(position.x, position.y);
			z += sample;
		}
		
		vertices.add(x);
		vertices.add(y);
		vertices.add(z);
		
		if (TileFlags.TRIPLANAR_TEXTURE_MAPPING.isSet(flags))
		{
			float absX = Math.abs(normal.x);
			float absY = Math.abs(normal.y);
			float absZ = Math.abs(normal.z);
			
			if (absX > absY && absX > absZ)
			{
				vertices.add(y / TRIPLANAR_TEX_SCALE);
				vertices.add(z / TRIPLANAR_TEX_SCALE);
			}
			else if (absY > absX && absY > absZ)
			{
				vertices.add(x / TRIPLANAR_TEX_SCALE);
				vertices.add(1f - (z / TRIPLANAR_TEX_SCALE));
			}
			else
			{
				vertices.add(x / TRIPLANAR_TEX_SCALE);
				vertices.add(y / TRIPLANAR_TEX_SCALE);
			}
		}
		else
		{
			vertices.add(texCoord.x());
			vertices.add(texCoord.y());
		}

		vertices.add(normal.x());
		vertices.add(normal.y());
		vertices.add(normal.z());

		vertices.add(color.x());
		vertices.add(color.y());
		vertices.add(color.z());
	}
}
