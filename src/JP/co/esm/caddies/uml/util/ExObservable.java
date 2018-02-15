package JP.co.esm.caddies.uml.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This code is modified by analyzing command line program.
 * You should use this hand written version instead of the 
 * auto-generated one.
 * 
 * @author Haoran Luo
 */

public class ExObservable extends java.util.Observable
    implements java.io.Serializable, JP.co.esm.caddies.uml.util.IExObservable {
    public static final long serialVersionUID = -4030011647484958863L;

    public boolean ex_changed;
    
    public transient List<Serializable> ex_observers;

    private void readObject(ObjectInputStream ois)
    	    throws ClassNotFoundException, IOException {
    	
    	ois.defaultReadObject();
    	Object localObject;
    	while (null != (localObject = ois.readObject())) {
    		if(!(localObject instanceof Serializable)) continue;
    		
    		if(ex_observers == null) 
        		ex_observers = new ArrayList<>();
        	
        	if(!ex_observers.contains((Serializable)localObject)) 
        		ex_observers.add((Serializable)localObject);
    	}
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
    	oos.defaultWriteObject();
    	if(ex_observers != null) 
    		for(Serializable s : ex_observers)
    			oos.writeObject(s);
    	oos.writeObject(null);
    }
}
