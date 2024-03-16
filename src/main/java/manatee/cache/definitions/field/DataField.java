package manatee.cache.definitions.field;

public interface DataField<T extends Number>
{
	public T get(int x, int y);
	
	public T getLocal(int x, int y);

	public void set(T value, int x, int y);
	
	public void setLocal(T value, int x, int y);
	
	public void add(T value, int x, int y);
	
	public abstract void addLocal(T value, int x, int y);

	//public T[][] get();

	//public void set(T[][] data);
}
