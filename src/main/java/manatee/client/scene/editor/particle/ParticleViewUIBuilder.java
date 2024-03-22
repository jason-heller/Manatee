package manatee.client.scene.editor.particle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Vector3f;
import org.joml.Vector4f;

import lwjgui.event.Event;
import lwjgui.event.EventHandler;
import lwjgui.geometry.Insets;
import lwjgui.geometry.Pos;
import lwjgui.paint.Color;
import lwjgui.scene.Node;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.WindowHandle;
import lwjgui.scene.WindowManager;
import lwjgui.scene.WindowThread;
import lwjgui.scene.control.Button;
import lwjgui.scene.control.ColorPicker;
import lwjgui.scene.control.ComboBox;
import lwjgui.scene.control.Label;
import lwjgui.scene.control.ScrollPane;
import lwjgui.scene.control.Slider;
import lwjgui.scene.control.TextField;
import lwjgui.scene.control.TreeItem;
import lwjgui.scene.control.TreeView;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.HBox;
import lwjgui.scene.layout.OpenGLPane;
import lwjgui.scene.layout.Pane;
import lwjgui.scene.layout.VBox;
import lwjgui.scene.layout.floating.FloatingPane;
import lwjgui.style.Percentage;
import manatee.cache.definitions.texture.ITexture;
import manatee.client.Client;
import manatee.client.dev.Dev;
import manatee.client.gl.camera.ControllableCamera;
import manatee.client.gl.camera.ICamera;
import manatee.client.gl.particle.ParticleManager;
import manatee.client.gl.particle.ParticleSystem;
import manatee.client.gl.particle.attribs.ColorParticleAttrib;
import manatee.client.gl.particle.attribs.FadeOutParticleAttrib;
import manatee.client.gl.particle.attribs.GravityParticleAttrib;
import manatee.client.gl.particle.attribs.IParticleAttrib;
import manatee.client.gl.particle.attribs.ImpulseParticleAttrib;
import manatee.client.gl.particle.attribs.LifeParticleAttrib;
import manatee.client.gl.particle.attribs.Rotate3dParticleAttrib;
import manatee.client.gl.particle.attribs.ScaleParticleAttrib;
import manatee.client.gl.particle.attribs.ScaleWobbleParticleAttrib;
import manatee.client.gl.particle.attribs.SwayParticleAttrib;
import manatee.client.gl.particle.attribs.WindParticleAttrib;
import manatee.client.gl.particle.attribs.emitter.BoxEmitterParticleAttrib;
import manatee.client.gl.particle.attribs.emitter.CircleEmitterParticleAttrib;
import manatee.client.gl.particle.attribs.emitter.RectEmitterParticleAttrib;
import manatee.client.gl.particle.attribs.emitter.SphereEmitterParticleAttrib;
import manatee.client.gl.particle.mesh.MeshParticleSystem;
import manatee.client.scene.IScene;
import manatee.client.ui.ClientRenderer;
import manatee.client.ui.UIBuilder;

public class ParticleViewUIBuilder implements UIBuilder
{

	private OpenGLPane oglPane;

	private TreeView<String> attached;

	private TreeView<String> detached;
	
	private Map<Class<?>, Map<String, Object>> attribValues = new HashMap<>();

	private VBox attribEditorBox;

	private HBox attribChooserBox;
	
	private static ComboBox<String> allSystems;
	
	public static Class<?>[] attribClasses = {
		ColorParticleAttrib.class,
		FadeOutParticleAttrib.class,
		GravityParticleAttrib.class,
		ImpulseParticleAttrib.class,
		LifeParticleAttrib.class,
		Rotate3dParticleAttrib.class,
		ScaleParticleAttrib.class,
		ScaleWobbleParticleAttrib.class,
		SwayParticleAttrib.class,
		WindParticleAttrib.class,
		
		BoxEmitterParticleAttrib.class,
		CircleEmitterParticleAttrib.class,
		RectEmitterParticleAttrib.class,
		SphereEmitterParticleAttrib.class
		
	};

