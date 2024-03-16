package manatee.cache.definitions.loader.component;

public class BinaryMapFile
{
	public static final int MAGIC_NUMBER = (('T' << 24) + ('M' << 16) + ('A' << 8) + 'P');
	
	public static final int EXPECTED_VERSION = 1; 
	
	public static int mapRevision = 0;
}
