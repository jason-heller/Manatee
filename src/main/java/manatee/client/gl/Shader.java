package manatee.client.gl;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import manatee.cache.definitions.loader.ShaderLoader;
import manatee.cache.definitions.texture.ITexture;

public class Shader
{
	private String vertexFile, fragmentFile;
	private int vertexID, fragmentID, programID;

	private static final Logger logger = Logger.getLogger(Shader.class.getName());

	public Shader(String vertexPath, String fragmentPath)
	{
		vertexFile = ShaderLoader.loadShader(vertexPath);
		fragmentFile = ShaderLoader.loadShader(fragmentPath);

		programID = GL20.glCreateProgram();
		vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

		GL20.glShaderSource(vertexID, vertexFile);
		GL20.glCompileShader(vertexID);

		if (GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			logger.warning("Vertex Shader: " + GL20.glGetShaderInfoLog(vertexID));
			return;
		}

		fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

		GL20.glShaderSource(fragmentID, fragmentFile);
		GL20.glCompileShader(fragmentID);

		if (GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			logger.warning("Fragment Shader: " + GL20.glGetShaderInfoLog(fragmentID));
			return;
		}

		GL20.glAttachShader(programID, vertexID);
		GL20.glAttachShader(programID, fragmentID);

		GL20.glLinkProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE)
		{
			logger.warning("Program Linking: " + GL20.glGetProgramInfoLog(programID));
			return;
		}

		GL20.glValidateProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE)
		{
			logger.warning("Program Validation: " + GL20.glGetProgramInfoLog(programID));
			return;
		}

		// GL20.glLinkProgram(programID);

		GL20.glDetachShader(programID, vertexID);
		GL20.glDetachShader(programID, fragmentID);
		GL20.glDeleteShader(vertexID);
		GL20.glDeleteShader(fragmentID);
	}

	public void bind()
	{
		GL20.glUseProgram(programID);
	}

	public void unbind()
	{
		GL20.glUseProgram(0);
	}

	public void dispose()
	{
		GL20.glDeleteProgram(programID);

	}

	public int getUniformLocation(String name)
	{
		return GL20.glGetUniformLocation(programID, name);
	}

	public void setUniform(String name, float value)
	{
		GL20.glUniform1f(getUniformLocation(name), value);
	}

	public void setUniform(String name, int value)
	{
		GL20.glUniform1i(getUniformLocation(name), value);
	}

	public void setUniform(String name, boolean value)
	{
		GL20.glUniform1i(getUniformLocation(name), value ? 1 : 0);
	}
	
	public void setUniform(String name, Vector2f value)
	{
		setUniform(name, value.x, value.y);
	}
	
	public void setUniform(String name, float x, float y)
	{
		GL20.glUniform2f(getUniformLocation(name), x, y);
	}

	public void setUniform(String name, Vector3f value)
	{
		setUniform(name, value.x, value.y, value.z);
	}
	
	public void setUniform(String name, float x, float y, float z)
	{
		GL20.glUniform3f(getUniformLocation(name), x, y, z);
	}

	public void setUniform(String name, Vector4f value)
	{
		GL20.glUniform4f(getUniformLocation(name), value.x(), value.y(), value.z(), value.w());
	}

	public void setUniform(String name, Matrix4f value)
	{
		float[] matrix = new float[16];
		value.get(matrix);

		GL20.glUniformMatrix4fv(getUniformLocation(name), false, matrix);
	}
	
	public void setUniformArray3f(String name, float[] value)
	{
		GL20.glUniform3fv(getUniformLocation(name), value);
	}

	public void setUniform(String name, Matrix4f[] matrices)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			int length = matrices != null ? matrices.length : 0;
			FloatBuffer fb = stack.mallocFloat(16 * length);
			for (int i = 0; i < length; i++)
			{
				matrices[i].get(16 * i, fb);
			}
			GL20.glUniformMatrix4fv(getUniformLocation(name), false, fb);
		}
	}

	public void setTexture(String name, ITexture texture, int activeTexture)
	{
		setUniform(name, activeTexture);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + activeTexture);
		texture.bind();
	}

}
