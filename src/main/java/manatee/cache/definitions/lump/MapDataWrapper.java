package manatee.cache.definitions.lump;

public class MapDataWrapper
{
	public MapInfoLump mapInfo;
	public RegionLump[] regData;
	public LightLump[] lights;
	public EntityLump[] entities;

	public MapDataWrapper(MapInfoLump mapInfo, RegionLump[] regData, LightLump[] lights, EntityLump[] entities)
	{
		this.mapInfo = mapInfo;
		this.regData = regData;
		this.lights = lights;
		this.entities = entities;
	}
}
