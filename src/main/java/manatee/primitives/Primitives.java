package manatee.primitives;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import manatee.maths.MCache;
import manatee.primitives.gl.BoxFillRenderer;
import manatee.primitives.gl.BoxRenderer;
import manatee.primitives.gl.ConeRenderer;
import manatee.primitives.gl.LineRenderer;
import manatee.primitives.gl.PointRenderer;
import manatee.primitives.gl.PrimitiveRenderer;
import manatee.primitives.gl.PrimitiveShader;

public class Primitives
{

	private static PrimitiveShader shader;
	private static PrimitiveVao point;
	private static PrimitiveVao line;
	private static PrimitiveVao box;
	private static PrimitiveVao boxFill;
	private static PrimitiveVao cone;

	private static Vector3f defaultColor = new Vector3f(1, 1, 0);

	private static Map<Integer, List<Primitive>> primitives = new HashMap<>();

	private static Map<Integer, PrimitiveRenderer> primitiveRenderers = new HashMap<>();
	
	public static final Matrix3f NO_ROTATION = new Matrix3f();

	public static void init()
	{

		final float[] boxPositions =
		{
				-1f, -1f, -1f, 1f, -1f, -1f, 1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f, 1f, 1f, 1f, -1f, -1f, 1f, -1f, -1f, 1f,
				1f
		};

		final int[] boxIndices =
		{
				0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 6, 1, 5, 2, 4, 3, 7
		};

		final int[] boxFillIndices =
		{
				0, 5, 1, 6, 5, 0, // bottom
				3, 2, 4, 3, 4, 7, // top
				2, 1, 5, 2, 5, 4, // R
				3, 6, 0, 3, 7, 6, // L
				7, 4, 6, 6, 4, 5, // F
				3, 1, 2, 3, 0, 1 // B
		};

		shader = new PrimitiveShader("shader/primative.vsh", "shader/primative.fsh");
		
		final int arrowHeadResolution = 6;

		point = PrimitiveUtil.createSphere(6);
		cone = PrimitiveUtil.createCone(arrowHeadResolution);
		line = new PrimitiveVao(new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, new int[] { 0, 1 });
		box = new PrimitiveVao(boxPositions, boxIndices);
		boxFill = new PrimitiveVao(boxPositions, boxFillIndices);

		primitives.put(point.getVao(), new ArrayList<>());
		primitives.put(line.getVao(), new ArrayList<>(50));
		primitives.put(box.getVao(), new ArrayList<>(10));
		primitives.put(boxFill.getVao(), new ArrayList<>(10));
		primitives.put(cone.getVao(), new ArrayList<>(10));

		primitiveRenderers.put(point.getVao(), new PointRenderer(180));
		primitiveRenderers.put(line.getVao(), new LineRenderer());
		primitiveRenderers.put(box.getVao(), new BoxRenderer());
		primitiveRenderers.put(boxFill.getVao(), new BoxFillRenderer());
		primitiveRenderers.put(cone.getVao(), new ConeRenderer((4 + arrowHeadResolution) * 3));

	}

	public static Primitive addPoint(Vector3f origin)
	{
		return addPoint(origin, defaultColor);
	}

	public static Primitive addPoint(Vector3f origin, Vector3f rgb)
	{
		int id = point.getVao();
		Primitive primitive = new Primitive(id, origin, rgb, NO_ROTATION);

		primitives.get(id).add(primitive);
		
		return primitive;
	}

	public static Primitive addLine(Vector3f start, Vector3f end)
	{
		return addLine(start, end, defaultColor);
	}

	public static Primitive addLine(Vector3f start, Vector3f end, Vector3f rgb)
	{
		int id = line.getVao();
		Primitive primitive = new Primitive(id, start, end, rgb, NO_ROTATION);

		primitives.get(id).add(primitive);
		
		return primitive;
	}
	
	public static Primitive addArrow(Vector3f start, Vector3f end)
	{
		return addArrow(start, end, defaultColor);
	}

	public static Primitive addArrow(Vector3f start, Vector3f end, Vector3f rgb)
	{
		Vector3f dir = new Vector3f(start).sub(end).normalize();
		Matrix3f rot = new Matrix3f();
		
		if (dir.z >= -.99f)
			rot.lookAlong(dir, MCache.Z_AXIS);
		
		int id = cone.getVao();
		Primitive head = new Primitive(id, start, start, rgb, rot);

		primitives.get(id).add(head);
		
		return head;
	}

	public static Primitive addBox(Vector3f origin, Vector3f halfSizes)
	{
		return addBox(origin, halfSizes, defaultColor);
	}

	public static Primitive addBox(Vector3f origin, Vector3f halfSizes, Vector3f rgb)
	{
		int id = box.getVao();
		Primitive primitive = new Primitive(id, origin, halfSizes, rgb, new Matrix3f());

		primitives.get(id).add(primitive);
		
		return primitive;
	}
	
	public static Primitive addBoxFill(Vector3f origin, Vector3f halfSizes)
	{
		return addBoxFill(origin, halfSizes, defaultColor);
	}

	public static Primitive addBoxFill(Vector3f origin, Vector3f halfSizes, Vector3f rgb)
	{
		int id = boxFill.getVao();
		Primitive primitive = new Primitive(id, origin, halfSizes, rgb, new Matrix3f());

		primitives.get(id).add(primitive);
		
		return primitive;
	}
	
	public static void remove(Primitive primitive)
	{
		int id = primitive.getId();
		
		primitives.get(id).remove(primitive);
	}

	public static void clear()
	{
		for (List<Primitive> prims : primitives.values())
			prims.clear();
	}

	public static void render(Matrix4f projectionMatrix, Matrix4f viewMatrix)
	{
		render(new Matrix4f(projectionMatrix).mul(viewMatrix));
	}

	public static void render(Matrix4f projectionViewMatrix)
	{
		shader.bind();
		shader.setUniform("ProjectionView", projectionViewMatrix);
		shader.setUniform("Rotation", NO_ROTATION);

		for (int vao : primitives.keySet())
		{
			renderBatch(vao, primitives.get(vao));
		}

		glDisableVertexAttribArray(0);
		glBindVertexArray(0);

		shader.unbind();
	}
	
	private static void renderBatch(int vao, Iterable<Primitive> list)
	{
		List<Primitive> drawnPrimitives = primitives.get(vao);
		PrimitiveRenderer renderer = primitiveRenderers.get(vao);

		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		renderer.preRender(shader);

		for (Primitive drawnPrim : drawnPrimitives)
		{
			renderer.render(drawnPrim, shader);
		}

		renderer.postRender(shader);
	}

	public static void renderBatch(int vao, Matrix4f projectionViewMatrix, Primitive[] prims)
	{
		renderBatch(vao, projectionViewMatrix, Arrays.asList(prims));
	}
	
	public static void renderBatch(int vao, Matrix4f projectionViewMatrix, Iterable<Primitive> prims)
	{
		shader.bind();
		shader.setUniform("ProjectionView", projectionViewMatrix);
		shader.setUniform("Rotation", NO_ROTATION);
		
		renderBatch(vao, prims);

		glDisableVertexAttribArray(0);
		glBindVertexArray(0);

		shader.unbind();
	}

	public static void dispose()
	{
		primitives.clear();
		point.destroy();
		line.destroy();
		box.destroy();

		shader.destroy();
	}
}
