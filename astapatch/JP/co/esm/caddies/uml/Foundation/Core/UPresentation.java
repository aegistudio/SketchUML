package JP.co.esm.caddies.uml.Foundation.Core;

/**
 * This code is modified by analyzing command line program.
 * You should use this hand written version instead of the 
 * auto-generated one.
 * 
 * @author Haoran Luo
 */

@SuppressWarnings("rawtypes")
public class UPresentation extends JP.co.esm.caddies.uml.util.ExObservable
    implements java.io.Serializable, JP.co.esm.caddies.uml.Foundation.Core.IUPresentation {
    public static final long serialVersionUID = 4069247657887637795L;

    public JP.co.esm.caddies.uml.Foundation.Core.UModelElement model;

    public JP.co.esm.caddies.uml.Foundation.Core.UDiagram diagram;

    public java.util.List clients = new java.util.ArrayList();

    public int version;

    public java.util.List servers = new java.util.ArrayList();

    public java.lang.String uid;

    public java.lang.String id;

    public boolean unsolvedFlag;

    public boolean stereotypeVisibility = true;

    public java.util.Map styleMap = new java.util.HashMap();

    public java.util.Map extensionProperties = null;

    public java.util.List hyperlinks = new java.util.ArrayList();

    public java.util.List miniIcons = null;
}
