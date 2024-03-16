package manatee.cache.definitions.binfile;

import static manatee.cache.definitions.loader.component.BinaryMapFile.EXPECTED_VERSION;
import static manatee.cache.definitions.loader.component.BinaryMapFile.MAGIC_NUMBER;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import manatee.cache.definitions.loader.exception.MapLoadException;
import manatee.cache.definitions.lump.EntityLump;
import manatee.cache.definitions.lump.LightLump;
import manatee.cache.definitions.lump.MapInfoLump;
import manatee.cache.definitions.lump.RegionLump;
import manatee.client.map.MapGeometry;
import manatee.client.scene.GameMap;
import manatee.client.scene.MapScene;

public class BinaryMapFileReader
{
	private static final Logger logger = (Logger) LoggerFactory.getLogger("IO");

	private String filePath;
	
	public BinaryMapFileReader(String filePath)
	{
		this.filePath = filePath;
	}

	public void read(MapScene scene) throws MapLoadException
	{
		logger.info("Loading map: " + filePath);
		
		GameMap map = scene.getMap();
		MapGeometry geom = map.getGeometry();
		
		try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath)))
		{
			// Header
			int magicNumber = dis.readInt();
			
			if (MAGIC_NUMBER != magicNumber)
				throw new MapLoadException("Tried to load an invalid map file: " + filePath);
			
			int fileVersion = dis.readInt();
			
			if (EXPECTED_VERSION != fileVersion)
				throw new MapLoadException("Tried to load an out of date map file: (" + filePath + ") expected version " + EXPECTED_VERSION);
			
			int mapRevision = dis.readInt();
			
			logger.debug("Map revision: " + mapRevision);
			
			int nLumps = dis.read();
			
			logger.debug("nLumps: " + nLumps);

			int[] offsets = new int[nLumps];
			int[] lengths = new int[nLumps];
			int[] lumpIds = new int[nLumps];
			
			// Lump Headers
            for(int i = 0; i < nLumps; i++)
            {
				offsets[i] = dis.readInt();
				lengths[i] = dis.readInt();
				lumpIds[i] = dis.read();
				int lumpVersion = dis.read();
				
				logger.debug("lumpid " + lumpIds[i] + " ver " + lumpVersion);
            }
			
			// Lumps
			for (int i = 0; i < nLumps; i++)
			{
				Object obj = readLump(dis.readNBytes(lengths[i])); //LumpDeserializer.deserialize(dis.readNBytes(lengths[i]), LumpSystem.LUMP_CLASSES[lumpIds[i]]);
				
				switch(lumpIds[i])
				{
				case 0:
				{
					MapInfoLump l = (MapInfoLump)obj;
					logger.debug("Parsing model info lump");
					
					l.process(scene, geom);
					break;
				}
				case 1:
				{
					logger.debug("Parsing region lump");
					RegionLump[] tfDataArr = (RegionLump[])obj;
					
					for (RegionLump regData : tfDataArr)
					{
						regData.process(geom);
					}
					break;
				}

				case 2:
				{
					logger.debug("Parsing light lump");
					
					LightLump[] lightDataArr = (LightLump[])obj;
					for (LightLump lightData : lightDataArr)
					{
						lightData.process(geom);
					}
					break;
				}

				case 3:
				{
					logger.debug("Parsing entity lump");
					
					EntityLump[] entityDataArr = (EntityLump[])obj;
					for (EntityLump entityData : entityDataArr)
					{
						entityData.process(scene, scene.getEntitySystem());
					}
					break;
				}
				default:
					logger.debug("Unknown lump with lumpid: " + lumpIds[i]);
				}
			}

			logger.info("Binary file read successfully.");
		}
		catch (IOException e) {
            e.printStackTrace();
        }
		
		geom.buildAllMeshes();
	}

	private Object readLump(byte[] data)
	{
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(bis))
		{
			return ois.readObject();
		}
		catch (IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
