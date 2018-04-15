package net.aegistudio.sketchuml.abstraction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface represents UML entities. This object serves as the model
 * object, and should be cooperated with the view objects to show
 * properties and render in the canvas.
 * 
 * @author Haoran Luo
 */
public interface Entity {
	/**
	 * Deserialize entity from the saved file format.
	 *  
	 * @param inputStream the data source's input stream.
	 * @throws IOException when there's failure related to I/O.
	 */
	public void load(DataInputStream inputStream) throws IOException;
	
	/**
	 * Sserialize entity to the saved file format.
	 *  
	 * @param outputStream the data source's output stream.
	 * @throws IOException when there's failure related to I/O.
	 */
	public void save(DataOutputStream outputStream) throws IOException;
}
