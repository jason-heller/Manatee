package manatee.primitives.gl;

import manatee.primitives.Primitive;

public interface PrimitiveRenderer {

    public void preRender(PrimitiveShader shader);

    public void render(Primitive primitive, PrimitiveShader shader);

	public void postRender(PrimitiveShader shader);
}
