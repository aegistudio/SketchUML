package JP.co.esm.caddies.golf.geom2D;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This code is modified by analyzing command line program.
 * You should use this hand written version instead of the 
 * auto-generated one.                       
 * 
 * @author Haoran Luo
 */

public class Tpl2d extends java.awt.geom.Point2D.Double
    implements java.io.Serializable {
    public static final long serialVersionUID = -9113352173891309436L;
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeDouble(super.x); oos.writeDouble(super.y);
    }
	  
    private void readObject(ObjectInputStream ois) throws IOException {
		super.x = ois.readDouble(); super.y = ois.readDouble();
    }
}
