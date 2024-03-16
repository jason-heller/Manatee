package manatee.client.gl.mesh;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import manatee.cache.definitions.MeshUtil;
import manatee.cache.definitions.field.DataFieldi;
import manatee.cache.definitions.mesh.BaseMesh;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.map.HeightQuery;
import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;
import manatee.client.map.tile.Tile;
import manatee.client.map.tile.TileFlags;
import manatee.client.map.tile.TileUpdate;
import manatee.client.map.tile.Tilemap;
import manatee.maths.geom.Plane;

public class TileFieldMesh extends BaseMesh
{
	protected int vertexCount;
	private TileShaderTarget shaderTarget = TileShaderTarget.GENERIC;
	private ITexture texture;

	// TODO: Use the TileUpdate object (when not null) to see which meshes are
	// affected, and return a mesh array equal to the origin TileFIeld's meshes
	// array, but with the one or two meshes changed updated
	public static Tile build(MapGeometry geom, MapRegion region, TileUpdate updatedTile)
	{
		final TileVertexMap vertexMap = new TileVertexMap();
		
		final int xResolution = region.getXResolution() - 1;
		final int yResolution = region.getYResolution() - 1;
		final int spacing = region.getSpacing();
		final int px = region.getPosition().x;
		final int py = region.getPosition().y;
		
		//DataFieldf heights = region.getHeightData();
		DataFieldi tiles = region.getTileData();
		
		Tilemap tilemap = geom.getTilemap();
		
		float offset = spacing / 2f;
		
		int wsTileX, wsTileY;		// Position of tile in world space
		
		int xScaled, yScaled;

		boolean fullMeshUpdate = updatedTile == null;
		
		Tile resultTile = (fullMeshUpdate) ? null : updatedTile.newTile;

		// Iterate thru all tiles, add meshes
		for(int x = 0; x < xResolution; x++)
		{
			xScaled = (x * spacing);
			
			for(int y = 0; y < yResolution; y++)
			{
				yScaled = (y * spacing);
				
				wsTileX = xScaled + px;
				wsTileY = yScaled + py;
				
				boolean isTheUpdatedTile = !fullMeshUpdate && updatedTile.x == wsTileX && updatedTile.y == wsTileY;
				
				int type, id;
				Tile tile;
				
				if (isTheUpdatedTile)
				{
					tile = updatedTile.newTile;
				}
				else
				{
					int tilemapIndex = tiles.getLocal(x, y);
					tile = tilemap.get(tilemapIndex);
				}

				id  = tile.getTilemapIndex();
				type = id & 0x00FFFFFF;
				
				// Check if we should transmogrify the tile
				//boolean illegal = tile.isIllegal();
				
				if (tile.isConnectable()/* && (!illegal || (illegal && !isTheUpdatedTile))*/)
				{
					// Reset id
					/*if (illegal)
					{
						do
						{
							--type;
						} while(TileFlags.ILLEGAL.isSet(tilemap.get(type).getFlags()));
					}*/

					boolean hasTrailRHS = tile.hasTrailerOnRight();
					boolean hasTrailLHS = tile.hasTrailerOnLeft();
					int nCorners = tile.getNumCorners();
					
					int neighborCase = 0;	
					
					// int scope = 2 + (hasTrailRHS ? 2 : 0) + nCorners;
					
					int left = x == 0 ?
							  geom.getTileTypeAt(wsTileX - spacing, wsTileY)
							: tiles.getLocal(x - 1, y) & 0x00FFFFFF;
					
					if (left - type == 0)
						neighborCase |= 1;
					
					int top = y == 0 ?
							  geom.getTileTypeAt(wsTileX, wsTileY - spacing)
							: tiles.getLocal(x, y - 1) & 0x00FFFFFF;
					
					if (top - type == 0)
						neighborCase |= 2;
					
					int right = x + 1 == xResolution ?
							  geom.getTileTypeAt(wsTileX + spacing, wsTileY)
							: tiles.getLocal(x + 1, y) & 0x00FFFFFF;
					
					if (right - type == 0)
						neighborCase |= 4;
					
					int bottom = y + 1 == yResolution ?
							  geom.getTileTypeAt(wsTileX, wsTileY + spacing)
							: tiles.getLocal(x, y + 1) & 0x00FFFFFF;
					
					if (bottom - type == 0)
						neighborCase |= 8;

					int subId = determineConnectorCase(neighborCase, hasTrailRHS, hasTrailLHS, nCorners);
					tile = tilemap.get(type, subId);
		
					if (isTheUpdatedTile)
						resultTile = tile;
					
					tiles.setLocal(tile.getTilemapIndex(), x, y);
				}
				
				HeightQuery query = geom.queryHeightAt(wsTileX, wsTileY);

				boolean ignoreHeightField = TileFlags.IGNORE_HEIGHTFIELD.isSet(tile.getFlags());
				
				float height = ignoreHeightField ? 0f : query.getHeight();
				
				Vector3f origin = new Vector3f(wsTileX + offset, wsTileY + offset, height);
				
				Quaternionf rotation = query.getRotation();
				
				TileShaderTarget shaderTarget = tile.getShaderTarget();//.fromFlags(tile.getFlags());
				ITexture tex = tile.getTexture();
				
				if (tile != Tile.AIR)
				{
					vertexMap.initIfEmpty(shaderTarget, tex);
					
					ArrayList<Float> vertices = vertexMap.get(shaderTarget, tex);

					insertTile(vertices, tile, geom, wsTileX, wsTileY, origin, rotation, spacing);
				}
			}
		}
		
		final int nKeys = vertexMap.keyCount();
		
		TileFieldMesh[] meshes = new TileFieldMesh[nKeys];
		
		int insertPos = 0;
		for(TileShaderTarget target : vertexMap.primaryKeySet())
		{
			for(ITexture tex : vertexMap.secondaryKeySet(target))
			{
				List<Float> vertices = vertexMap.get(target, tex);
				
				int len = vertices.size();
				float[] positions = new float[len];
				for(int i = 0; i < len; i++)
					positions[i] = vertices.get(i);
				
				final int vertexCount = vertices.size() / 11;
				
				meshes[insertPos++] = new TileFieldMesh(positions, vertexCount, target, tex);
			}
		}
		
		region.setTileMeshes(meshes);
		
		return resultTile;
	}
	
