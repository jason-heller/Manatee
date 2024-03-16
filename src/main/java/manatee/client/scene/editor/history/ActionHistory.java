package manatee.client.scene.editor.history;

public class ActionHistory
{
	private ReversableAction[] actions;
	
	private int actionIndex = 0;
	
	public ActionHistory(int memorySize)
	{
		actions = new ReversableAction[memorySize];
	}
	
	public void commit(ReversableAction action)
	{
		if (actionIndex == actions.length)
		{
			for(int i = 1; i < actionIndex; i++)
				actions[i - 1] = actions[i];
			
			actions[actionIndex - 1] = action;
		}
		else
		{
			actions[actionIndex++] = action;
			
			for(int i = actionIndex; i < actions.length; i++)
				actions[i] = null;
		}
	}

	public void undo()
	{
		if (actionIndex == 0)
			return;
		actions[--actionIndex].reverse();
	}
	
	public void redo()
	{
		if (actionIndex == actions.length || actions[actionIndex] == null)
			return;
		
		actions[actionIndex++].act();
	}

	public void clear()
	{
		for(int i = 0; i < actions.length; i++)
			actions[i] = null;
		
		actionIndex = 0;
	}
}
