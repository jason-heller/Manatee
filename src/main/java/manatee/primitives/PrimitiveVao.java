package manatee.primitives;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class PrimitiveVao {

    private final int vao;
    private int positionBuffer;
    private int indexBuffer;

    public PrimitiveVao(float[] positions, int[] indices) {
        vao = glGenVertexArrays();

        bind();
        setPositions(positions);
        setIndices(indices);
        unbind();
    }
    
    public PrimitiveVao(FloatBuffer positions, IntBuffer indices) {
        vao = glGenVertexArrays();

        bind();
        setPositions(positions);
        setIndices(indices);
        unbind();
    }

    private void setPositions(float[] positions) {
        positionBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, positionBuffer);

        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void setIndices(int[] indices) {
        indexBuffer = glGenBuffers();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    private void setPositions(FloatBuffer positions) {
        positionBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, positionBuffer);

        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void setIndices(IntBuffer indices) {
        indexBuffer = glGenBuffers();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void bind() {
        glBindVertexArray(vao);
    }

    private void unbind() {
        glBindVertexArray(0);
    }

    int getVao() {
        return vao;
    }

    void destroy() {
        glDeleteBuffers(indexBuffer);
        glDeleteBuffers(positionBuffer);
        glDeleteVertexArrays(vao);
    }
}