	public static TileFieldMesh build(MapGeometry geom, Tile tile)
	{
		ArrayList<Float> vertices = new ArrayList<>();

		insertTile(vertices, tile, geom, 0, 0, new Vector3f(0.5f, 0, 0.5f), new Quaternionf(), 1);

		int len = vertices.size();
		float[] positions = new float[len];
		for (int i = 0; i < len; i++)
			positions[i] = vertices.get(i);

		final int vertexCount = vertices.size() / 11;

		return new TileFieldMesh(positions, vertexCount, TileShaderTarget.GENERIC, tile.getTexture());
	}
	
	public static void insertTile(ArrayList<Float> vertices, Tile tile, MapGeometry geom, int wsTileX, int wsTileY,
			Vector3f origin, Quaternionf rotation, int res)
	{
		TileMeshData meshData = tile.getTileMeshData();
		
		Plane[] clipPlanes = null;
		
		int planeX = wsTileX;
		int planeY = wsTileY;
		
		int flags = tile.getFlags();
		
		if (TileFlags.PROJECT_ONTO_TERRAIN.isSet(flags))
		{
			origin.z = 0;
		}
		
		if ((flags & TileFlags.CLIP_XY) != 0)
		{
			clipPlanes = new Plane[(flags & TileFlags.CLIP_XY) == TileFlags.CLIP_XY ? 4 : 2];
			int c = 0;
			
			if (TileFlags.CLIP_X.isSet(flags))
			{
				clipPlanes[c++] = new Plane(planeX, planeY, 0, 0.5f, 0, 0);
				clipPlanes[c++] = new Plane(planeX + res, planeY + res, 0, -0.5f, 0, 0);
				
				Matrix4f rotationMatrix = new Matrix4f().rotation(rotation);

				// Remove Y rotation
				rotationMatrix.m10(0f);
				rotationMatrix.m11(0f);
				rotationMatrix.m12(0f);
				
				rotationMatrix.m01(0f);
				rotationMatrix.m21(0f);
				
		        rotation.setFromNormalized(rotationMatrix);
				
			}
			
			if (TileFlags.CLIP_Y.isSet(flags))
			{
				clipPlanes[c++] = new Plane(planeX, planeY, 0, 0, 0.5f, 0);
				clipPlanes[c++] = new Plane(planeX + res, planeY + res, 0, 0, -0.5f, 0);
				
				Matrix4f rotationMatrix = new Matrix4f().rotation(rotation);

				// Remove X rotation
				rotationMatrix.m00(0f);
				rotationMatrix.m01(0f);
				rotationMatrix.m02(0f);
				
				rotationMatrix.m10(0f);
				rotationMatrix.m20(0f);
				
		        rotation.setFromNormalized(rotationMatrix);
			}
		}
		
		// MapRegion region = geom == null ? null : geom.getRegionAt(origin.x, origin.y);

		meshData.insert(vertices, origin, rotation, flags, clipPlanes, geom);
	}
	