	private void propertiesBox(VBox box, ParticleViewScene scene)
	{
		ScrollPane attribsAttached = new ScrollPane();
		ScrollPane attribsDetached = new ScrollPane();
		attribsAttached.setPrefSize(250, 200);
		attribsDetached.setPrefSize(250, 200);
		
		attribEditorBox = new VBox();
		
		attribPanes(attribsAttached, attribsDetached);
		
		{
			allSystems = new ComboBox<String>("---");
			
			for(String s : scene.getParticleSystemNames())
				allSystems.getItems().add(s);
			
			allSystems.setOnAction((e) -> {
				scene.particleType = allSystems.getValue();
				
				ParticleSystem ps = scene.getParticleManager().getParticleSystem(scene.particleType);

				attribPanes(attribsAttached, attribsDetached);
				
				if (ps != null)
				{
					attribChooserBox.setVisible(true);
					attribValues.clear();
					for(IParticleAttrib attrib : ps.getAttributes())
					{
						Map<String, Object> data = new LinkedHashMap<>();
						for(Field f : attrib.getClass().getFields())
						{
							try
							{
								data.put(f.getName(), f.get(attrib));
							}
							catch (IllegalArgumentException | IllegalAccessException e1)
							{
								e1.printStackTrace();
							}
						}
						
						String className = attrib.getClass().getSimpleName();
						attribValues.put(attrib.getClass(), data);
						TreeItem<String> item = null;
						for(int i = 0; i < detached.getItems().size(); i++)
						{
							item = detached.getItems().get(i);
							if (item.getText().equals(className))
								break;
						}
						attached.getItems().add(item);
						detached.getItems().remove(item);
						setProperties(scene, className);
						
						item.setOnMouseClicked((ev)-> {
							setProperties(scene, className);
						});
					}
				}
				scene.resetEmitters();
				//attribBox.getChildren().clear();
			});
			
			add(box, "Effect", allSystems);

			//
			
			HBox hb = new HBox();
			
			Button b1 = new Button("New Effect");
			b1.setOnAction((e)-> {
				namePopup(scene);
			});
			
			Button b2 = new Button("Delete Effect");
			b2.setOnAction((e)-> {
				scene.getParticleManager().removeSystem(scene.particleType);
				allSystems.getItems().remove(scene.particleType);
				allSystems.setValue("---");
				attribChooserBox.setVisible(false);
				
				scene.resetEmitters();
			});
			
			hb.getChildren().add(b1);
			hb.getChildren().add(b2);
			
			box.getChildren().add(hb);
		}
		
		{
			ComboBox<String> combo = new ComboBox<String>();
			
			for(String s : ParticleManager.meshes.keySet())
				combo.getItems().add(s);
			
			for(String s : ParticleManager.textures.keySet())
				combo.getItems().add(s);
			
			combo.setOnAction((e) -> {
				ParticleSystem ps = scene.getParticleManager().getParticleSystem(scene.particleType);
				if (ps != null)
				{
					String value = combo.getValue();
					
					if (ParticleManager.textures.containsKey(value))
					{
						ITexture tex = ParticleManager.textures.get(value);
						int atlasWidth = ParticleManager.textureAtlasSizes.get(value);
						scene.getParticleManager().removeSystem(scene.particleType);
						scene.getParticleManager().addSystem(scene.particleType, tex, atlasWidth, true, false, ps.getAttributes());
					}
					else
					{
						int mid = ParticleManager.meshIndices.get(value);
						((MeshParticleSystem)ps).setMeshIndex(mid);
						scene.getParticleManager().removeSystem(scene.particleType);
						scene.getParticleManager().addSystem(scene.particleType, mid, ps.getAttributes());
					}
					scene.resetEmitters();
				}
			});
			
			add(box, "Particle Type", combo);
		}
		
		{
			/*TextField tf = new TextField("1.0");
			tf.setOnTextChange((e) -> {
				try {
					scene.pps = Float.parseFloat(tf.getText());
					scene.resetEmitters();
				} catch (Exception ignored) {}
			});

			add(box, "Particles Per Second", tf);*/
			Slider s = new Slider(.25, 100, 10);
			s.setPrefWidth(400);
			s.setOnValueChangedEvent((e) -> {
				try {
					scene.pps = (float)s.getValue();
					scene.resetEmitters();
				} catch (Exception ignored) {}
			});

			add(box, "Particles Per Second", s);
		}
		
		{
			Slider s = new Slider(1, 10, 1);
			s.setOnValueChangedEvent((e) -> {
				try {
					scene.ppe = (int) s.getValue();
					scene.resetEmitters();
				} catch (Exception ignored) {}
			});

			add(box, "Particles Per Emission", s);
		}
		
		{
			Slider s = new Slider(-1, 1, 0);
			s.setOnValueChangedEvent((e) -> {
				try {
					scene.ppsVariance = (float) s.getValue();
					scene.resetEmitters();
				} catch (Exception ignored) {}
			});

			add(box, "Particle Emission Variance", s);
		}
		
		{
			attribChooserBox = new HBox();
			attribChooserBox.setVisible(false);
			attribChooserBox.setFillToParentWidth(true);
			VBox vb1 = new VBox();
			VBox vb2 = new VBox();
			add(vb1, "Attached Attributes", attribsAttached);
			add(vb2, "Available Attributes", attribsDetached);
			
			Button b1 = new Button("< Remove");
			b1.setOnAction((e) -> {
				TreeItem<String> item = attached.getLastSelectedItem();
				if (item != null)
				{
					attached.getItems().remove(item);
					detached.getItems().add(item);
					attribValues.remove(getClassByName(item.getText()));
					updateAttribs(scene);
				}
			});
			vb1.getChildren().add(b1);
			
			Button b2 = new Button("Attach >");
			vb2.getChildren().add(b2);
			
			b2.setOnAction((e) -> {
				TreeItem<String> item = detached.getLastSelectedItem();
				if (item != null)
				{
					detached.getItems().remove(item);
					attached.getItems().add(item);
					
					attribValues.put(getClassByName(item.getText()), new LinkedHashMap<>());
					setProperties(scene, item.getText());
					
					item.setOnMouseClicked((ev)-> {
						setProperties(scene, item.getText());
					});
					updateAttribs(scene);
				}
			});

			attribChooserBox.getChildren().add(vb2);
			attribChooserBox.getChildren().add(vb1);
			
			add(box, "---", attribChooserBox);
		}
		
		add(box, "Properties", attribEditorBox);
		
		{
			Button b = new Button("Save");
			b.setOnAction((e) -> {
				scene.save("src/main/resources/data/particles.txt");
			});
			
			box.getChildren().add(b);
		}
		
		{
			Button b = new Button("Restart");
			b.setOnAction((e) -> {
				Client.launchParticleView();
			});
			
			box.getChildren().add(b);
		}
	}

