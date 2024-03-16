package manatee.client.dev;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public class Tracker
{
	private Object obj;
	private Field[] fields;
	private String[] names;
	
	private Map<Condition, Object> conditions = new HashMap<>();
	
	public Tracker(String title, Object value)
	{
		this(new String[] {title}, value, null);
	}
	
	public Tracker(String[] names, Object obj, Field[] fields)
	{
		this.names = names;
		this.obj = obj;
		this.fields = fields;
	}

	public Tracker is(Object value)
	{
		conditions.put(Condition.EQUAL, value);
		return this;
	}
	
	public Tracker isGreater(Object value)
	{
		conditions.put(Condition.GREATER, value);
		return this;
	}
	
	public Tracker isLess(Object value)
	{
		conditions.put(Condition.LESSER, value);
		return this;
	}
	
	public Tracker isNot(Object value)
	{
		conditions.put(Condition.INEQUAL, value);
		return this;
	}
	
	public Tracker isNotNaN()
	{
		conditions.put(Condition.NOT_NAN, 0);
		return this;
	}
	
	public Tracker isFinite()
	{
		conditions.put(Condition.FINITE, 0);
		return this;
	}
	
	public Tracker isTrue()
	{
		conditions.put(Condition.TRUE, 0);
		return this;
	}
	
	public Tracker isFalse()
	{
		conditions.put(Condition.FALSE, 0);
		return this;
	}
	
	public Tracker isNull()
	{
		conditions.put(Condition.NULL, null);
		return this;
	}
	
	public Tracker isNotNull()
	{
		conditions.put(Condition.NOT_NULL, null);
		return this;
	}
	
	@Override
	public String toString()
	{
		if (obj == null)
			return "OBJ MISSING";
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(obj.getClass().getSimpleName())
			.append(": ");
		
		if (fields != null)
		{
			appendFields(sb);
			return sb.toString();
		}
		
		sb.append('(');
		
		for(String name : names)
			sb.append(name);
			
		sb.append(") ");
		
		appendObject(sb, obj);
		
		return sb.toString();
	}

	private void appendFields(StringBuilder sb)
	{
		for(int i = 0; i < fields.length; i++)
		{
			Field field = fields[i];
			String name = names[i];
			
			sb.append(name);
			sb.append(" <");
			try
			{
				Object o = field.get(obj);
			
				if (o == null)
				{
					sb.append("null");
				}
				else
				{
					if (o.getClass().isArray())
					{
						for(Object elem : ((Object[])o))
						{
							sb.append('[');
	
							appendObject(sb, elem);
							
							sb.append(']');
						}
					}
					else
					{
	
						appendObject(sb, o);
					}
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		
			sb.append("> ");
		}
	}

	private void appendObject(StringBuilder sb, Object printObj)
	{
		String txt = printObj.toString();
		
		if (printObj instanceof Map)
		{
			Map<?, ?> map = ((Map<?, ?>) printObj);
			sb.append("size=" + map.size());
		}
		else if (txt.contains("@"))
		{
			sb.append(txt.getClass().getSimpleName()+"@"+Integer.toHexString(hashCode()));
		}
		else
		{
			sb.append(txt.toString());
		}
	}
	
	public Object getObj()
	{
		return obj;
	}

	public void setObj(Object obj)
	{
		this.obj = obj;
	}

	public Field[] getFields()
	{
		return fields;
	}

	public String[] getNames()
	{
		return this.names;
	}
	
	public boolean conditionsMet()
	{
		if (obj == null)
			return false;
		
		if (fields == null)
		{
			for(Entry<Condition, Object> cond : conditions.entrySet())
			{
				if (!checkCond(obj, cond.getKey(), cond.getValue()))
					return false;
			}
			
			return true;
		}
		
		for(Field field : fields)
		{
			for(Entry<Condition, Object> cond : conditions.entrySet())
			{
				try
				{
					if (!checkCond(field.get(obj), cond.getKey(), cond.getValue()))
						return false;
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private boolean checkCond(Object reference, Condition cond, Object value)
	{
		switch (cond)
		{
		case EQUAL:
			return reference.equals(value);
			
		case GREATER:
			if (reference instanceof Comparable)
				return ((Comparable<Object>)reference).compareTo(value) > 0;
			
			return false;
			
		case LESSER:
			if (reference instanceof Comparable)
				return ((Comparable<Object>)reference).compareTo(value) < 0;
			
			return false;
			
		case INEQUAL:
			return !reference.equals(value);
			
		case NOT_NAN:
			if (reference instanceof Number)
				return Double.isNaN(((Number)reference).doubleValue());
			
			return false;
			
		case FINITE:
			if (reference instanceof Number)
				return Double.isFinite(((Number)reference).doubleValue());
			
			if (reference instanceof Vector2fc)
				return ((Vector2fc)reference).isFinite();
			
			if (reference instanceof Vector3fc)
				return ((Vector3fc)reference).isFinite();
			
			if (reference instanceof Vector4fc)
				return ((Vector4fc)reference).isFinite();
			
			if (reference instanceof Matrix3fc)
				return ((Matrix3fc)reference).isFinite();
			
			if (reference instanceof Matrix4fc)
				return ((Matrix4fc)reference).isFinite();
			
			if (reference instanceof Quaternionf)
				return ((Quaternionf)reference).isFinite();
			
			return false;
			
		case TRUE:
			if (reference instanceof Boolean)
				return ((Boolean)reference).booleanValue();
			
			return false;
			
		case FALSE:
			if (reference instanceof Boolean)
				return !((Boolean)reference).booleanValue();
			
			return false;
			
		case NULL:
			return reference == null;
			
		case NOT_NULL:
			return reference != null;
		}
		
		return false;
	}

	private enum Condition
	{
		EQUAL, GREATER, LESSER, INEQUAL, NOT_NAN, FINITE, TRUE, FALSE, NULL, NOT_NULL;
	}
}
