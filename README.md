# anno-statechart

This library provides a simple way to create versatile state machines.

## Quick example
```java
@StateMachine
public class MyStateMachine {

  @Signals public enum Signal { SayHello }
  @States public enum States { Strangers, Introduced }
  
  @Connection(from = "Strangers", to="Introduced", signal="SayHello")
  protected boolean sayHello(SignalPayload payload) {
    System.out.println("Hello");
    return true;
  }
}
```

The snippet above creates a fully functioning state machine class in the sub package "**generated**". The class is named after the source class but with an "**Impl**" suffix.

com.package.**MyStateMachine** is converted to com.package.**generated.MyStateMachineImpl**

This is how you use it:
```java
MyStateMachineImpl stateMachine = new MyStateMachineImpl();
stateMachine.init(MyStateMachine.State.Strangers, null);
stateMachine.send(MyStateMachine.Signal.SayHello);
```
The three lines will result in "Hello" being printed.

## What if I make a typo in the @Connection annotation?
The validity of the state machine declaration is checked at compile time. You will get a build error if you mistype a state or signal name.

You can find an extensive [manual](https://github.com/jayway/anno-statechart/wiki/manual) as well as other resources on the [wiki](https://github.com/jayway/anno-statechart/wiki/).
