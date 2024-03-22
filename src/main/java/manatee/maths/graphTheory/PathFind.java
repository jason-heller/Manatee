package manatee.maths.graphTheory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joml.Vector2f;
import org.joml.Vector2i;

import manatee.client.map.MapGeometry;
import manatee.client.map.tile.Tile;

public class PathFind
{
	private static final Vector2i[] NEIGHBORS = {
			new Vector2i(-1,-1),
			new Vector2i(0,-1),
			new Vector2i(1,-1),
			new Vector2i(-1,0),
			new Vector2i(1,0),
			new Vector2i(-1,1),
			new Vector2i(0,1),
			new Vector2i(1,1)
	};
	
	private static final int MAX_ITERATIONS = 80;
	
	//private static List<Primitive> pts = new ArrayList<>();
	
	public static Vector2f[] aStar(int startX, int startY, int endX, int endY, MapGeometry geom)
	{
		return aStar(new Vector2i(startX, startY), new Vector2i(endX, endY), geom);
	}

	public static Vector2f[] aStar(Vector2i start, Vector2i end, MapGeometry geom)
	{
		Tile tile = geom.getTileAt(end.x, end.y);
    	
    	if (tile.isSolid())
		    return null;
    	
    	int iterations = 0;
		
		List<Vector2i> open = new ArrayList<>();
		Set<Vector2i> closed = new HashSet<>();
		
		Hashtable<Vector2i, Vector2i> parents = new Hashtable<>();
		Hashtable<Vector2i, Integer> gScores = new Hashtable<>();
		
		List<Vector2f> result = new LinkedList<>();
		
		/*for(Primitive p : pts)
			Primitives.remove(p);
		pts.clear();*/

		Vector2i current = null;

		open.add(start);
		gScores.put(start, 0);

		while (!open.isEmpty())
		{
			iterations++;
			
			if (iterations > MAX_ITERATIONS)
				return null;
			
			float min = Float.POSITIVE_INFINITY;
			// searching for minimal F score
			for (Vector2i f : open)
			{
				//Tile tile = geom.getTileAt(f.x, f.y);

				int score = gScores.get(f) + getDist(f, end);
				if (min > score)
				{
					min = score;
					current = f;
				}
			}
			
			// Found path
			if (current.equals(end))
			{
				
				Vector2i v = end;
	            while(!v.equals(start)){
	            	result.add(0, new Vector2f(v.x + 0.5f, v.y + 0.5f));
	                v = parents.get(v);
	                //pts.add(Primitives.addBox(new Vector3f(v.x + .5f, v.y + .5f, 1f), new Vector3f(.5f,.5f,0f)));
	            }
	            //pts.add(Primitives.addBox(new Vector3f(end.x + .5f, end.y + .5f, 1f), new Vector3f(.5f,.5f,0f)));
	            
				return result.toArray(new Vector2f[0]);
			}
			
			open.remove(current);
	        closed.add(current);
	        
	        for (Vector2i offset : NEIGHBORS)
	        {
	        	Vector2i neighbor = new Vector2i(current).add(offset);
	        	tile = geom.getTileAt(neighbor.x, neighbor.y);
	        	
	        	if (tile.isSolid() || closed.contains(neighbor))
				    continue;
	        	
	        	int score = 1;//getDist(current, neighbor);
	        	boolean distIsBetter = false;

	        	if (!open.contains(neighbor))
	        	{
	        		open.add(neighbor);
	        		distIsBetter = true;
	        	}
	        	/*else if (score < gScores.get(neighbor))
	        	{
	        		distIsBetter = true;
	        	}*/
	        	
	        	if (distIsBetter)
	        	{
	        		parents.put(neighbor, current);
	        		gScores.put(neighbor, score);
	        	}
	        }
		}
		
		return null;
	}

	private static int getDist(Vector2i a, Vector2i b)
	{
		int x = a.x - b.x;
		int y = a.y - b.y;
		
		x = (x < 0) ? -x : x;
		y = (y < 0) ? -y : y;

		return x + y;
	}
}
