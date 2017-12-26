package net.aegistudio.sketchuml.stroke;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

import de.dubs.dollarn.NDollarRecognizer;
import de.dubs.dollarn.PointR;

/**
 * Encapsulate the $N recognizer and make it more expressive in 
 * the context of this application.
 * 
 * @see de.dubs.dollarn.GraphicLauncher
 * 
 * @author Haoran Luo
 */
public class SketchRecognizer {
	private final File contextDir;
	public final SketchMapEntry[] entries;
	
	/**
	 * @param contextDir the context directory to find the stroke
	 * data from hard disk.
	 */
	public SketchRecognizer(File contextDir, SketchMapEntry[] entries) {
		this.contextDir = contextDir;
		this.entries = entries;
	}
	
	private File baseDir(String name) {
		return new File(contextDir, 
				name.replace('/', File.separatorChar));
	}
	
	public File[] listInstanceXmls(SketchMapEntry entry) {
		File baseDir = baseDir(entry.entry);
		if(!baseDir.exists()) return new File[0];
		return baseDir.listFiles((file, name) 
				-> name.endsWith(".xml"));
	}
	
	public SketchInstance[] loadInstance(SketchMapEntry entry) {
		return Arrays.stream(listInstanceXmls(entry))
				.map(SketchInstance::createAndLoad)
				.filter(Objects::isNull)
				.toArray(SketchInstance[]::new);
	}
	
	private NDollarRecognizer recognizer = null;
	public void initializeNDollar() {
		this.recognizer = new NDollarRecognizer();
		Arrays.asList(entries).parallelStream().forEach(entry -> {
			Arrays.asList(listInstanceXmls(entry)).parallelStream()
				.forEach(file -> recognizer.loadGesture(file, entry));
		});
	}
	
	public void destroyNDollar() {
		this.recognizer = null;
		System.gc();
	}
	
	/**
	 * @param points the points in the stroke.
	 * @return candidate entity factories, sorted by the 
	 * possibilities descending.
	 */
	public SketchMapEntry[] recognize(Vector<PointR> points, int numStrokes) {
		return null;
	}
}
