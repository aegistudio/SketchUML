package JP.co.esm.caddies.er;

/**
 * This file is auto-generated from java packages through 
 * reflection and will never contain piece of original    
 * source of astah-community. These code should never be  
 * utilized for commercial purpose.                       
 * 
 * @author Haoran Luo
 */

@SuppressWarnings("rawtypes")
public class ERAttributeImp extends JP.co.esm.caddies.uml.Foundation.Core.UAttributeImp
    implements java.io.Serializable, JP.co.esm.caddies.er.ERAttribute {
    public static final long serialVersionUID = 3561232075199484970L;

    public JP.co.esm.caddies.er.ERRelationship relationship;

    public JP.co.esm.caddies.er.ERAttribute referencedPrimaryKey;

    public java.util.List subtypeRelationships;

    public JP.co.esm.caddies.er.ERSubtypeRelationship subTypeForeignKeyInv;

    public java.util.List referencedForeignKeys;

    public JP.co.esm.caddies.er.ERAttribute identifiedAttribute;

    public java.util.List erIndexes;
}
