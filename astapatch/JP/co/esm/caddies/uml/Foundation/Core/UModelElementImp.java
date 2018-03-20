package JP.co.esm.caddies.uml.Foundation.Core;

/**
 * This code is modified by analyzing command line program.
 * You should use this hand written version instead of the 
 * auto-generated one.
 * 
 * @author Haoran Luo
 */

@SuppressWarnings("rawtypes")
public class UModelElementImp extends JP.co.esm.caddies.uml.Foundation.Core.UElementImp
    implements java.io.Serializable, JP.co.esm.caddies.uml.Foundation.Core.UModelElement {
    public static final long serialVersionUID = 6503275615930165386L;

    public JP.co.esm.caddies.uml.Foundation.DataTypes.UName name
    		= new JP.co.esm.caddies.uml.Foundation.DataTypes.UName();

    public JP.co.esm.caddies.uml.Foundation.DataTypes.UUninterpreted definition
    		= new JP.co.esm.caddies.uml.Foundation.DataTypes.UUninterpreted();
    /** Initialize the string inside */ { name.body = ""; definition.body = ""; }
    
    public java.util.List constraint = new java.util.ArrayList();

    public java.util.List elementResidence = new java.util.ArrayList();

    public java.util.List supplierDependency = new java.util.ArrayList();

    public java.util.List clientDependency = new java.util.ArrayList();

    public java.util.List taggedValue = new java.util.ArrayList();

    public java.util.List stereotype = new java.util.ArrayList();

    public JP.co.esm.caddies.uml.Foundation.Core.UElementOwnership namespaceOwnership;

    public java.util.List uPackage = null;

    public java.util.List elemRefer = null;

    public java.util.List presentation = new java.util.ArrayList();

    public java.util.List behavior = new java.util.ArrayList();

    public java.util.List annotatedElementInv = new java.util.ArrayList();
}
