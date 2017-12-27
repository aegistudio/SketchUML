package net.aegistudio.sketchuml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
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
	public static final int MAX_CANDIDATE = 5;
	
	public static void main(String[] arguments) {
		try { UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); } 
		catch (Exception e) {	}
		
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for(Font font : fonts) Main.fonts.put(font.getName(), font);
		
		SketchRecognizer recognizer = new SketchRecognizer(
				new File("data"), templates[0].entities());
		DefaultSketchModel model = new DefaultSketchModel(recognizer);
		recognizer.initializeNDollar();
		
		JFrame frame = new JFrame();
		frame.setTitle("SketchUML");
		frame.setLocationRelativeTo(null);
		frame.setLocation(0, 0);
		frame.setSize(1024, 768);
		
		SketchPanel sketchPanel = new SketchPanel(model);
		JPanel selectionPanel = new JPanel();
		JLabel[] selectionLabels = new JLabel[MAX_CANDIDATE]; 
		for(int i = 0; i < selectionLabels.length; ++ i) {
			final int current = i;
			selectionLabels[i] = new JLabel();
			selectionLabels[i].setPreferredSize(new Dimension(180, 30));
			selectionLabels[i].setHorizontalAlignment(JLabel.CENTER);
			selectionPanel.add(selectionLabels[i]);
			selectionLabels[i].setFont(Main.fonts
					.get("Courier New").deriveFont(16.0f));
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
			if(sketchPanel.candidates == null) 
				for(JLabel label : selectionLabels)
					label.setText("");
			else {
				for(int i = 0; i < selectionLabels.length; ++ i) {
					String nameText = "" + (i + 1) + ": " + sketchPanel.candidates[i].name;
					selectionLabels[i].setText(
						(sketchPanel.candidateIndex != i)? nameText :
							"<html><b style=\"background:yellow\">" 
							+ nameText + "</b></html>");
				}
			}
			selectionPanel.repaint();
		};
		
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
