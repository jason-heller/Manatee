package manatee.client.entity;

import static manatee.client.scene.GlobalAssets.MISSING_COLOR;
import static manatee.client.scene.GlobalAssets.NO_TEX;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import manatee.cache.definitions.Model;
import manatee.cache.definitions.mesh.IMesh;
import manatee.cache.definitions.mesh.anim.Animator;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.gl.mesh.EntityShaderTarget;
import manatee.client.scene.GlobalAssets;
import manatee.client.scene.MapScene;
import manatee.maths.MCache;

/**
 * A form is an entity with a visible presence in the game scene.
 * 
 * @author Jay
 *
 */
public abstract class Form extends SpatialEntity
{
	protected boolean visible = true;
	
	protected boolean lod = false;
	protected float lodDistanceSqr;

	protected Model model;
	
	protected Animator animator;
	
	private boolean fullbright;

	protected Vector4f color = new Vector4f(MISSING_COLOR);

	protected Matrix4f modelMatrix = new Matrix4f();
	
	protected EntityShaderTarget shaderTarget = EntityShaderTarget.GENERIC;

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void setGraphic(IMesh mesh, ITexture texture, EntityShaderTarget shaderTarget)
	{
		setShaderTarget(shaderTarget);
		
		ITexture[] textures = null;
		
		if (mesh == null)
		{
			mesh = GlobalAssets.MISSING_MESH;
			this.color = MISSING_COLOR;
			textures = new ITexture[] {NO_TEX};
		}
		
		if (texture == null)
			texture = GlobalAssets.MISSING_TEX;
		
		if (model == null && this.boundingBox.halfExtents.equals(MCache.EMPTY))
		{
			Vector3f halfExtents = new Vector3f(mesh.getMax()).sub(mesh.getMin()).mul(0.5f);

			//Vector3f center = new Vector3f(mesh.getMin()).add(halfExtents);
			this.boundingBox.halfExtents.set(halfExtents);
		}
		
		IMesh[] meshes = new IMesh[] {mesh};
		this.model = new Model(meshes, textures);
	}
	
	public void setGraphic(Model model, EntityShaderTarget shaderTarget)
	{
		this.model = model;
		
		setShaderTarget(shaderTarget);
		
		Vector3f sceneMax = new Vector3f(Float.NEGATIVE_INFINITY);
		Vector3f sceneMin = new Vector3f(Float.POSITIVE_INFINITY);
		
		for(IMesh mesh : model.getMeshes())
		{
			sceneMax.max(mesh.getMax());
			sceneMin.min(mesh.getMin());
			
			Vector3f halfExtents = new Vector3f(sceneMax).sub(sceneMin).mul(0.5f);

			//Vector3f center = new Vector3f(mesh.getMin()).add(halfExtents);
			this.boundingBox.halfExtents.set(halfExtents);
		}
		
		if (model.isAnimated())
		{
			animator = new Animator(model);
		}
	}

	protected void updateModelMatrix()
	{
		modelMatrix.identity();
		modelMatrix.translate(position.x, position.y, position.z);
		modelMatrix.rotate(rotation);
		modelMatrix.scale(scale.x, scale.y, scale.z);
	}
	
	@Override
	public void updateInternal(MapScene scene)
	{
		boundingBox.update();
		
		if (animator != null)
			animator.tick();
		
		update(scene);
		
		updateModelMatrix();
	}

	public Matrix4f getModelMatrix()
	{
		return modelMatrix;
	}
	
	public Vector4f getColor()
	{
		return color;
	}
	
	public void setColor(Vector4f color)
	{
		if (model.getMeshes()[0] != GlobalAssets.MISSING_MESH)
			this.color = color;
	}

	public IMesh getMesh(int i)
	{
		return model.getMeshes()[i];
	}

	public ITexture getTexture(int i)
	{
		return model.getTextures()[i];
	}

	public boolean isFullbright()
	{
		return fullbright;
	}

	public void setFullbright(boolean fullbright)
	{
		this.fullbright = fullbright;
	}

	public float getLodDistanceSqr()
	{
		return lodDistanceSqr;
	}
	
	public boolean hasLod()
	{
		return lod;
	}

	public void setLod(boolean lod)
	{
		this.lod = lod;
	}

	public void setLodDistanceSqr(float lodDistanceSqr)
	{
		this.lodDistanceSqr = lodDistanceSqr;
	}

	public EntityShaderTarget getShaderTarget()
	{
		return shaderTarget;
	}

	public void setShaderTarget(EntityShaderTarget shaderTarget)
	{
		this.shaderTarget = shaderTarget;
	}

	public Model getModel()
	{
		return model;
	}

	public Animator getAnimator()
	{
		return animator;
	}
}
