package JP.co.esm.caddies.uml.BehavioralElements.StateMachines;

/**
 * This file is auto-generated from java packages through 
 * reflection and will never contain piece of original    
 * source of astah-community. These code should never be  
 * utilized for commercial purpose.                       
 * 
 * @author Haoran Luo
 */

@SuppressWarnings("rawtypes")
public class UStateImp extends JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateVertexImp
    implements java.io.Serializable, JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UState {
    public static final long serialVersionUID = -7194961838608411907L;

    public JP.co.esm.caddies.uml.BehavioralElements.CommonBehavior.UAction entry;

    public JP.co.esm.caddies.uml.BehavioralElements.CommonBehavior.UAction exit;

    public JP.co.esm.caddies.uml.BehavioralElements.CommonBehavior.UAction doActivity;

    public java.util.List deferrableEvent = new java.util.ArrayList();

    public java.util.List internalTransitions = new java.util.ArrayList();

    public java.util.List inStateInv = new java.util.ArrayList();
}
