package net.aegistudio.sketchuml;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * Represents a group of JMenuRadioItem that is initialized
 * and could be selected according to the underlying concrete
 * types.
 * 
 * @author Haoran Luo
 */
public abstract class RadioMenuItemGroup<T> {
	private final ButtonGroup menuItemGroup;
	private final List<T> menuItemObjects;
	private final List<JRadioButtonMenuItem> menuItems;
	private final List<ButtonModel> menuModels;
	
	public RadioMenuItemGroup() {
		this.menuItemGroup = new ButtonGroup();
		this.menuItemObjects = new ArrayList<>();
		this.menuItems = new ArrayList<>();
		this.menuModels = new ArrayList<>();
	}
	
	public void addItem(T t, String name, boolean selected) {
		menuItemObjects.add(t);
		JRadioButtonMenuItem item = new 
				JRadioButtonMenuItem(name, selected);
		item.addActionListener(a -> {
			int index = menuModels.indexOf(
					menuItemGroup.getSelection());
			if(index < 0) return;
			selectItem(menuItemObjects.get(index));
		});
		menuItems.add(item);
		menuModels.add(item.getModel());
		menuItemGroup.add(item);
	}
	
	public void addMenu(JMenu menu) {
		menuItems.forEach(menu::add);
	}
	
	public abstract void selectItem(T t);
}