	private void setProperties(ParticleViewScene scene, String text)
	{
		attribEditorBox.getChildren().clear();

		Class<?> cl = getClassByName(text);

		int fieldId = -1;
		for(Field f : cl.getFields())
		{
			Class<?> c = cl;
			fieldId++;
			
			if (f.getType() == Vector4f.class)
			{
				ColorPicker cp = new ColorPicker();
				Vector4f v = (Vector4f)getFieldValue(cl, fieldId, scene.getParticleManager().getParticleSystem(scene.particleType));
				if (v == null)
					v = new Vector4f(1,1,1,1);
				
				cp.setColor(new Color(v.x,v.y,v.z,1f));
				
				attribValues.get(c).put(f.getName(), cp.getColor().getVector());
				
				cp.setOnColorUpdate((e) -> {
					try {
						scene.resetEmitters();
						attribValues.get(c).put(f.getName(), cp.getColor().getVector());
						updateAttribs(scene);
						
					} catch (Exception ignored) {}
				});
				add(attribEditorBox, f.getName(), cp);
			}
			else if (f.getType() == Vector3f.class)
			{
				HBox hb = new HBox();
				TextField x, y, z;
				Vector3f v = (Vector3f)getFieldValue(cl, fieldId, scene.getParticleManager().getParticleSystem(scene.particleType));
				if (v == null)
					v = new Vector3f();
				
				x = new TextField("" + v.x);
				y = new TextField("" + v.y);
				z = new TextField("" + v.z);
				hb.getChildren().add(x);
				hb.getChildren().add(y);
				hb.getChildren().add(z);
				
				attribValues.get(c).put(f.getName(), new Vector3f(
						Float.parseFloat(x.getText()),
						Float.parseFloat(y.getText()),
						Float.parseFloat(z.getText()))
						);
				
				EventHandler<Event> evt = (e) -> {
					try {
						scene.resetEmitters();
						attribValues.get(c).put(f.getName(), new Vector3f(
								Float.parseFloat(x.getText()),
								Float.parseFloat(y.getText()),
								Float.parseFloat(z.getText()))
								);
						updateAttribs(scene);
						
					} catch (Exception ignored) {}
				};
				
				x.setOnTextChange(evt);
				y.setOnTextChange(evt);
				z.setOnTextChange(evt);
				
				add(attribEditorBox, f.getName(), hb);
			}
			else
			{
				Float fl = (Float)getFieldValue(cl, fieldId, scene.getParticleManager().getParticleSystem(scene.particleType));
				if (fl == null)
					fl = 0f;
				
				TextField tf = new TextField("" + fl);
				attribValues.get(c).put(f.getName(), Float.parseFloat(tf.getText()));
				
				tf.setOnTextChange((e) -> {
					try {
						scene.resetEmitters();
						attribValues.get(c).put(f.getName(), Float.parseFloat(tf.getText()));
						updateAttribs(scene);
						
					} catch (Exception ignored) {}
				});
				
				add(attribEditorBox, f.getName(), tf);
			}
		}
	}

