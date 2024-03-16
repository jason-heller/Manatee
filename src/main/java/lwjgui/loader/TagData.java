package lwjgui.loader;

import lwjgui.scene.control.Button;
import lwjgui.scene.control.CheckBox;
import lwjgui.scene.control.ColorPicker;
import lwjgui.scene.control.Label;
import lwjgui.scene.control.PasswordField;
import lwjgui.scene.control.Slider;
import lwjgui.scene.control.TextArea;
import lwjgui.scene.control.TextField;
import lwjgui.scene.image.ImageView;
import lwjgui.scene.layout.BlurPane;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.FlowPane;
import lwjgui.scene.layout.GridPane;
import lwjgui.scene.layout.HBox;
import lwjgui.scene.layout.OpenGLPane;
import lwjgui.scene.layout.StackPane;
import lwjgui.scene.layout.VBox;
import lwjgui.scene.layout.floating.FloatingPane;

public enum TagData
{
	NO_OPERATION,
	STYLE,
	NOSCRIPT,
	
	BORDERPANE(BorderPane.class),
	BLURPANE(BlurPane.class),
	FLOWPANE(FlowPane.class),
	GRIDPANE(GridPane.class),
	HBOX(HBox.class),
	OPENGLPANE(OpenGLPane.class),
	STACKPANE(StackPane.class),
	VBOX(VBox.class),
	DIV(StackPane.class), 
	FLOATINGPANE(FloatingPane.class),
	
	LABEL(Label.class, true),
	BUTTON(Button.class, "", true),
	CHECKBOX(CheckBox.class, true),
	COLORPICKER(ColorPicker.class, true),
	IMG(ImageView.class, true),
	IMAGE(ImageView.class, true),
	IMAGEVIEW(ImageView.class, true),
	PASSWORDFIELD(PasswordField.class, true),
	TEXTAREA(TextArea.class, true),
	TEXTFIELD(TextField.class, true),
	SLIDER(Slider.class, true),
	SPINBOX(CheckBox.class, "spin", true);
	
	private boolean selfClosing;
	private Class<?> corrospondingTo = null;
	
	String argument;

	private TagData()
	{
	}
	
	private TagData(Class<?> corrospondingTo)
	{
		this.corrospondingTo = corrospondingTo;
	}
	
	private TagData(boolean selfClosing)
	{
		this.selfClosing = selfClosing;
	}
	
	private TagData(Class<?> corrospondingTo, boolean selfClosing)
	{
		this.corrospondingTo = corrospondingTo;
		this.selfClosing = selfClosing;
	}

	TagData(Class<?> corrospondingTo, String argument, boolean selfClosing)
	{
		this.corrospondingTo = corrospondingTo;
		this.argument = argument;
		
		this.selfClosing = selfClosing;
	}

	boolean isSelfClosing()
	{
		return selfClosing;
	}

	Class<?> getCorrospondingControl()
	{
		return corrospondingTo;
	}
}
