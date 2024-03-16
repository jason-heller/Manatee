package manatee.client.gl.mesh;

/** Used to determine which tile shader a given tile type belongs to.
 * Aditionally, render order is maintained, that is to say that enum
 * values appearing later and rendered after the ones prior
 *
 */
public enum TileShaderTarget
{
	GENERIC, FOLIAGE, WATER, TRANSLUCENT;
}
