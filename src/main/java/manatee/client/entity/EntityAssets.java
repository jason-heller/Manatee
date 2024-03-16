package manatee.client.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import manatee.cache.definitions.IAsset;
import manatee.client.scene.Assets;

public class EntityAssets extends Assets
{
	private final Logger logger = (Logger) LoggerFactory.getLogger("IO");
	
	private String currentTileset = "forest";
	
	@Override
	public void loadAssets()
	{
		reloadTilesets();
	}
	
	private void reloadTilesets()
	{
		disposeTexturesOnly();
		
		loadTileset("global");
		loadTileset(currentTileset);
	}

	public void loadTileset(String name)
	{
		assert name != null : "Current tileset being loaded should not be null";
		
		Set<String> textureFiles, meshFiles;
		
		try
		{
			textureFiles = getAllFiles(RESOURCE_PATH + name + "/ent");
			meshFiles = getAllFiles(RESOURCE_PATH + name + "/ent");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
		
		for(String filePath : meshFiles)
		{
			String index = filePath.substring(0, filePath.lastIndexOf('.'));
			
			loadMesh(index, name + "/mesh/" + filePath);
		}
		
		for(String filePath : textureFiles)
		{
			String index = filePath.substring(0, filePath.lastIndexOf('.'));
			
			loadTexture2D(index, name + "/tex/" + filePath);
		}
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
	}
	
	private void disposeTexturesOnly()
	{
		for(IAsset asset : textures.values())
		{
			if (asset != null)
				asset.dispose();
		}
	}

	public String getCurrentTileset()
	{
		return currentTileset;
	}

	public void setCurrentTileset(String currentTileset)
	{
		this.currentTileset = currentTileset;
		
		reloadTilesets();
	}
}
