package JP.co.esm.caddies.jomt.jmodel;

/**
 * This code is modified by analyzing command line program.
 * You should use this hand written version instead of the 
 * auto-generated one.                       
 * 
 * @author Haoran Luo
 */

@SuppressWarnings("rawtypes")
public class ModelManageInfo extends java.lang.Object
    implements java.io.Serializable {
    public static final long serialVersionUID = -4849569514030136901L;

    public int currentModelVersion;

    public int maxModelVersion;

    public java.lang.String currentModelProducer;

    public java.util.HashMap judeVersionHistory = new java.util.HashMap();

    public java.util.List sortedVersionHistory = new java.util.ArrayList();

    public long lastModifiedTime;
}
