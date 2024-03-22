package manatee.primitives.gl;

import org.lwjgl.opengl.GL11;

import manatee.maths.MCache;
import manatee.primitives.Primitive;

public class LineRenderer implements PrimitiveRenderer {

    @Override
    public void preRender(PrimitiveShader shader) {
        shader.setUniform("Scale", MCache.ONE);
    }

    @Override
    public void render(Primitive primitive, PrimitiveShader shader) {
        shader.setUniform("OffsetA", primitive.getStart());
        shader.setUniform("OffsetB", primitive.getEnd());
        shader.setUniform("EdgeColor", primitive.getColor());

        GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, 2);
    }
    
    @Override
	public void postRender(PrimitiveShader shader)
	{
	}
}
