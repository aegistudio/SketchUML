package net.aegistudio.sketchuml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.aegistudio.sketchuml.framework.DefaultSketchModel;
import net.aegistudio.sketchuml.framework.SketchPanel;
import net.aegistudio.sketchuml.statechart.TemplateStateChart;
import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public class Main {
	public static Template[] templates = { new TemplateStateChart() };
	public static Map<String, Font> fonts = new HashMap<>();
	
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
		
		// Initialize the recognizers.
		SketchRecognizer recognizer = new SketchRecognizer(
				new File("data"), templates[0].entities());
		DefaultSketchModel model = new DefaultSketchModel(recognizer);
		recognizer.initializeNDollar();
		
		// Create the main frame.
		JFrame frame = new JFrame();
		frame.setTitle("SketchUML");
		frame.setLocationRelativeTo(null);
		frame.setLocation(0, 0);
		frame.setSize(1400, 768);
		
		// Create the sketch painting panel.
		SketchPanel sketchPanel = new SketchPanel(model);
		JPanel selectionPanel = new JPanel();
		JLabel[] selectionLabels = new JLabel[
			Configuration.getInstance().MAX_CANDIDATE]; 
		for(int i = 0; i < selectionLabels.length; ++ i) {
			final int current = i;
			selectionLabels[i] = new JLabel();
			selectionLabels[i].setPreferredSize(new Dimension(180, 30));
			selectionLabels[i].setHorizontalAlignment(JLabel.CENTER);
			selectionPanel.add(selectionLabels[i]);
			selectionLabels[i].setFont(Configuration
					.getInstance().HANDWRITING_FONT);
			selectionLabels[i].addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					sketchPanel.selectCandidate(current);
				}
			});
		}
		frame.add(sketchPanel, BorderLayout.CENTER);
		frame.add(selectionPanel, BorderLayout.SOUTH);
		
		// Update selection panel while sketch scrolling.
		sketchPanel.candidateNotifier = () -> {
			for(JLabel label : selectionLabels)
				label.setText("");
			if(sketchPanel.candidates != null) {
				for(int i = 0; i < sketchPanel.candidates.length && 
						i < Configuration.getInstance().MAX_CANDIDATE; ++ i) {
					String nameText = "" + (i + 1) + ": " 
						+ sketchPanel.candidates[i].name;
					selectionLabels[i].setText(
						(sketchPanel.candidateIndex != i)? nameText :
							"<html><b style=\"background:yellow\">" 
							+ nameText + "</b></html>");
				}
			}
			selectionPanel.repaint();
		};
		
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
