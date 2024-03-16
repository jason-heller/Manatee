package lwjgui.scene.control;

import lwjgui.collections.ObservableList;

public class ToggleGroup {
	private ObservableList<Toggle> toggleables = new ObservableList<Toggle>();
	private Toggle currentSelected = null;
	private boolean maintainSelection;
	
	public ToggleGroup()
	{
		this(false);
	}
	
	public ToggleGroup(boolean maintainSelection)
	{
		this.maintainSelection = maintainSelection;
	}
	
	public void add(Toggle b) {
		this.toggleables.add(b);
	}
	
	public void remove(Toggle b) {
		this.toggleables.remove(b);
	}

	/**
	 * Selects a Toggle object
	 * @param b
	 */
	public void selectToggle(Toggle b) {
		currentSelected = b;
		
		for (int i = 0; i < toggleables.size(); i++) {
			Toggle t = toggleables.get(i);
			
			if ( !t.equals(b) ) {
				t.setSelected(false);
			}
		}
		b.setSelected(true);
	}

	public boolean isMaintainsSelection()
	{
		return this.maintainSelection;
	}

	/**
	 * 
	 * @return Returns the currently selected Toggle object.
	 */
	public Toggle getCurrectSelected() {
		return this.currentSelected;
	}

	/**
	 * 
	 * @return Returns the list of toggles within the ToggleGroup.
	 */
	public ObservableList<Toggle> getToggles() {
		return toggleables;
	}
}
