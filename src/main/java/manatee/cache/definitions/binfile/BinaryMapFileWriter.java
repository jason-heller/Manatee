package manatee.cache.definitions.binfile;

import static manatee.cache.definitions.loader.component.BinaryMapFile.EXPECTED_VERSION;
import static manatee.cache.definitions.loader.component.BinaryMapFile.MAGIC_NUMBER;
import static manatee.cache.definitions.loader.component.BinaryMapFile.mapRevision;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class BinaryMapFileWriter
{
	private static final Logger logger = (Logger) LoggerFactory.getLogger("IO");

	private String filePath;
	
	private Object[] lumps;
	
	public BinaryMapFileWriter(String filePath)
	{
		this.filePath = filePath;
	}

	public void write()
	{
		int nLumps = lumps.length;
	
		byte[][] lumpData = new byte[nLumps][];
		
		for(int i = 0; i < nLumps; i++)
		{
        	try
			{
				lumpData[i] = writeLump(lumps[i]);;//LumpSerializer.serialize(lumps[i]);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
        }
		
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath)))
		{
			// Write header
			dos.writeInt(MAGIC_NUMBER); // Magic Number
			dos.writeInt(EXPECTED_VERSION); // File Version
			dos.writeInt(mapRevision); // Map Revision
			dos.writeByte(nLumps); // Lump Count

			// Lump Headers
			int offset = 0;
            for(int i = 0; i < nLumps; i++)
            {
				int len = lumpData[i] == null ? 0 : lumpData[i].length;
				Class<?> cl = lumps[i].getClass().isArray() ? lumps[i].getClass().getComponentType() : lumps[i].getClass();
				byte lumpId = (byte) LumpSystem.getClassId(cl);
            	
            	dos.writeInt(offset);
            	dos.writeInt(len);
            	dos.write(lumpId);		// Lump identifier
            	dos.write(lumps[i].getClass().getFields().length);			// Lump version (# fields)
            	
            	offset += len;
            }
			
			// Lumps
			for (int i = 0; i < nLumps; i++)
			{
				if (lumpData[i] != null)
					dos.write(lumpData[i]);
			}

			logger.info("Binary file exported successfully.");
			logger.info("Num bytes: " + offset);
			logger.info("Num lumps: " + lumps.length);
		}
		catch (IOException e) {
            e.printStackTrace();
        }
	}

	private byte[] writeLump(Object obj)
	{
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos))
		{
			oos.writeObject(obj);
			oos.flush();
			
			return bos.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	public void addLumps(Object... lumps)
	{
		this.lumps = lumps;
	}

}
