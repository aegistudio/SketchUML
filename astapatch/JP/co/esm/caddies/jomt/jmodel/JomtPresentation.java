package JP.co.esm.caddies.jomt.jmodel;

/**
 * This code is modified by analyzing command line program.
 * You should use this hand written version instead of the 
 * auto-generated one.
 * 
 * @author Haoran Luo
 */

public class JomtPresentation extends JP.co.esm.caddies.uml.Foundation.Core.UPresentation
    implements java.io.Serializable {
    public static final long serialVersionUID = -7508818022374904276L;

    public JP.co.esm.caddies.golf.geom2D.Pnt2d location
    	= new JP.co.esm.caddies.golf.geom2D.Pnt2d();

    public java.lang.Long changeId = -1l;

    public int depth = Integer.MAX_VALUE;

    public JP.co.esm.caddies.golf.geom2D.Vec2d localMovement
    	= new JP.co.esm.caddies.golf.geom2D.Vec2d();

    public JP.co.esm.caddies.uml.Foundation.Core.IUPresentation compositeParent;

    public JP.co.esm.caddies.jomt.jmodel.IRectPresentation container;

    public int notationType;

    public java.lang.String iconID;

    public boolean constraintVisibility;
}
