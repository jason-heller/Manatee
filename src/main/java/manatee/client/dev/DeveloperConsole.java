package manatee.client.dev;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.glfw.GLFW;

import lwjgui.LWJGUI;
import lwjgui.paint.Color;
import lwjgui.scene.Context;
import lwjgui.scene.Scene;
import lwjgui.scene.control.TextArea;
import lwjgui.scene.control.TextField;
import lwjgui.scene.control.TextInputControl;
import lwjgui.scene.layout.Pane;
import lwjgui.scene.layout.VBox;
import lwjgui.scene.layout.floating.FloatingPane;
import lwjgui.style.Background;
import lwjgui.style.BackgroundSolid;
import lwjgui.style.Percentage;
import lwjgui.transition.SizeTransition;

public class DeveloperConsole
{
	private static final Color BACKGROUND_COLOR = new Color("#1c0733");
	private static final Background BACKGROUND = new BackgroundSolid(BACKGROUND_COLOR);
	private static TextArea textArea;
	private TextField input;
	private static ConsoleSuggestions suggestions;

	private static boolean isVisible;
	
	private Pane pane;
	private VBox box;

	public DeveloperConsole(Scene scene)
	{
		createFrame(scene);
		
		//LoggerOutputStream.create();
	}

	private void createFrame(Scene scene)
	{
		box = new VBox();
		
		textArea = new TextArea();
		textArea.setDisabled(true);
		textArea.setFillToParentHeight(true);
		applyStyle(textArea);
		
		input = new TextField();
		applyStyle(input);
		
		box.getChildren().add(textArea);
		box.getChildren().add(input);
		box.setFillToParentWidth(true);
		
		pane = new FloatingPane();
		pane.setBackgroundLegacy(new Color(0, 0, 150, 191));
		pane.setPrefWidthRatio(Percentage.ONE_HUNDRED);
		pane.setMinHeight(0);
		pane.setAbsolutePosition(0, 0);
		
		pane.getChildren().add(box);
		pane.setVisible(false);
		
		suggestions = new ConsoleSuggestions(BACKGROUND_COLOR);
		
		//attachToUI(scene);
		
		// Update suggestions
		input.setOnTextChange((e) -> {
			if (!isVisible)
				return;
			
			suggestions.update(input.getText());
		});
		
		// Hacky fix to prevent the toggle key from appearing in the input
		input.setOnTextInput((e) -> {
			if (e.character == '`')
			{
				String txt = input.getText();
				
				if (txt.length() > 0)
					input.setText(txt.substring(0, txt.length() - 1));
			}
		});
		
		input.setOnKeyPressed((e) ->
		{
			if (!isVisible)
				return;
			
			switch (e.getKey())
			{
				case GLFW.GLFW_KEY_ENTER:
				{
					String txt = input.getText();
					
					println(txt);
					
					Command.processCommand(txt);
					input.setText("");
					input.setCaretPosition(0);
	
					suggestions.clear();
					suggestions.addHistory(txt);
					break;
				}
				case GLFW.GLFW_KEY_TAB:
				{
					if (!suggestions.isVisible())
						break;
					
					getSuggestion();
					
					suggestions.clear();
					break;
				}
				case GLFW.GLFW_KEY_UP:
				{
					suggestions.select(-1);
					if (suggestions.getSelectedId() < 0)
					{
						getSuggestion();
					}
					break;
				}
				case GLFW.GLFW_KEY_DOWN:
				{
					suggestions.select(1);
					
					if (suggestions.getSelectedId() < 0)
					{
						getSuggestion();
					}
					else if (suggestions.getSelectedId() == 0)
					{
						input.setText("");
						input.setCaretPosition(0);
					}
					break;
				}
					
				default:
				{
				}
			}
		});
	}

	private void getSuggestion()
	{
		input.setText(suggestions.getSuggestion());
		input.setCaretPosition(input.getText().length());
	}

	public void attachToUI(Scene scene)
	{
		Pane parent = ((Pane)scene.getRoot());
		parent.getChildren().add(pane);
	}

	private void applyStyle(TextInputControl field)
	{
		field.setFillToParentWidth(true);
		field.setBackground(BACKGROUND);
		field.setBorderColor(null);
		field.setTextFill(Color.WHITE);

		field.setSelectionOutlineEnabled(false);
	}

	public void forceClose()
	{
		isVisible = false;
		pane.setVisible(false);
		input.setVisible(false);
		textArea.setVisible(false);
		
		input.setText("");
		suggestions.clear();
		box.setPrefHeight(0);
		textArea.setPrefHeight(0);
	}
	
	// Flips the console on/off
	AtomicBoolean inAnimation = new AtomicBoolean(false);
	public void toggle()
	{
		if (inAnimation.get())
			return;
		
		isVisible = !isVisible;
		pane.setVisible(isVisible);
		input.setVisible(isVisible);
		textArea.setVisible(isVisible);
		
		input.setText("");
		
		if (isVisible)
		{
			LWJGUI.getThreadWindow().getContext().setSelected(input);
			
			SizeTransition transition = new SizeTransition(100, pane.getWidth(), 225) {
				@Override
				protected double getCurrentWidth() {
					return 0;
				}

				@Override
				protected double getCurrentHeight() {
					return 0;
				}

				@Override
				protected void setWidth(double width) {
				}

				@Override
				protected void setHeight(double height) {
					box.setPrefHeight(height);
					suggestions.updatePosition(height);
				}
				
				@Override
				public void completedCallback() {
					inAnimation.set(false);
					suggestions.updatePosition(225);
				}
			};
			
			inAnimation.set(true);
			transition.play();
		}
		else
		{
			suggestions.clear();
			
			box.setPrefHeight(0);
			textArea.setPrefHeight(0);
		}
	}

	public static void print(String text)
	{
		print(text, Color.WHITE);
	}

	public static void print(String text, Color color)
	{
		textArea.appendText(text);
	}
	
	public static void print(char c)
	{
		textArea.appendText(Character.toString(c));
	}

	public static void println(String text)
	{
		println(text, Color.WHITE);
	}

	public static void println(String text, Color color)
	{
		textArea.appendText(text + '\n');
	}

	public static void saveLogs()
	{
		BufferedWriter fileOut;
		try
		{
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm");
			String dateStr = dateFormat.format(cal.getTime());
			String filename = "log_" + dateStr + ".txt";

			fileOut = new BufferedWriter(new FileWriter(filename));
			fileOut.write(textArea.getText());
			fileOut.close();

			println("Logs saved to '" + filename + "'");
		} catch (Exception e)
		{
			println("Failed to save logs");
		}
	}

	public static boolean isVisible()
	{
		return isVisible;
	}

	public static ConsoleSuggestions suggestions()
	{
		return suggestions;
	}
}
