package manatee.client.map.tile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import manatee.cache.definitions.Model;
import manatee.cache.definitions.loader.MeshLoader;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.texture.ITexture;
import manatee.cache.definitions.texture.Texture2D;
import manatee.client.gl.mesh.TileMeshData;
import manatee.client.scene.Assets;
import manatee.client.scene.GlobalAssets;

public class TileAssets extends Assets
{
	public static TileMeshData CUBE;
	
	public static Texture2D waterFoam;
	
	private Map<String, TileMeshData> tileMeshes = new HashMap<>();

	private String currentTileset = "forest";
	
	private final Logger logger = (Logger) LoggerFactory.getLogger("IO");
	
	private static final String TILE_RESOURCE_PATH = RESOURCE_PATH + "tile/";
	
	private Assets sceneAssets;
	
	public TileAssets(Assets sceneAssets)
	{
		this.sceneAssets = sceneAssets;
	}
	
	@Override
	public void loadAssets()
	{
		dispose();
		
		if (CUBE == null)
			CUBE = TileMeshData.create("global/mesh/cube.obj");
		
		waterFoam = Texture2D.load("texture/foam.png");
		
		loadTileset("global");
		loadTileset(currentTileset);
	}

	public void loadTileset(String name)
	{
		assert name != null : "Current tileset being loaded should not be null";
		
		Set<String> textureFiles, meshFiles;
		List<String> entTileAssets;
		
		try
		{
			textureFiles = getAllFiles(TILE_RESOURCE_PATH + name + "/tex");
			meshFiles = getAllFiles(TILE_RESOURCE_PATH + name + "/mesh");
			Set<String> entTileAssetsUnsorted = getAllFiles(TILE_RESOURCE_PATH + name + "/ent");
			
			entTileAssets = new ArrayList<>(entTileAssetsUnsorted);
			Collections.sort(entTileAssets);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
		
		TileMeshData tileMesh;
		Texture2D texture;
		Model model =
		null;
		
		for(String filePath : meshFiles)
		{
			String index = filePath.substring(0, filePath.lastIndexOf('.'));
			
			tileMesh = TileMeshData.create(name + "/mesh/" + filePath);
			tileMeshes.put(index, tileMesh);
		}
		
		for(String filePath : textureFiles)
		{
			String index = filePath.substring(0, filePath.lastIndexOf('.'));
			
			texture = Texture2D.load(TILE_RESOURCE_PATH + name + "/tex/" + filePath);
			textures.put(index, texture);
		}
		
		int texIndex = 0;

		for(String filePath : entTileAssets)
		{
			int periodIndex = filePath.lastIndexOf('.');
			String index = filePath.substring(0, periodIndex);
			
			// Kinda wasteful, but intended to be temporary until assets are lumped into wad like structures
			if (filePath.endsWith("png"))
			{
				
				
				texture = Texture2D.load(TILE_RESOURCE_PATH + name + "/ent/" + filePath);
				textures.put(index, texture);
				
				model.getTextures()[texIndex++] = texture;
			}
			else
			{
				IMesh[] meshArr = MeshLoader.loadModel(new File(TILE_RESOURCE_PATH + name + "/ent/" + filePath)).getMeshes();
				ITexture[] texArr = new ITexture[meshArr.length];
				
				for(int i = 0; i < meshArr.length; i++)
				{
					meshes.add(meshArr[i]);
					texArr[i] = GlobalAssets.NO_TEX;
				}
				
				model = new Model(meshArr, texArr);
				sceneAssets.getModels().put(index, model);
				texIndex = 0;
			}
		}
		
		/*
		 * 	loadModel("tree", new String[] {
				"tile/forest/ent/tree_foliage_0_0.obj", "tile/forest/ent/tree_foliage_0_1.obj", "tile/forest/ent/tree_foliage_0_2.obj", "tile/forest/ent/tree_foliage_0_3.obj"
		}, null);
		 */
	}
	
	private Set<String> getAllFiles(String dir) throws IOException {
	    try (Stream<Path> stream = Files.list(Paths.get(dir))) {
	        return stream
	  	          .filter(file -> !Files.isDirectory(file))
		          .map(Path::getFileName)
		          .map(Path::toString)
		          .collect(Collectors.toSet());
	    }
	    catch(NoSuchFileException e)
	    {
	    	logger.warn("Missing file/directory in tileset: " + dir);
	    	return Collections.emptySet();
	    }
	}

	@Override
	public void dispose()
	{
		super.dispose();
		
		textures.clear();
		
		if (waterFoam != null)
			waterFoam.dispose();
	}

	public String getCurrentTileset()
	{
		return currentTileset;
	}

	public void setCurrentTileset(String currentTileset)
	{
		this.currentTileset = currentTileset;
		
		loadAssets();
	}

	public TileMeshData getTileMesh(String name)
	{
		TileMeshData mesh = this.tileMeshes.get(name);
		
		return mesh == null ? CUBE : mesh;
	}
}
