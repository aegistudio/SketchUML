package JP.co.esm.caddies.uml.Foundation.Core;

/**
 * This file is auto-generated from java packages through 
 * reflection and will never contain piece of original    
 * source of astah-community. These code should never be  
 * utilized for commercial purpose.                       
 * 
 * @author Haoran Luo
 */

@SuppressWarnings("rawtypes")
public class UOperationImp extends JP.co.esm.caddies.uml.Foundation.Core.UBehavioralFeatureImp
    implements java.io.Serializable, JP.co.esm.caddies.uml.Foundation.Core.UOperation {
    public static final long serialVersionUID = -2150554416508649235L;

    public JP.co.esm.caddies.uml.Foundation.DataTypes.UCallConcurrencyKind concurrency;

    public boolean root;

    public boolean leaf;

    public boolean isAbst;

    public java.util.List method = new java.util.ArrayList();

    public java.util.List operationInv = new java.util.ArrayList();

    public java.util.List representedOperationInv = new java.util.ArrayList();

    public JP.co.esm.caddies.uml.Foundation.Core.UConstraint bodyCondition;

    public java.util.List preConditions = new java.util.ArrayList();

    public java.util.List postConditions = new java.util.ArrayList();
}
