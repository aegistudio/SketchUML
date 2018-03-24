package net.aegistudio.sketchuml;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
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
import net.aegistudio.sketchuml.framework.PathEditor;
import net.aegistudio.sketchuml.framework.SketchLinkComponent;
import net.aegistudio.sketchuml.framework.SketchPanel;
import net.aegistudio.sketchuml.path.TrifoldProxyPath;
import net.aegistudio.sketchuml.path.TrifoldPathManager;
import net.aegistudio.sketchuml.path.BezierPathView;
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
	
	// The history stack and the undo/redo item.
	public static DefaultHistory history;
	public static JMenuItem menuItemUndo, menuItemRedo;
	
	// The export file options.
	public static JMenuItem menuItemExportAstah;
	
	// The sketch render style options.
	public static RadioMenuItemGroup<SketchRenderHint> sketchRenderHints;
	public static SketchRenderHint sketchRenderHint;
	
	// The sketch background options.
	public static RadioMenuItemGroup<Background> backgrounds;
	public static Background background;
	
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
				return previousFile == null? new File(
						".", "Untitled.suml") : previousFile;
			}
			
			public FileFilter getPreviousFormat() {
				return previousFilter;
			}
			
			public void setPreviousStatus(File file, FileFilter format) {
				previousFile = file;
				previousFilter = format;
			}
		};

		// Initialize the history.
		history = new DefaultHistory() {
			@Override
			public void perform(Object id, 
					Command command, boolean perform) {
				super.perform(id, command, perform);
				updateHistoryItems();
			}
			
			@Override
			public void startLocal(Object id) {
				super.startLocal(id);
				updateHistoryItems();
			}
			
			@Override
			public void undo() {
				super.undo();
				updateHistoryItems();
			}
			
			@Override
			public void redo() {
				super.redo();
				updateHistoryItems();
			}
		};
		
		// Add the title bar.
		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);
		
		// Add the file menu.
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic('F');
		menuBar.add(menuFile);
		
		// The [File -> Open] item.
		JMenuItem menuItemOpen = new JMenuItem("Open");
		menuItemOpen.addActionListener(a -> {
			SketchPersistence.SketchFile<TrifoldProxyPath> fileModel = 
					 new SketchPersistence.SketchFile<>();
			if(!persistence.openModelPanel(mainFrame, fileModel)) return;
			
			// Update workspace status.
			updateTitle();
			history.reset();
			updateHistoryItems();
			resetSketchModel(fileModel.template, 
					(DefaultSketchModel<TrifoldProxyPath>)
					fileModel.sketchModel);
		});
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
		menuFile.add(menuItemOpen);
		
		// The [File -> Save] item.
		JMenuItem menuItemSave = new JMenuItem("Save");
		menuItemSave.addActionListener(a -> {
			SketchPersistence.SketchFile<TrifoldProxyPath> fileModel = 
					 new SketchPersistence.SketchFile<>();
			fileModel.formatVersion = formatVersion;
			fileModel.template = currentTemplate;
			fileModel.sketchModel = currentModel;
			
			// Attempt to save previous file, or normal save if there's 
			// no file previously save.
			boolean saveStatus = false;
			if(previousFile != null) saveStatus = 
					persistence.savePreviousFile(mainFrame, fileModel);
			else saveStatus = persistence.saveModelPanel(mainFrame, fileModel);
			if(!saveStatus) return;
			
			// Update the workspace status.
			updateTitle();
		});
		menuItemSave.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		menuFile.add(menuItemSave);
		
		// The [File -> Save As] item.
		JMenuItem menuItemSaveAs = new JMenuItem("Save As");
		menuItemSaveAs.addActionListener(a -> {
			SketchPersistence.SketchFile<TrifoldProxyPath> fileModel = 
					 new SketchPersistence.SketchFile<>();
			fileModel.formatVersion = formatVersion;
			fileModel.template = currentTemplate;
			fileModel.sketchModel = currentModel;
			if(!persistence.saveModelPanel(mainFrame, fileModel)) return;
			
			// Update workspace status.
			updateTitle();
		});
		menuItemSaveAs.setAccelerator(KeyStroke.getKeyStroke("ctrl alt S"));
		menuFile.add(menuItemSaveAs);
		
		// An separator before export and import options.
		menuFile.addSeparator();
		
		// The export to astah project persistence.
		AstahPersistence<TrifoldProxyPath> astahPersistence = 
				new AstahPersistence<TrifoldProxyPath>() {
			
			@Override
			public File getPreviousFile() {
				File projectFile = persistence.getPreviousFile();
				File projectDir = projectFile.getParentFile();
				return new File(projectDir, getProjectFileName() + ".asta");
			}
		};
		
		// The [File -> Export as Astah Project] item.
		menuItemExportAstah = new JMenuItem("Export as Astah Project");
		menuItemExportAstah.addActionListener(a -> {
			AstahPersistence.AstahTransaction<TrifoldProxyPath> astahTransaction =
					new AstahPersistence.AstahTransaction<>();
			astahTransaction.projectName = getProjectFileName();
			astahTransaction.sketchModel = currentModel;
			astahTransaction.pathManager = pathManager;
			if(!astahPersistence.saveModelPanel(mainFrame, astahTransaction)) return;
		});
		menuFile.add(menuItemExportAstah);
		
		// Add the edit menu.
		JMenu menuEdit = new JMenu("Edit");
		menuEdit.setMnemonic('E');
		menuBar.add(menuEdit);
		
		// Add the [Edit -> Undo] item.
		menuItemUndo = new JMenuItem();
		menuItemUndo.addActionListener(a -> history.undo());
		menuEdit.add(menuItemUndo);
		menuItemUndo.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
		menuItemUndo.setEnabled(false);
		
		// Add the [Edit -> Redo] item.
		menuItemRedo = new JMenuItem();
		menuItemRedo.addActionListener(a -> history.redo());
		menuEdit.add(menuItemRedo);
		menuItemRedo.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
		
		// Initialize history items.
		updateHistoryItems();
		
		// Initialize the view menu.
		JMenu menuView = new JMenu("View");
		menuView.setMnemonic('V');
		menuBar.add(menuView);
		
		// Initialize the sketch render hints.
		sketchRenderHints = new RadioMenuItemGroup<SketchRenderHint>() {
			@Override
			public void selectItem(SketchRenderHint t) {
				if(t != null && t != Main.sketchRenderHint) {
					Main.sketchRenderHint = t;
					sketchPanel.updateUI();
					sketchPanel.repaint();
				}
			}
		};
		{
			// The default SketchUML style render hint.
			SketchRenderHint defaultRenderHint = new SketchRenderHint();
			defaultRenderHint.labelFont = Configuration
					.getInstance().HANDWRITING_FONT;
			defaultRenderHint.awtRenderingHints = new RenderingHints(null);
			defaultRenderHint.lineColorNormal = Color.BLACK;
			defaultRenderHint.fillColorNormal = Color.WHITE;
			defaultRenderHint.lineColorSelected = Color.GRAY;
			defaultRenderHint.fillColorSelected = Color.WHITE;
			defaultRenderHint.userColor = Color.BLACK;
			defaultRenderHint.outlineWidth = 2.0f;
			defaultRenderHint.inlineWidth = 2.0f;
			defaultRenderHint.lineWidthSelected = 3.0f;
			defaultRenderHint.userWidth = 2.0f;
			sketchRenderHint = defaultRenderHint;
			sketchRenderHints.addItem(defaultRenderHint, "Default", true);
			
			// For those requires anti-aliasing, initilize this hint.
			RenderingHints awtAntiAliasedHint = new RenderingHints(
					RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			// Attempt to configure the color just as the Visio.
			Font visioMimicFont = Main.fonts.get("Calibri");
			if(visioMimicFont != null) {
				visioMimicFont = visioMimicFont.deriveFont(
					Configuration.getInstance().HANDWRITING_FONTSTYLE, 
					Configuration.getInstance().HANDWRITING_FONTSIZE);
				SketchRenderHint visioMimicHint = new SketchRenderHint();
				visioMimicHint.awtRenderingHints = awtAntiAliasedHint;
				visioMimicHint.labelFont = visioMimicFont;
				visioMimicHint.lineColorNormal = new Color(47, 82, 143);
				visioMimicHint.fillColorNormal = new Color(68, 114, 196);
				visioMimicHint.lineColorSelected = new Color(129, 151, 187);
				visioMimicHint.fillColorSelected = new Color(142, 170, 219);
				visioMimicHint.userColor = Color.GRAY;
				visioMimicHint.outlineWidth = 2.0f;
				visioMimicHint.inlineWidth = 1.0f;
				visioMimicHint.lineWidthSelected = 3.0f;
				visioMimicHint.userWidth = 2f;
				visioMimicHint.colorMap.put(SketchRenderHint.innerLabelColor, Color.WHITE);
				sketchRenderHints.addItem(visioMimicHint, "Visio Styled", false);
			}
		}
		sketchRenderHints.addMenu(menuView);
		
		// Initialize the backgrounds.
		menuView.addSeparator();
		backgrounds = new RadioMenuItemGroup<Background>() {
			@Override
			public void selectItem(Background t) {
				if(background != t) {
					background = t;
					sketchPanel.updateUI();
					sketchPanel.repaint();
				}
			}
		};
		{
			// These style settings will be used later.
			BasicStroke slimDenseStroke = new BasicStroke(1.f);
			BasicStroke slimDotStroke = new BasicStroke(2.f, 
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f,
					new float[] {2.0f, 3.0f}, 5.f);
			BasicStroke thickStroke = new BasicStroke(2.f);
			Color gridColor = new Color(0.95f, 0.95f, 0.95f, 1.0f);
			
			// The background with nothing on it.
			Background backgroundEmpty = new BackgroundNull();
			background = backgroundEmpty;
			backgrounds.addItem(backgroundEmpty, "Empty", true);
			
			// The background with 2-period repetitive grid (dot or not) on it.
			Background background2Grid = new BackgroundGrid(
					2, 20., slimDenseStroke, thickStroke, gridColor);
			backgrounds.addItem(background2Grid, "2-Grid", false);
			Background background2DotGrid = new BackgroundGrid(
					2, 20., slimDotStroke, thickStroke, gridColor);
			backgrounds.addItem(background2DotGrid, "2-Grid (Dot)", false);
			
			// The background with 2-period repetitive grid on it.
			Background background5Grid = new BackgroundGrid(
					5, 10., slimDenseStroke, thickStroke, gridColor);
			backgrounds.addItem(background5Grid, "5-Grid", false);
			Background background5DotGrid = new BackgroundGrid(
					5, 10., slimDotStroke, thickStroke, gridColor);
			backgrounds.addItem(background5DotGrid, "5-Grid (Dot)", false);
		}
		backgrounds.addMenu(menuView);
		
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
			for(int j = 0; j < menu.getItemCount(); ++ j) {
				JMenuItem menuItem = menu.getItem(j);
				if(menuItem != null) menuItem.setFont(Configuration
						.getInstance().PROPERTY_FONT);
			}
		}
		
		// Add the result selection panel.
		candidatePanel = new CandidatePanel();
		mainFrame.add(candidatePanel, BorderLayout.SOUTH);
		
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
		
		// Show the main user interface.
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		updateTitle();
	}
	
	public static void resetSketchModel(Template newTemplate,
			DefaultSketchModel<TrifoldProxyPath> model) {
		
		// Remove previous existing sketch and edit panel.
		if(sketchPanel != null) mainFrame.remove(sketchPanel);
		if(editPanel != null) mainFrame.remove(editPanel);
		
		// Update when a new template is selected.
		if(currentTemplate != newTemplate) {
			// Destroy N-Dollar recognizer if template mismatches.
			// And initialize the new template's recognizers.
			if(recognizer != null) recognizer.destroyNDollar();
			recognizer = new SketchRecognizer(
					new File(Configuration.getInstance().GESTURE_PATH), 
					newTemplate.entities());
			currentTemplate = newTemplate;
			recognizer.initializeNDollar();
			
			// Update the exporting and importing stack.
			menuItemExportAstah.setEnabled(newTemplate.canExportAstah());
		}
		
		// Create the new sketch panel.
		currentModel = model;
		sketchPanel = new SketchPanel<>(candidatePanel, 
				history, currentModel, currentModel, recognizer, 
				pathManager, pathView, cheatSheet, 
				() -> sketchRenderHint, () -> background);
		mainFrame.add(sketchPanel, BorderLayout.CENTER);
		
		// Create the new edit panel.
		editPanel = new ComponentEditPanel<>(model, model,
			history, new TrifoldPathEditor(), 
			new PathEditor.PathChangeListener<TrifoldProxyPath>() {

				@Override
				public Object beforeChange(
						SketchLinkComponent<TrifoldProxyPath> previousPath) {
					return null;
				}

				@Override
				public void receiveChange(Object beforeChange, 
						SketchLinkComponent<TrifoldProxyPath> newPath) {
					if(editPanel == null) return;
					model.notifyLinkStyleChanged(newPath);
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
	
	public static void updateTitle() {
		mainFrame.setTitle("SketchUML - " + getProjectFileName());
	}
	
	public static String getProjectFileName() {
		String fileName = previousFile == null? 
				"Untitled" : previousFile.getName();
		if(fileName.toLowerCase().endsWith(".suml"))
			fileName = fileName.substring(0, 
					fileName.length() - ".suml".length());
		return fileName;
	}
	
	public static void updateHistoryItems() {
		// Get the undo description.
		Command undo = history.lastUndo();
		String undoName = undo == null? 
				"..." : undo.name();
		menuItemUndo.setEnabled(undo != null);
		menuItemUndo.setText("Undo " + undoName);
		
		// Get the redo description.
		Command redo = history.lastRedo();
		String redoName = redo == null?
				"..." : redo.name();
		menuItemRedo.setEnabled(redo != null);
		menuItemRedo.setText("Redo " + redoName);
	}
}
