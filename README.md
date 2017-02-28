# anno-state-machine

This library provides a simple way to create versatile state machines. State machines put the logical flow of your code in focus. You can easily follow the decisions that are made and you will no longer have to keep track of multiple fields that together determine the actions to take for specific events.

The result is:
* Less time spent on debugging and wondering why your application doesn't behave as it should
* Code that is easy to test with unit tests
* Simple handling of (chained) asynchronous events
* The possibility to draw diagrams that explain your code. (And even generate such diagrams in the future)

## Quick example
```java
@StateMachine
public class MyStateMachine {

  @Signals public enum Signal { SayHello }
  @States public enum State { Strangers, Introduced }
  
  @Connection(from = "Strangers", to="Introduced", on="SayHello")
  protected boolean sayHello(SignalPayload payload) {
    System.out.println("Hello");
    return true;
  }
}
```
UML diagram for the state machine:

![An UML diagram for the state machine](https://github.com/jayway/anno-statechart/blob/master/doc/diagrams/SayHelloMachine.png)

The code above creates a fully functioning state machine class in the sub package "**generated**". The class is named after the source class but with an "**Impl**" suffix.

com.package.**MyStateMachine** is converted to com.package.**generated.MyStateMachineImpl**

This is how you use it:
```java
MyStateMachineImpl stateMachine = new MyStateMachineImpl();
stateMachine.init(Strangers);
stateMachine.send(SayHello);
```
The three lines will result in "Hello" being printed.

## What if I make a typo in the @Connection annotation?
The validity of the state machine declaration is checked at compile time. You will get a build error if you mistype a state or signal name.

You can find an extensive [manual](https://github.com/jayway/anno-statechart/wiki/manual) as well as other resources on the [wiki](https://github.com/jayway/anno-statechart/wiki/).
