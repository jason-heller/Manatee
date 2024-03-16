package lwjgui.scene.control;

import lwjgui.paint.Color;
import lwjgui.theme.Theme;

public class StandaloneToggleButton extends Button {
	
	private boolean impressed;
	
	public StandaloneToggleButton(String name) {
		super( name );
		
		this.setOnActionInternal( event -> {

			this.impressed = !impressed;
		});
	}
	
	@Override
	protected void defaultStyle()
	{
		super.defaultStyle();
		
		if (impressed)
		{
			Color outlineColor = Theme.current().getSelection();
			this.setBorderColor(outlineColor);
		}
	}
}
