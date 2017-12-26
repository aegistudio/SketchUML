package net.aegistudio.sketchuml;

import java.io.File;

import javax.swing.JFrame;

import net.aegistudio.sketchuml.framework.DefaultSketchModel;
import net.aegistudio.sketchuml.framework.SketchPanel;
import net.aegistudio.sketchuml.statechart.TemplateStateChart;
import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public class Main {
	static Template[] templates = { new TemplateStateChart() };
	
	public static void main(String[] arguments) {
		SketchRecognizer recognizer = new SketchRecognizer(
				new File("data"), templates[0].entities());
		DefaultSketchModel model = new DefaultSketchModel(recognizer);
		recognizer.initializeNDollar();
		
		JFrame frame = new JFrame();
		frame.setTitle("SketchUML");
		frame.setLocationRelativeTo(null);
		frame.setLocation(0, 0);
		frame.setSize(1024, 768);
		
		frame.add(new SketchPanel(model));
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
