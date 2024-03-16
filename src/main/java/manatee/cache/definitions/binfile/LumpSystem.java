package manatee.cache.definitions.binfile;

import manatee.cache.definitions.lump.EntityLump;
import manatee.cache.definitions.lump.LightLump;
import manatee.cache.definitions.lump.MapInfoLump;
import manatee.cache.definitions.lump.RegionLump;

public class LumpSystem
{
	public static Class<?>[] LUMP_CLASSES =
	{
			MapInfoLump.class, RegionLump.class, LightLump.class, EntityLump.class
	};
	
	public static int getClassId(Class<?> c)
	{
		for(int i = 0; i < LUMP_CLASSES.length; i++)
		{
			if (c == LUMP_CLASSES[i])
				return i;
		}
		
		return -1;
	}
	
	public Class<?> getClassFromBy(int id)
	{
		return LUMP_CLASSES[id];
	}
}
