package manatee.client.scene.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import lwjgui.LWJGUI;
import lwjgui.glfw.input.MouseHandler;
import lwjgui.scene.Window;
import manatee.cache.definitions.mesh.IMesh;
import manatee.client.entity.SpatialEntity;
import manatee.client.entity.stock.editor.EditorEntity;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.renderer.nvg.NVGLine;
import manatee.client.gl.renderer.nvg.NVGObject;
import manatee.client.input.Input;
import manatee.client.input.KeybindsInternal;
import manatee.client.scene.WindowPicker;
import manatee.client.scene.editor.history.ActionHistory;
import manatee.client.scene.editor.history.TransformAction;
import manatee.maths.Vectors;

public class Transformer
{

	private Map<EditorEntity, Object> transformables = new HashMap<>();
	
	private int mx, my;
	
	private Vector3f transformV = new Vector3f();
	private Quaternionf transformQ = new Quaternionf();
	
	private Vector3f transformAxis;
	
	private float mTransAngleIdentity;
	private float mTransLenIdentity;
	
	private Vector2i mTransSSOrigin = new Vector2i();
	private Vector2f mTransSSNormal = new Vector2f();
	private Vector2f mTransformVec = new Vector2f();
	
	private float mTransformAngle;
	
	private float mTransLenScale;
	
	private Vector3f mTransPosIdentity = new Vector3f();
	
	private NVGLine mTransLine;
	
	private final Vector3f mouseRay;
	
	private Transformation transform = Transformation.NONE;
	
	public Transformer(final Vector3f mouseRay)
	{
		this.mouseRay = mouseRay;
		transformAxis = Vectors.Z_AXIS;
	}
	
	public void transform(ICamera camera)
	{
		Window window = LWJGUI.getThreadWindow();
		MouseHandler mh = window.getMouseHandler();
		float[] viewport = camera.getViewport();
		
		mx = (int)(mh.getX() - viewport[0]);
		my = (int)(mh.getY() - viewport[1]);
		
		mTransformVec.set(mx, my).sub(mTransSSOrigin.x, mTransSSOrigin.y);
		mTransformAngle = (float) Math.atan2(mTransformVec.y, mTransformVec.x);

		Vector2f pLocal = new Vector2f(mx, my).sub(mTransSSOrigin.x, mTransSSOrigin.y);
		
		mTransLenScale = (pLocal.dot(mTransSSNormal) > 0) ? -1f : 1f;
		
		
		switch(transform)
		{
		case TRANSLATION:
			
			transformV.set(mouseRay)
				.sub(mTransPosIdentity)
				.mul(transformAxis)
				.mul(mTransPosIdentity.distance(camera.getPosition()));
		
			for(SpatialEntity e : transformables.keySet())
			{
				Vector3f startPos = (Vector3f)transformables.get(e);
				e.getPosition().set(startPos)
					.add(transformV);
				
				if (e instanceof EditorEntity)
				{
					((EditorEntity)e).onTranslate();
				}
			}
			
			
			break;
			
		case ROTATION:
			
			transformQ.fromAxisAngleRad(transformAxis, mTransAngleIdentity - mTransformAngle);
			
			mTransLine.setPoints(mx, my, mTransSSOrigin.x, mTransSSOrigin.y);

			for (SpatialEntity e : transformables.keySet())
			{
				Quaternionf startRot = (Quaternionf)transformables.get(e);
				e.getRotation().set(startRot);
				e.getRotation().mul(transformQ);
				
				if (e instanceof EditorEntity)
				{
					((EditorEntity)e).onRotate();
				}
			}
			
			break;
			
		case SCALE:
			
			transformV.set(transformAxis).mul(mTransformVec.length() / mTransLenIdentity);
			transformV.add(new Vector3f(Vectors.ONE).sub(transformAxis));
			
			mTransLine.setPoints(mx, my, mTransSSOrigin.x, mTransSSOrigin.y);

			for (EditorEntity e : transformables.keySet())
			{
				Vector3f startScale = (Vector3f)transformables.get(e);
				e.getScale().set(startScale)
					.mul(transformV)
					.mul(mTransLenScale);

				if (e instanceof EditorEntity)
				{
					((EditorEntity)e).onScale();
				}

				Vector3f sceneMax = new Vector3f(Float.NEGATIVE_INFINITY);
				Vector3f sceneMin = new Vector3f(Float.POSITIVE_INFINITY);
				
				for(int i = 0; i < e.getNumMeshes(); i++)
				{
					IMesh mesh = e.getMesh(i);
					sceneMax.max(mesh.getMax());
					sceneMin.min(mesh.getMin());
					
					Vector3f halfExtents = new Vector3f(sceneMax).sub(sceneMin).mul(0.5f);

					e.getBoundingBox().halfExtents.set(halfExtents).mul(e.getScale());
				}
			}
			
			
			break;
			
		default:
		}
	}
	