	private Object getFieldValue(Class<?> cl, int fieldId, ParticleSystem particleSystem)
	{
		for(IParticleAttrib attrib : particleSystem.getAttributes())
		{
			if (attrib.getClass() == cl)
			{
				try
				{
					return attrib.getClass().getFields()[fieldId].get(attrib);
				}
				catch (IllegalArgumentException | IllegalAccessException | SecurityException e)
				{
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
	}

	private Class<?> getClassByName(String text)
	{
		for(Class<?> c : attribClasses)
		{
			if (c.getSimpleName().equals(text))
			{
				return c;
			}
		}
		
		return ColorParticleAttrib.class;
	}

	private void updateAttribs(ParticleViewScene scene)
	{
		IParticleAttrib[] attribs = new IParticleAttrib[attribValues.keySet().size()];
		
		int i = 0;
		for(Entry<Class<?>, Map<String, Object>> entry : attribValues.entrySet())
		{
			Constructor<?> constructor = entry.getKey().getConstructors()[0];
			// If this doesn't work, iter thru constructors and find the one with only floats
			
			Object[] values = new Object[entry.getValue().size()];
			int j = 0;
			for(Object o : entry.getValue().values())
				values[j++] = o;
			
			if (values.length != constructor.getParameterCount())
			{
				Dev.log("Wrong number of args", values.length, constructor.getParameterCount());
				return;
			}
			
			IParticleAttrib obj = null;
			try
			{
				obj = (IParticleAttrib)constructor.newInstance(values);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e)
			{
				e.printStackTrace();
				return;
			}
			
			attribs[i++] = obj;
		}
		
		scene.setAttributes(scene.particleType, attribs);
	}

	private void attribPanes(ScrollPane attribsAttached, ScrollPane attribsDetached)
	{
		attached = new TreeView<String>();
		detached = new TreeView<String>();
		
		for(Class<?> attribClass : attribClasses)
		{
			detached.getItems().add(new TreeItem<String>(attribClass.getSimpleName()));
		}
		
		attribsAttached.setContent(attached);
		attribsDetached.setContent(detached);
	}

	private void add(Pane p, String labelText, Node node)
	{
		Label l = new Label(labelText);
		
		p.getChildren().add(l);
		p.getChildren().add(node);
	}

	@Override
	public Node buildUI(IScene gameScene, ClientRenderer clientRenderer)
	{
		FloatingPane root = new FloatingPane();
		BorderPane borderPane = new BorderPane();
		
		oglPane = new OpenGLPane();
		oglPane.setFillToParentHeight(true);
		oglPane.setFillToParentWidth(true);
		oglPane.setMouseTransparent(false);
		borderPane.setCenter(oglPane);
		
		VBox properties = new VBox();
		properties = new VBox();
		properties.setSpacing(8);
		properties.setAlignment(Pos.TOP_LEFT);
		properties.setPrefWidthRatio(new Percentage(30));
		properties.setFillToParentHeight(true);
		properties.setBackgroundLegacy(Color.GRAY);
		properties.setPadding(new Insets(10f));
		
		propertiesBox(properties, (ParticleViewScene)gameScene);
		
		borderPane.setRight(properties);
		
		root.getChildren().add(borderPane);
		
		oglPane.setOnMouseEntered(e ->
		{
			ICamera camera = gameScene.getCamera();

			if (camera instanceof ControllableCamera)
				((ControllableCamera) camera).setDraggable(true);
		});

		oglPane.setOnMouseExited(e ->
		{
			;
			ICamera camera = gameScene.getCamera();

			if (camera instanceof ControllableCamera)
				((ControllableCamera) camera).setDraggable(false);
		});

		oglPane.setRendererCallback(clientRenderer);
		
		return root;
	}

	@Override
	public OpenGLPane getWorldRendererPane()
	{
		return oglPane;
	}

	@Override
	public void initImages()
	{
	}
	
	public static void namePopup(ParticleViewScene scene) {
		
		WindowManager.runLater(() -> {
			new WindowThread(500, 150, "New Particle System") {
				@Override
				protected void setupHandle(WindowHandle handle) {
					super.setupHandle(handle);
					handle.canResize(false);
				}
				@Override
				protected void init(Window window) {
					super.init(window);
					
					BorderPane root = new BorderPane();
					root.setPadding(new Insets(32,32,32,32));
					
	
					HBox box = new HBox();
					box.setSpacing(8f);
					box.setFillToParentWidth(true);
					Label l = new Label("Name:");
					TextField ta = new TextField("New Effect");
					ta.setFillToParentWidth(true);
					
					box.getChildren().add(l);
					box.getChildren().add(ta);
					
					root.setCenter(box);
					
					Button btn = new Button("Create");
					btn.setOnAction((event)-> {
						
						scene.newEffect = ta.getText();
						window.close();
					});
					
					root.setBottom(btn);
					
					window.setScene(new Scene(root, 500, 150));
					window.show();
				}
			}.start();
		});
	}

	public void handleNewEffect(String name)
	{

		allSystems.getItems().add(name);
		allSystems.setValue(name);
	}
}
