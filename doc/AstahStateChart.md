Astah State Chart
=================

## Overview

This file is a report of reverse engineering result of an astah project file contains a single
state chart, which is one of the export result of our program. I construct a simple analytic
program to analyze the relation between all entities in the entity root.

We should present the result first, the result is in the format of:

      ObjectIndex: ClassName:     
    - FieldName[Type]: ReferenceIndex(ReferenceType)

Then the presented result will be analyzed and an induction of the result will be formed.

## Reference Analysis

### UModelImp

    14: UModelImp
    - taggedValue[C]: [18(UTaggedValueImp), 17(UTaggedValueImp)]
    - behavior[C]: [16(UStateMachineImp)]

The *UModelImp* is a model wrapper that references the state machine's model, along with some
model level configurations.
- taggedValue: references some property of the model.

The *UModelImp* has two tagged values (see *UTaggedValueImp*), and all other entities seem to 
have greater ID than the *UModelImp* and its two tagged values.

### UTaggedValueImp

    17: UTaggedValueImp
    - invTaggedValue[R]: 14(UModelImp)
    18: UTaggedValueImp
    - invTaggedValue[R]: 14(UModelImp)

The *UTaggedValueImp* stores the property inside a model object, it seems to store
some value that need not to be changed.
- invTaggedValue: the inverse look up of the model that references it.

The tagged value doesn't seem to be used outside the *UModelImp*, and the two tagged
value in *UModelImp* are always *jude.profiles* and *jude.usericons*. The ID of tagged 
values are always *uModelImp.id + 1* and *uModelImp.id + 2*.

### CompositeStatePresentation

    0: CompositeStatePresentation
    - model[R]: 8(UCompositeStateImp)
    - diagram[R]: 15(UStateChartDiagramImp)
    - clients[C]: [4(TransitionPresentation), 3(TransitionPresentation)]
    - ex_observers[C]: [2(FramePresentation), 4(TransitionPresentation), 3(TransitionPresentation)]
    1: CompositeStatePresentation
    - model[R]: 9(UCompositeStateImp)
    - diagram[R]: 15(UStateChartDiagramImp)
    - clients[C]: [3(TransitionPresentation)]
    - ex_observers[C]: [2(FramePresentation), 3(TransitionPresentation)]
	
The *CompositeStatePresentation* stores the view of a UML state.
- model: obviously references the model of this state.
- diagram: reference the state chart diagram (model) which contains this state.
- clients: stores the transitions (view) of the state.
- ex_observer: stores the frame (view) and the transitions (view). 

### FramePresentation

    2: FramePresentation
    - diagram[R]: 15(UStateChartDiagramImp)

The *FramePresentation* stores the frame view of a state chart.
- diagram: references the state chart diagram (model).

### UStateMachineImp

    16: UStateMachineImp
    - context[R]: 14(UModelImp)
    - top[R]: 7(UCompositeStateImp)
    - transition[C]: [20(UTransitionImp), 19(UTransitionImp)]
    - diagram[R]: 15(UStateChartDiagramImp)

The *UStateMachineImp* stores the model data and hierarchy of the state machine.
- context: references the model object (which stores it as behavior).
- top: references the dummy node (model) of the state machine.
- transition: references the transitions (model) of the state machine.
- diagram: references the diagram objects.

### UStateChartDiagramImp

    15: UStateChartDiagramImp
    - stateMachine[R]: 16(UStateMachineImp)
    - presentation[C]: [2(FramePresentation), 0(CompositeStatePresentation), 4(TransitionPresentation), 1(CompositeStatePresentation), 3(TransitionPresentation)]
    - ex_observers[C]: [2(FramePresentation)]
	
The *UStateChartDiagramImp* seems to store view and model of a state diagram.
- stateMachine: references the state machine's model.
- presentation: references the view of frame, states and transitions in the diagram.
- ex_observers: it seems only needs to reference the frame's view.

