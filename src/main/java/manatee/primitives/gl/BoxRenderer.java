package manatee.primitives.gl;

import org.lwjgl.opengl.GL11;

import manatee.primitives.Primitive;
import manatee.primitives.Primitives;

public class BoxRenderer implements PrimitiveRenderer {

    @Override
    public void preRender(PrimitiveShader shader) {
    }

    @Override
    public void render(Primitive primitive, PrimitiveShader shader) {
        shader.setUniform("OffsetA", primitive.getStart());
        shader.setUniform("OffsetB", primitive.getStart());
        shader.setUniform("Scale", primitive.getEnd());
        shader.setUniform("EdgeColor", primitive.getColor());
		shader.setUniform("Rotation", primitive.getRotation());

        GL11.glDrawElements(GL11.GL_LINES, 24, GL11.GL_UNSIGNED_INT, 0);
    }
    
    @Override
	public void postRender(PrimitiveShader shader)
	{
		shader.setUniform("Rotation", Primitives.NO_ROTATION);
	}
}
