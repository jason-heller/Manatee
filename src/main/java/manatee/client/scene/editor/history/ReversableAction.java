package manatee.client.scene.editor.history;

public interface ReversableAction
{
	public void act();
	public void reverse();
}
