# anno-state-machine

This library provides a simple way to create versatile state machines. State machines put the logical flow of your code in focus. You can easily follow the decisions that are made and you will no longer have to keep track of multiple fields that together determine the actions to take for specific events.

## Quick example
```java
@StateMachine
public class MyStateMachine {

  @Signals public enum Signal { SayHello }
  @States public enum State { Strangers, Introduced }
  
  @Connection(from = "Strangers", to="Introduced", on="SayHello")
  protected void sayHello(SignalPayload payload) {
    System.out.println("Hello");
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

Since the MyStateMachineImpl class is generated from the MyStateMachine class you have to make sure that the MyStateMachine class is compiled once before referencing MyStateMachineImpl. Compile your project once you've specified a Signal enum and a State enum and you should be fine.

## Benefits
* Less time spent on debugging and wondering why your application doesn't behave as it should
* Code that is easy to test with unit tests
* Simple handling of (chained) asynchronous events
* The possibility to draw diagrams that explain your code. (And even generate such diagrams in the future)

## What if I make a typo in the @Connection annotation?
The validity of the state machine declaration is checked at compile time. You will get a build error if you mistype a state or signal name.

You can find a [crash course](https://github.com/MaxRingstrom/anno-state-machine/wiki/Crash-course), an extensive [manual](https://github.com/jayway/anno-statechart/wiki/manual), [examples](https://github.com/jayway/anno-statechart/wiki/examples) and other resources on the [wiki](https://github.com/jayway/anno-statechart/wiki/).

See release notes [here](https://github.com/jayway/anno-statechart/wiki/Release-notes)
