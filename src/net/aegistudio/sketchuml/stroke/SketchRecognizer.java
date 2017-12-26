package net.aegistudio.sketchuml.stroke;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import de.dubs.dollarn.NBestList;
import de.dubs.dollarn.NDollarRecognizer;
import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.EntityEntry;

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
	public final EntityEntry[] entries;
	
	/**
	 * @param contextDir the context directory to find the stroke
	 * data from hard disk.
	 */
	public SketchRecognizer(File contextDir, EntityEntry[] entries) {
		this.contextDir = contextDir;
		this.entries = entries;
	}
	
	private File baseDir(String name) {
		return new File(contextDir, 
				name.replace('/', File.separatorChar));
	}
	
	public File[] listInstanceXmls(EntityEntry entry) {
		File baseDir = baseDir(entry.entry);
		if(!baseDir.exists()) return new File[0];
		return baseDir.listFiles((file, name) 
				-> name.endsWith(".xml"));
	}
	
	public SketchInstance[] loadInstance(EntityEntry entry) {
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
	public EntityEntry[] recognize(Vector<PointR> points, int numStrokes) {
		if(this.recognizer == null) return new EntityEntry[0];
		NBestList bestList = this.recognizer.Recognize(points, numStrokes);
		NBestList.NBestResult result;
		List<EntityEntry> candidates = new ArrayList<>();
		Set<EntityEntry> appeared = new HashSet<>();
		for(int i = 0; (result = bestList.get(i)) != null; ++ i) {
			EntityEntry entry = (EntityEntry) result.getUserdata();
			if(entry == null) continue;
			if(appeared.add(entry))
				candidates.add(entry);
		}
		return candidates.toArray(new EntityEntry[0]);
	}
}
