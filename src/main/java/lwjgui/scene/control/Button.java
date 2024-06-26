package lwjgui.scene.control;

import lwjgui.geometry.Insets;
import lwjgui.scene.Node;

public class Button extends ButtonBase {
	
	public Button(String name) {
		this( name, null );
	}
	
	public Button( String name, Node graphic ) {
		super(name);
		
		this.setPadding(new Insets(4,6,4,6));
		this.setText(name);
		this.setGraphic(graphic);
	}

	@Override
	public String getElementType() {
		return "button";
	}
	
	@Override
	public boolean isResizeable() {
		return false;
	}

	@Override
	public Button clone()
	{
		return new Button(getText());
	}
	
	/*@Override
	public Vector2d getAvailableSize() {
		return new Vector2d(getMaxWidth(),getMaxHeight());
	}*/
}