	private static int determineConnectorCase(int neighborCase, boolean hasTrail, boolean trailOnBothEnds, int nCorners)
	{
		int vert = hasTrail ? 2  : 1;
		int nwCornerOffset = hasTrail ? 4 : 2;
		int cornerMultiplier = nCorners == 1 ? 0 : 1;
		
		// NW, NE, SW, SE

		switch (neighborCase)
		{
		case 0:
			return hasTrail ? 1 : 0;//(nCorners > 0 ? 2 : 0);
			
		case 1: // One tile on left, this is a possible trailer
			return hasTrail ? 1 : 0;//(nCorners > 0 ? 2 : 0);

		case 2: // One tile on top, this is a possible trailer
			return hasTrail ? 3 : vert;//(nCorners > 0 ? 2 : 0);
			
		case 8: // One tile on bottom,
			return trailOnBothEnds ? 3 : vert;//(nCorners == 1 ? 2 : vert);
			
		case 10: // Tile is vert
		case 11: // top left bottom .. vert?
		case 14: // bottom right top .. vert?
			return vert;

		case 3: // Tile is a SE corner
			return nwCornerOffset + (1 * cornerMultiplier);

		case 6: // Tile is SW corner
			return nwCornerOffset;
			

		case 9: // Tile is a NE corner
			return nwCornerOffset + (3 * cornerMultiplier);

		case 12: // Tile is a NW corner
			return nwCornerOffset + (2 * cornerMultiplier);

		case 4: // One tile on right (horz)
			return trailOnBothEnds ? 1 : 0;//(nCorners == 1 ? 2 : 0);
			
		case 5: // Tile is horz
		case 7: // Left,Top,Right
		case 13: // Bottom,right,left
		case 15: // fully surrounded
			return 0;
		
		default: // or out of bounds.
		}

		return -1;
	}

	private TileFieldMesh(float[] positions, int vertexCount, TileShaderTarget shaderTarget, ITexture texture)
	{
		this.vertexCount = vertexCount;
		this.shaderTarget = shaderTarget;
		this.texture = texture;
		
		load(positions);
	}

	@Override
	public void bind()
	{
		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
	}
	
	@Override
	public void unbind()
	{
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
		glBindVertexArray(0);
	}
	
	@Override
	protected void setAttribPointers()
	{
		MeshUtil.attribInterlacedf(3, 2, 3, 3);
	}

	@Override
	public int getVertexCount()
	{
		return vertexCount;
	}

	public ITexture getTexture()
	{
		return texture;
	}

	public TileShaderTarget getShaderTarget()
	{
		return shaderTarget;
	}
}
