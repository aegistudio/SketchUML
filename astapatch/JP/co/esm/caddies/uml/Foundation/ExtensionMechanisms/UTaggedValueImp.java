package JP.co.esm.caddies.uml.Foundation.ExtensionMechanisms;

/**
 * This code is modified by analyzing command line program.
 * You should use this hand written version instead of the 
 * auto-generated one.                       
 * 
 * @author Haoran Luo
 */

public class UTaggedValueImp extends JP.co.esm.caddies.uml.Foundation.Core.UElementImp
    implements java.io.Serializable {
    public static final long serialVersionUID = -2616926138076397907L;

    public JP.co.esm.caddies.uml.Foundation.DataTypes.UName tag 
    	= new JP.co.esm.caddies.uml.Foundation.DataTypes.UName();

    public JP.co.esm.caddies.uml.Foundation.DataTypes.UUninterpreted value
    	= new JP.co.esm.caddies.uml.Foundation.DataTypes.UUninterpreted();

    public JP.co.esm.caddies.uml.Foundation.ExtensionMechanisms.UStereotype invRequiredTag;

    public JP.co.esm.caddies.uml.Foundation.Core.UModelElement invTaggedValue;
}
