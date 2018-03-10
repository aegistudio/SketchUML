package net.aegistudio.sketchuml;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import net.aegistudio.sketchuml.framework.CandidatePanel;
import net.aegistudio.sketchuml.framework.CheatSheetGraphics;
import net.aegistudio.sketchuml.framework.ComponentEditPanel;
import net.aegistudio.sketchuml.framework.DefaultSketchModel;
import net.aegistudio.sketchuml.framework.SketchPanel;
import net.aegistudio.sketchuml.path.TrifoldProxyPath;
import net.aegistudio.sketchuml.path.TrifoldPathManager;
import net.aegistudio.sketchuml.path.BezierPathView;
import net.aegistudio.sketchuml.path.PathEditor;
import net.aegistudio.sketchuml.path.TrifoldPathEditor;
import net.aegistudio.sketchuml.statechart.TemplateStateChart;
import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public class Main {
	public static Template[] templates = { new TemplateStateChart() };
	public static Map<String, Font> fonts = new HashMap<>();
	public static ComponentEditPanel<TrifoldProxyPath> editPanel;
	public static SketchPanel<TrifoldProxyPath> sketchPanel;
	
	public static void main(String[] arguments) {
		// Set the UI's major look and feel. Could fail.
		try { UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); } 
		catch (Exception e) {	}
		
		// Fetch all available fonts.
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for(Font font : fonts) Main.fonts.put(font.getName(), font);
		
		// Initialize font configuration.
		Configuration.getInstance().HANDWRITING_FONT = Main.fonts.get(
				Configuration.getInstance().HANDWRITING_FONTNAME).deriveFont(
					Configuration.getInstance().HANDWRITING_FONTSTYLE, 
					Configuration.getInstance().HANDWRITING_FONTSIZE);
		Configuration.getInstance().EDITING_FONT = Main.fonts.get(
				Configuration.getInstance().EDITING_FONTNAME).deriveFont(
					Configuration.getInstance().EDITING_FONTSTYLE,
					Configuration.getInstance().EDITING_FONTSIZE);
		Configuration.getInstance().PROPERTY_FONT = Main.fonts.get(
				Configuration.getInstance().PROPERTY_FONTNAME).deriveFont(
					Configuration.getInstance().PROPERTY_FONTSTYLE,
					Configuration.getInstance().PROPERTY_FONTSIZE);
		
		// Create the path manager and view.
		TrifoldPathManager pathManager = new TrifoldPathManager();
		BezierPathView<TrifoldProxyPath> pathView = new BezierPathView<>();
		
		// Initialize the recognizers.
		SketchRecognizer recognizer = new SketchRecognizer(
				new File(Configuration.getInstance().GESTURE_PATH), 
				templates[0].entities());
		DefaultSketchModel<TrifoldProxyPath> model = 
				new DefaultSketchModel<>(templates[0], 
						recognizer, pathView, pathManager);
		recognizer.initializeNDollar();
		
		// Create the main frame.
		JFrame frame = new JFrame();
		frame.setTitle("SketchUML");
		frame.setLocationRelativeTo(null);
		frame.setLocation(0, 0);
		frame.setSize(1400, 768);
		
		// Add the title bar.
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		// Add the file menu.
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic('F');
		menuBar.add(menuFile);
		
		// Add the edit menu.
		JMenu menuEdit = new JMenu("Edit");
		menuEdit.setMnemonic('E');
		menuBar.add(menuEdit);
		
		// Add the help menu.
		JMenu menuHelp = new JMenu("Help");
		menuHelp.setMnemonic('H');
		menuBar.add(menuHelp);
		
		JMenuItem menuItemCheatSheet = 
				new JMenuItem("Show/Hide Cheat Sheet");
		menuItemCheatSheet.addActionListener(a -> {
			sketchPanel.displayUsage = !sketchPanel.displayUsage;
			frame.repaint();
		});
		menuItemCheatSheet.setAccelerator(KeyStroke.getKeyStroke("F1"));
		menuHelp.add(menuItemCheatSheet);
		
		// Set the menu's text font.
		for(int i = 0; i < menuBar.getMenuCount(); ++ i) {
			JMenu menu = menuBar.getMenu(i);
			menu.setFont(Configuration
					.getInstance().PROPERTY_FONT);
			for(int j = 0; j < menu.getItemCount(); ++ j)
				menu.getItem(j).setFont(Configuration
						.getInstance().PROPERTY_FONT);
		}
		
		// Add the result selection panel.
		CandidatePanel candidatePanel = new CandidatePanel();
		frame.add(candidatePanel, BorderLayout.SOUTH);
		
		// Create the sketch painting panel.
		CheatSheetGraphics cheatSheet = null;
		try { cheatSheet = new CheatSheetGraphics("en_US"); }
		catch(IOException e) {e.printStackTrace();}
		sketchPanel = new SketchPanel<>(candidatePanel, model, 
				pathManager, pathView, cheatSheet);
		frame.add(sketchPanel, BorderLayout.CENTER);
		
		// Create the property panels.
		editPanel = new ComponentEditPanel<>(model, new TrifoldPathEditor(),
			new PathEditor.PathChangeListener<TrifoldProxyPath>() {

				@Override
				public Object beforeChange(TrifoldProxyPath previousPath) {
					return null;
				}

				@Override
				public void receiveChange(Object beforeChange, 
						TrifoldProxyPath newPath) {
					if(editPanel == null) return;
					model.notifyLinkStyleChanged(editPanel.linkEditor);
				}
			});
		frame.add(editPanel, BorderLayout.EAST);
		
		// Create the keyboard capture's listener.
		frame.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				Point mouseLocation = MouseInfo.getPointerInfo()
						.getLocation();
				Point sketchLocation = sketchPanel.getLocationOnScreen();
				if(sketchPanel.contains(new Point(
						mouseLocation.x - sketchLocation.x, 
						mouseLocation.y - sketchLocation.y)))
					sketchPanel.keyPressed(ke);
			}
		});
		
		// Show the main user interface.
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