### UCompositeStateImp

    7: UCompositeStateImp
    - subvertex[C]: [8(UCompositeStateImp), 9(UCompositeStateImp)]
    8: UCompositeStateImp
    - outgoing[C]: [20(UTransitionImp), 19(UTransitionImp)]
    - incoming[C]: [20(UTransitionImp)]
    - container[R]: 7(UCompositeStateImp)
    - presentation[C]: [0(CompositeStatePresentation)]
    - ex_observers[C]: [0(CompositeStatePresentation)]
    9: UCompositeStateImp
    - incoming[C]: [19(UTransitionImp)]
    - container[R]: 7(UCompositeStateImp)
    - presentation[C]: [1(CompositeStatePresentation)]
    - ex_observers[C]: [1(CompositeStatePresentation)]
    
The *UCompositeStateImp* is the model object which stores the data of a composite state.
Every state chart diagram has a dummy super state (7 in the result), which is referenced 
by the *UStateMachineImp*. Every state could comprise sub-state when they are composite.
- subvertex: references the states inside the composite state.
- incoming/outgoing: the transitions (model) that comes into or from the state.
- container: if it is inside some composite state, reference the super state node. Or 
references null if it is the dummy super state.
- presentation: the array of state views that references the model.
- ex_observers: the array of state views should listen to the changes of the state.

### TransitionPresentation

    3: TransitionPresentation
    - model[R]: 19(UTransitionImp)
    - diagram[R]: 15(UStateChartDiagramImp)
    - servers[C]: [0(CompositeStatePresentation), 1(CompositeStatePresentation)]
    - ex_observers[C]: [2(FramePresentation)]
    4: TransitionPresentation
    - model[R]: 20(UTransitionImp)
    - diagram[R]: 15(UStateChartDiagramImp)
    - servers[C]: [0(CompositeStatePresentation), 0(CompositeStatePresentation)]
    - ex_observers[C]: [2(FramePresentation)]

The *TransitionPresentation* stores the transition view of one state or between two states.
- model: references the model of the transition.
- diagram: references the state chart diagram (model) which contains this transition.
- servers: references the two ends of the transition, so they could be the same if it is
a self transform.
- ex_observers: references the frame (view).

To get the transition work correctly, there's integrity requirement between the *name* field of 
*UTransitionImp* and the *namePresentation* field of *TransitionPresentation*.

### UEventImp/UGuardImp/UActionImp

    5: UActionImp
    - effectInv[R]: 19(UTransitionImp)
    6: UActionImp
    - effectInv[R]: 20(UTransitionImp)
	...
    10: UEventImp
    11: UEventImp
    12: UGuardImp
    13: UGuardImp

The *UEventImp*, *UGuardImp* and *UActionImp* are referenced by the transition's model object
in the trigger, guard and action field.
- UActionImp.effectInv: an inverse look-up of the transition's model.

### UTransitionImp

    19: UTransitionImp
    - source[R]: 8(UCompositeStateImp)
    - target[R]: 9(UCompositeStateImp)
    - guard[R]: 13(UGuardImp)
    - effect[R]: 5(UActionImp)
    - trigger[R]: 11(UEventImp)
    - transitionsInv[R]: 16(UStateMachineImp)
    - presentation[C]: [3(TransitionPresentation)]
    - ex_observers[C]: [3(TransitionPresentation)]
    20: UTransitionImp
    - source[R]: 8(UCompositeStateImp)
    - target[R]: 8(UCompositeStateImp)
    - guard[R]: 12(UGuardImp)
    - effect[R]: 6(UActionImp)
    - trigger[R]: 10(UEventImp)
    - transitionsInv[R]: 16(UStateMachineImp)
    - presentation[C]: [4(TransitionPresentation)]
    - ex_observers[C]: [4(TransitionPresentation)]

The *UTransitionImp* stores the model of transition.
- source/target: reference the model from and to the state machine.
- guard/effect/trigger: references the guard, action and trigger of the transition.
- transitionsInv: inverse look up to the state machine.
- presentation: references the view of the transition.
- ex_observers: only the view of the transition looks on it.
