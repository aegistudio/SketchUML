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
import javax.swing.filechooser.FileFilter;

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
	// The version of the SketchUML software.
	// Should be in format MMMMmmmmRRRR, where M is major
	// version, m is minor version and R is revision.
	public static int softwareVersion = 001000000000;
	
	// The version of the SketchUML's persistent
	// format, each time the major structure of the file
	// is changed, this value should be changed.
	public static int formatVersion = 001000000001;
	
	// The available templates, current template and model.
	public static Template[] templates = { new TemplateStateChart() };
	public static Template currentTemplate;
	public static DefaultSketchModel<TrifoldProxyPath> currentModel;
	public static File previousFile;
	
	// The available fonts in the system.
	public static Map<String, Font> fonts = new HashMap<>();
	
	// UI components inside the main frame.
	public static JFrame mainFrame;
	public static ComponentEditPanel<TrifoldProxyPath> editPanel;
	public static SketchPanel<TrifoldProxyPath> sketchPanel;
	public static CandidatePanel candidatePanel;
	public static CheatSheetGraphics cheatSheet;
	
	// The path manager and view.
	public static TrifoldPathManager pathManager;
	public static BezierPathView<TrifoldProxyPath> pathView;
	
	// The sketch recognizer and current template.
	public static SketchRecognizer recognizer;
	
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
		UIManager.put("OptionPane.messageFont", Configuration
				.getInstance().PROPERTY_FONT);
		
		// Create the path manager and view.
		pathManager = new TrifoldPathManager();
		pathView = new BezierPathView<>();
		
		// Create the main frame.
		mainFrame = new JFrame();
		mainFrame.setTitle("SketchUML");
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setLocation(0, 0);
		mainFrame.setSize(1400, 768);
		
		// Initialize the persistent object.
		SketchPersistence<TrifoldProxyPath> persistence 
			= new SketchPersistence<TrifoldProxyPath>(formatVersion, templates) {
			
			FileFilter previousFilter = null;
			
			public DefaultSketchModel<TrifoldProxyPath> 
				newSketchModel(Template template) {
				
				return new DefaultSketchModel<>(
						template, pathView, pathManager);
			}
			
			public File getPreviousFile() {
				return previousFile == null? 
						new File(".") : previousFile;
			}
			
			public FileFilter getPreviousFormat() {
				return previousFilter;
			}
			
			public void setPreviousStatus(File file, FileFilter format) {
				previousFile = file;
				previousFilter = format;
			}
		};
		
		// Add the title bar.
		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);
		
		// Add the file menu.
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic('F');
		menuBar.add(menuFile);
		
		JMenuItem menuItemOpen = new JMenuItem("Open...");
		menuItemOpen.addActionListener(a -> {
			SketchPersistence.SketchFile<TrifoldProxyPath> fileModel = 
					 new SketchPersistence.SketchFile<>();
			if(!persistence.openModelPanel(mainFrame, fileModel)) return;
			resetSketchModel(fileModel.template, 
					(DefaultSketchModel<TrifoldProxyPath>)
					fileModel.sketchModel);
		});
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
		menuFile.add(menuItemOpen);
		
		JMenuItem menuItemSave = new JMenuItem("Save as...");
		menuItemSave.addActionListener(a -> {
			SketchPersistence.SketchFile<TrifoldProxyPath> fileModel = 
					 new SketchPersistence.SketchFile<>();
			fileModel.formatVersion = formatVersion;
			fileModel.template = currentTemplate;
			fileModel.sketchModel = currentModel;
			if(!persistence.saveModelPanel(mainFrame, fileModel)) return;
		});
		menuItemSave.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		menuFile.add(menuItemSave);
		
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
			mainFrame.repaint();
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
		candidatePanel = new CandidatePanel();
		mainFrame.add(candidatePanel, BorderLayout.SOUTH);
		
		// Create the sketch painting panel.
		cheatSheet = null;
		try { cheatSheet = new CheatSheetGraphics("en_US"); }
		catch(IOException e) { e.printStackTrace(); }
		
		// Insert the welcome template.
		DefaultSketchModel<TrifoldProxyPath> model = 
				new DefaultSketchModel<>(templates[0],
						pathView, pathManager);
		resetSketchModel(templates[0], model);
		sketchPanel.displayUsage = true;
		
		// Create the keyboard capture's listener.
		mainFrame.addKeyListener(new KeyAdapter() {
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
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void resetSketchModel(Template newTemplate,
			DefaultSketchModel<TrifoldProxyPath> model) {
		
		// Remove previous existing sketch and edit panel.
		if(sketchPanel != null) mainFrame.remove(sketchPanel);
		if(editPanel != null) mainFrame.remove(editPanel);
		
		// Destroy N-Dollar recognizer if template mismatches.
		// And initialize the new template's recognizers.
		if(currentTemplate != newTemplate) {
			if(recognizer != null) recognizer.destroyNDollar();
			recognizer = new SketchRecognizer(
					new File(Configuration.getInstance().GESTURE_PATH), 
					newTemplate.entities());
			currentTemplate = newTemplate;
			recognizer.initializeNDollar();
		}
		
		// Create the new sketch panel.
		currentModel = model;
		sketchPanel = new SketchPanel<>(candidatePanel, currentModel, 
				recognizer, pathManager, pathView, cheatSheet);
		mainFrame.add(sketchPanel, BorderLayout.CENTER);
		
		// Create the new edit panel.
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
		mainFrame.add(editPanel, BorderLayout.EAST);
		
		// Update main frame and repaint.
		sketchPanel.repaint();
		sketchPanel.updateUI();
		editPanel.repaint();
		editPanel.updateUI();
		mainFrame.repaint();
	}
}
