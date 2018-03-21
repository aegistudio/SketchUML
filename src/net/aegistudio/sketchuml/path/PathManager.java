package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import JP.co.esm.caddies.golf.geom2D.Pnt2d;
import de.dubs.dollarn.PointR;

public interface PathManager<Path> {
	/**
	 * Convert the points indicated by the stroke to the 
	 * quantized path, so that could be rendered later. 
	 * 
	 * @param stroke the points in the stroke.
	 * @param boundBegin the starting object's bound.
	 * @param boundEnd the ending object's bound.
	 * @return the generated path by this object.
	 */
	public Path quantize(Vector<PointR> stroke, 
			Rectangle2D boundBegin, Rectangle2D boundEnd);
	
	/**
	 * Save the path instance to the disk file.
	 * @param output the output stream.
	 * @param pathObject the path to save
	 * @throws IOException when the path fails to serialized.
	 */
	public void save(DataOutputStream output, 
			Path pathObject) throws IOException;
	
	/**
	 * Read the path instance from the disk file.
	 * @param input the input stream
	 * @return the path that was read
	 * @throws IOException when the path fails to deserialize.
	 */
	public Path read(DataInputStream input) throws IOException;
	
	public static class AstahPathHint {
		public Pnt2d[] innerPoints;
		
		public Pnt2d[] outerPoints;
		
		public Pnt2d[] controlPoints;
		
		public double sourceX, sourceY;
		
		public double targetX, targetY;
		
		public String lineStyle;
		
		public Pnt2d pathCenter;
	}
	
	public AstahPathHint getAstahPathHint(Path path, 
			Rectangle2D pathBegin, Rectangle2D pathEnd);
}
