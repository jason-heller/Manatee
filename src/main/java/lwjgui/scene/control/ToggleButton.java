package lwjgui.scene.control;

import org.lwjgl.nanovg.NanoVG;

import lwjgui.paint.Color;
import lwjgui.scene.Context;
import lwjgui.scene.Node;
import lwjgui.style.BlockPaneRenderer;
import lwjgui.style.BoxShadow;
import lwjgui.theme.Theme;
import manatee.client.dev.Dev;

public class ToggleButton extends Button implements Toggle {
	protected boolean selected;
	protected ToggleGroup toggleGroup;
	
	public ToggleButton( String name ) {
		this(name, null);
	}

	public ToggleButton(String name, ToggleGroup group) {
		super(name);
		
		this.setOnActionInternal( event -> {

			setSelected(true);
		});
		
		this.setToggleGroup(group);
	}
	
	@Override
	protected boolean isPressed() {
		return super.isPressed() || isSelected();
	}

	@Override
	protected void defaultStyle()
	{
		super.defaultStyle();
		
		if (this.toggleGroup.getCurrectSelected() != null && this.toggleGroup.getCurrectSelected().equals(this))
		{
			Color outlineColor = Theme.current().getSelection();
			this.setBorderColor(outlineColor);
		}
	}
	
	/**
	 * Sets the toggle group of this button.
	 * @param g
	 */
	public void setToggleGroup( ToggleGroup g ) {
		if ( g == null )
			return;
		
		this.toggleGroup = g;
		g.add(this);
	}

	/**
	 * Sets whether or not this button is selected.
	 * <br>
	 * If it belongs to a ToggleGroup, only one button can be selected at a time.
	 * @param b
	 */
	public void setSelected(boolean b) {
		this.selected = b;

		if ( this.toggleGroup != null && b && (this.toggleGroup.getCurrectSelected()==null || !this.toggleGroup.getCurrectSelected().equals(this)) ) {
			this.toggleGroup.selectToggle(this);
		}
	}
	
	/**
	 * 
	 * @return Returns whether or not the button is selected.
	 */
	public boolean isSelected() {
		return this.selected;
	}
}
