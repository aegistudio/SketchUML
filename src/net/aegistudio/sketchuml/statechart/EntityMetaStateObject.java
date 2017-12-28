package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.PropertyView;
import net.aegistudio.sketchuml.SketchView;

public class EntityMetaStateObject implements SketchView, PropertyView {
	public static int STATE_ROUNDSIZE = 50;
	public static int STATE_NAMEHEIGHT = 24;
	private JPanel viewObject;

	private EntityStateObject entity;
	private Consumer<Entity> notifier;
	
	private JTextField nameField;
	private JCheckBox isBrief;
	private JTextArea actionArea;
	
	private ActionListener safeAction(Runnable action) {
		return a -> {
			if(entity == null) return;
			if(notifier == null) return;
			action.run();
			notifier.accept(entity);
		};
	}
	
	@Override
	public Component getViewObject(Consumer<Entity> notifier) {
		if(viewObject == null) {
			Font font = Configuration.getInstance().PROPERTY_FONT;
			
			viewObject = new JPanel();
			viewObject.setLayout(new BoxLayout(viewObject, BoxLayout.Y_AXIS));
			
			// The state name.
			JPanel namePanel = new JPanel();
			namePanel.setLayout(new BorderLayout());
			JLabel nameLabel = new JLabel("State Name");
			nameField = new JTextField();
			nameField.addActionListener(safeAction(() -> {
				entity.name = nameField.getText();
			}));
			namePanel.add(nameLabel, BorderLayout.WEST);
			namePanel.add(nameField, BorderLayout.CENTER);
			viewObject.add(namePanel);
			
			// The is brief option.
			JPanel isBriefPanel = new JPanel();
			isBrief = new JCheckBox("Hide actions");
			isBrief.addActionListener(safeAction(() -> {
				entity.isBrief = isBrief.isSelected();
			}));
			isBriefPanel.setLayout(new GridLayout(1, 1));
			isBriefPanel.add(isBrief);
			viewObject.add(isBriefPanel);
			
			// The concrete action area.
			JPanel actionPanel = new JPanel();
			actionPanel.setLayout(new BoxLayout(
					actionPanel, BoxLayout.Y_AXIS));
			JLabel actionLabel = new JLabel("Actions:");
			JPanel actionLabelPanel = new JPanel();
			actionLabelPanel.setLayout(new BorderLayout());
			actionLabelPanel.add(actionLabel);
			actionPanel.add(actionLabelPanel);
			actionArea = new JTextArea();
			ActionListener areaListener = safeAction(() -> {
				entity.actions = actionArea.getText();
			});
			actionArea.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent fe) {
					areaListener.actionPerformed(null);
				}
			});
			
			actionArea.setRows(4);
			actionPanel.add(new JScrollPane(actionArea));
			viewObject.add(actionPanel);
			
			Arrays.asList((Component)
				nameLabel, nameField, isBrief, 
				actionLabel, actionArea
			).forEach(c -> c.setFont(font));
		}
		this.notifier = notifier;
		return viewObject;
	}

	@Override
	public void updateEntity(Entity entity) {
		this.entity = (EntityStateObject)entity;
		nameField.setText(this.entity.name);
		isBrief.setSelected(this.entity.isBrief);
		actionArea.setText(this.entity.actions);
	}

	@Override
	public void renderEntity(Graphics g, Entity entity, boolean preview) {
		Rectangle bound = g.getClipBounds();
		EntityStateObject entityState = (EntityStateObject)entity;
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		
		// Draw outline.
		g.setColor(Color.WHITE);
		g.fillRoundRect(1, 1, bound.width - 2, bound.height - 2, 
				STATE_ROUNDSIZE, STATE_ROUNDSIZE);
		g.setColor(preview? Color.GRAY : Color.BLACK);
		g.drawRoundRect(1, 1, bound.width - 2, bound.height - 2, 
				STATE_ROUNDSIZE, STATE_ROUNDSIZE);
		
		// Draw internal text.
		Rectangle2D nameMetric = g.getFontMetrics()
				.getStringBounds(entityState.name, g); 
		if(!entityState.isBrief) {
			g.drawLine(0, STATE_NAMEHEIGHT, 
					bound.width, STATE_NAMEHEIGHT);
			g.drawString(entityState.name, 
					(int)(bound.width - nameMetric.getWidth()) / 2, 
					(int)(STATE_NAMEHEIGHT + nameMetric.getHeight() / 2) / 2);
			
			Graphics actionGraphics = g.create(2, STATE_NAMEHEIGHT + 2, 
					bound.width - 4, bound.height - STATE_NAMEHEIGHT - 2);
			actionGraphics.drawString(entityState.actions, 0, 20);
		}
		else {
			// Draw string only.
			g.drawString(entityState.name, 
					(int)(bound.width - nameMetric.getWidth()) / 2, 
					(int)(bound.height + nameMetric.getHeight() / 2) / 2);
		}
	}

}