	public void update(ICamera camera, Set<SpatialEntity> selected, ActionHistory history, List<NVGObject> nvgObjects)
	{
		if (Input.isPressed(KeybindsInternal.X))
			transformAxis = Vectors.X_AXIS;
		
		if (Input.isPressed(KeybindsInternal.Y))
			transformAxis = Vectors.Y_AXIS;
		
		if (Input.isPressed(KeybindsInternal.Z))
			transformAxis = Vectors.Z_AXIS;
		
		if (selected.size() > 0)
		{
			// Toggle translation tool
			if (Input.isPressed(KeybindsInternal.T))
			{
				reset(history, nvgObjects);
				
				if (transform != Transformation.TRANSLATION)
				{
					transformAxis = Vectors.XY_AXIS;

					transformables.clear();
					for (SpatialEntity e : selected)
					{
						if (e != null && (e instanceof EditorEntity))
						{
							transformables.put((EditorEntity) e, new Vector3f(e.getPosition()));
						}
					}
					
					if (transformables.size() != 0)
					{
						mTransPosIdentity.set(mouseRay);
						transform = Transformation.TRANSLATION;
					}
				}
			}
			
			// Toggle rotation tool
			if (Input.isPressed(KeybindsInternal.R))
			{
				reset(history, nvgObjects);
				
				if (transform != Transformation.ROTATION)
				{
					int nSelected = 0;
					transformables.clear();
					mTransSSOrigin.zero();
					
					for (SpatialEntity e : selected)
					{
						if (e != null && (e instanceof EditorEntity))
						{
							transformables.put((EditorEntity) e, new Quaternionf(e.getRotation()));
							mTransSSOrigin.add(WindowPicker.worldSpaceToViewportSpace(camera, e.getPosition()));
							++nSelected;
						}
					}
					
					if (nSelected != 0) 
					{
						mTransAngleIdentity = mTransformAngle;
						transformAxis = Vectors.Z_AXIS;
	
						mTransSSOrigin.div(nSelected);
						
						mTransLine = new NVGLine(mx, my, mTransSSOrigin.x, mTransSSOrigin.y);
						nvgObjects.add(mTransLine);
						transform = Transformation.ROTATION;
					}
				}
			}
			
			if (Input.isPressed(KeybindsInternal.E))
			{
				reset(history, nvgObjects);
				
				if (transform != Transformation.SCALE)
				{
					int nSelected = 0;
					transformables.clear();
					mTransSSOrigin.zero();
					
					for (SpatialEntity e : selected)
					{
						if (e != null && (e instanceof EditorEntity))
						{
							transformables.put((EditorEntity) e, new Vector3f(e.getScale()));
							mTransSSOrigin.add(WindowPicker.worldSpaceToViewportSpace(camera, e.getPosition()));
							++nSelected;
						}
					}
					
					if (nSelected != 0) 
					{
						transformAxis = Vectors.ONE;
	
						mTransSSOrigin.div(nSelected);
						mTransSSNormal.set(mTransSSOrigin).sub(mx, my).normalize();
						mTransformVec.set(mx, my).sub(mTransSSOrigin.x, mTransSSOrigin.y);
						mTransLenIdentity = mTransformVec.length();
						
						mTransLine = new NVGLine(mx, my, mTransSSOrigin.x, mTransSSOrigin.y);
						nvgObjects.add(mTransLine);
						transform = Transformation.SCALE;
					}
				}
			}
		}
	}
	
	public void reset(ActionHistory history, List<NVGObject> nvgObjects)
	{
		if (transform == Transformation.NONE)
			return;
		
		float x, y, z, w;
		if (transform == Transformation.ROTATION)
		{
			x = transformQ.x;
			y = transformQ.y;
			z = transformQ.z;
			w = transformQ.w;
		}
		else
		{
			x = transformV.x;
			y = transformV.y;
			z = transformV.z;
			w = 0f;
		}
		
		history.commit(new TransformAction(transform, x, y, z, w, transformables.keySet()));
		
		transform = Transformation.NONE;
		
		nvgObjects.remove(mTransLine);
		mTransLine = null;
		
		transformables.clear();
	}
}
