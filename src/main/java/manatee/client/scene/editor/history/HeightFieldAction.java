package manatee.client.scene.editor.history;

import manatee.client.map.MapGeometry;
import manatee.client.map.MapRegion;

public class HeightFieldAction implements ReversableAction
{
	private MapGeometry geom;
	private MapRegion add, remove;
	
	public HeightFieldAction(MapGeometry geom, MapRegion add, MapRegion remove)
	{
		this.geom = geom;
		this.add = add;
		this.remove = remove;
	}

	@Override
	public void act()
	{
		if (add != null)
			geom.addRegion(add);
		
		if (remove != null)
			geom.removeRegion(remove);
	}

	@Override
	public void reverse()
	{
		if (add != null)
			geom.removeRegion(add);
		
		if (remove != null)
			geom.addRegion(remove);
	}

}
