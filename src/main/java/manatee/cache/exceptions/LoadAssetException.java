package manatee.cache.exceptions;

@SuppressWarnings("serial")
public class LoadAssetException extends RuntimeException
{
	public LoadAssetException(String errorMessage)
	{
		super(errorMessage);
	}
}
