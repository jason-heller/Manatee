package manatee.client.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import lwjgui.event.ActionEvent;
import lwjgui.event.EventHandler;
import lwjgui.event.EventHelper;
import lwjgui.geometry.Insets;
import lwjgui.paint.Color;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.WindowHandle;
import lwjgui.scene.WindowManager;
import lwjgui.scene.WindowThread;
import lwjgui.scene.control.Button;
import lwjgui.scene.control.Label;
import lwjgui.scene.control.ScrollPane;
import lwjgui.scene.control.TextField;
import lwjgui.scene.control.TreeItem;
import lwjgui.scene.control.TreeView;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.HBox;

public class FileChooser
{
	private static Path path;
	public static String fileName;

	public static void create(String action, String extension, boolean openAction, String dir, EventHandler<ActionEvent> event) {
		
		WindowManager.runLater(() -> {
			new WindowThread(500, 400, action) {
				@Override
				protected void setupHandle(WindowHandle handle) {
					super.setupHandle(handle);
					handle.canResize(false);
				}
				
				@Override
				protected void init(Window window) {
					super.init(window);
					
					TextField input = new TextField("");
					TreeView<String> fileView = new TreeView<>();;
					TextField pathField = new TextField(dir);
					
					BorderPane root = new BorderPane();
					root.setPadding(new Insets(8));
					root.setSpacing(8.0);
					
					HBox top = new HBox();
					top.setFillToParentWidth(true);
					{
						Button back = new Button("Back");
						back.setOnAction((e) -> {
							openDirectory(path.getParent(), fileView, input, pathField);
							pathField.setText(path.toString());
						});
						
						pathField.setFillToParentWidth(true);
						pathField.setOnKeyPressed((keyEvent) -> {
							if (keyEvent.getKey() == GLFW.GLFW_KEY_ENTER)
							{
								Path p = Paths.get(pathField.getText());
								if (Files.exists(p) && Files.isDirectory(p))
									openDirectory(p, fileView, input, pathField);
							}
						});
						
						Button go = new Button("Go");
						go.setOnAction((e) -> {
							Path p = Paths.get(pathField.getText());
							if (Files.exists(p) && Files.isDirectory(p))
								openDirectory(p, fileView, input, pathField);
						});

						top.getChildren().add(back);
						top.getChildren().add(pathField);
						top.getChildren().add(go);
					}
					root.setTop(top);
					
					ScrollPane center = new ScrollPane();
					center.setFillToParentWidth(true);
					center.setFillToParentHeight(true);
					{
						openDirectory(Paths.get(dir), fileView, input, pathField);
						
						center.setContent(fileView);
					}
					root.setCenter(center);
					
					HBox bottom = new HBox();
					bottom.setSpacing(8f);
					bottom.setFillToParentWidth(true);
					{
						input.setFillToParentWidth(true);
						input.setOnTextChange((e) -> {
							FileChooser.fileName = input.getText();
						});
						
						EventHandler<ActionEvent> executeEvent = (e) -> {
							
							Path filePath = Paths.get(FileChooser.fileName);
		
							if (Files.isDirectory(filePath))
							{
								openDirectory(path, fileView, input, pathField);
							}
							else if (Files.exists(filePath) && !openAction)
							{
								overwriteConfirm((ev) -> {
									EventHelper.fireEvent(event, new ActionEvent());
									window.close();
								});
							}
							else
							{
								EventHelper.fireEvent(event, new ActionEvent());
								window.close();
							}
						};
						
						input.setOnKeyPressed((keyEvent) -> {
							if (keyEvent.getKey() == GLFW.GLFW_KEY_ENTER)
							{
								EventHelper.fireEvent(executeEvent, new ActionEvent());
							}
						});
						
						fileView.setOnKeyPressed((keyEvent) -> {
							if (keyEvent.getKey() == GLFW.GLFW_KEY_ENTER)
							{
								/*Path p = Paths.get(fileName);
								if (Files.exists(p) && Files.isDirectory(p))
									openDirectory(p, fileView, input, pathField);
								else*/
									EventHelper.fireEvent(executeEvent, new ActionEvent());
							}
						});
						
						bottom.getChildren().add(input);
						
						Button btn = new Button(action);
						btn.setOnAction(executeEvent);
						bottom.getChildren().add(btn);
					}
					root.setBottom(bottom);
					
					window.setScene(new Scene(root, 500, 400));
					window.show();
				}

				
				private void openDirectory(Path path, TreeView<String> fileView, TextField input, TextField pathField)
				{
					FileChooser.path = path;
					fileView.getItems().clear();
					Set<Path> paths = null;
					try
					{
						paths = getAllFilesIn(path, extension);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					for(Path p : paths)
					{
						String fName = p.getFileName().toString();
						
						TreeItem<String> item = new TreeItem<>(fName);
						fileView.getItems().add(item);
						
						if (Files.isDirectory(p))
							item.setBorderColor(Color.MAGENTA);
						
						item.setOnMouseClicked((e)->{
							
							if (Files.isDirectory(p))
							{
								openDirectory(p, fileView, input, pathField);
								pathField.setText(p.toString());
							}
							else
							{
								input.setText(item.getText());
							}
						});
					}
				}
			}.start();
		});
	}
	
	private static Set<Path> getAllFilesIn(Path parent, String extension) throws IOException
	{

		Set<Path> fileList = new HashSet<>();
        Files.walk(parent)
        	.filter(path -> !path.equals(parent))
	        .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
	        .filter(path -> Files.isDirectory(path) || path.toString().toLowerCase().endsWith("." + extension.toLowerCase()))
            .forEach(fileList::add);
        
        return fileList;
	}
	
	private static void overwriteConfirm(EventHandler<ActionEvent> event)
	{
		WindowManager.runLater(() -> {
			new WindowThread(400, 400, "Overwrite?") {
				@Override
				protected void setupHandle(WindowHandle handle) {
					super.setupHandle(handle);
					handle.canResize(false);
				}
				
				@Override
				protected void init(Window window) {
					super.init(window);
					
					BorderPane root = new BorderPane();
					
					root.setTop(new Label("Overwrite file?"));
					
					HBox actions = new HBox();
					root.setBottom(actions);
					
					Button yes = new Button("Yes");
					yes.setOnAction((e) -> {
						EventHelper.fireEvent(event, new ActionEvent());
					});
					
					Button no = new Button("No");
					no.setOnAction((e) -> {
						window.close();
					});
					
					window.setScene(new Scene(root, 400, 400));
					window.show();
				}
			}.start();
		});
	}
}
