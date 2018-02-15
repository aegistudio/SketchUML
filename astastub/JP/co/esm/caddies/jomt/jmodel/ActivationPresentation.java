package JP.co.esm.caddies.jomt.jmodel;

/**
 * This file is auto-generated from java packages through 
 * reflection and will never contain piece of original    
 * source of astah-community. These code should never be  
 * utilized for commercial purpose.                       
 * 
 * @author Haoran Luo
 */

@SuppressWarnings("rawtypes")
public class ActivationPresentation extends JP.co.esm.caddies.jomt.jmodel.RectPresentation
    implements java.io.Serializable, JP.co.esm.caddies.jomt.jmodel.IActivationPresentation {
    public static final long serialVersionUID = -4110403242710613488L;

    public boolean visibility;

    public boolean doAutoResize;

    public JP.co.esm.caddies.uml.BehavioralElements.Collaborations.UMessage activator;

    public JP.co.esm.caddies.jomt.jmodel.ITerminationPresentation relatedTp;

    public JP.co.esm.caddies.jomt.jmodel.ITerminationPresentation termPs;

    public java.util.List inMessages;

    public java.util.List outMessages;

    public java.util.List leftActivationPs;

    public java.util.List rightActivationPs;
}
