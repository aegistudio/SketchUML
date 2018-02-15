package JP.co.esm.caddies.uml.BehavioralElements.StateMachines;

/**
 * This file is auto-generated from java packages through 
 * reflection and will never contain piece of original    
 * source of astah-community. These code should never be  
 * utilized for commercial purpose.                       
 * 
 * @author Haoran Luo
 */

public class UTransitionImp extends JP.co.esm.caddies.uml.Foundation.Core.UModelElementImp
    implements java.io.Serializable, JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UTransition {
    public static final long serialVersionUID = 6314222865271285450L;

    public JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateVertex source;

    public JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateVertex target;

    public JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UGuard guard;

    public JP.co.esm.caddies.uml.BehavioralElements.CommonBehavior.UAction effect;

    public JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UEvent trigger;

    public JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateMachine transitionsInv;

    public JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UState internalTransitionInv;
}
