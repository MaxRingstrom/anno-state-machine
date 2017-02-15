# anno-statechart

This library provides a simple way to create versatile state machines.

## Quick example
```java
@StateMachine
public class MyStateMachine {
  @Signals
  public enum Signal {
    SAY_HELLO
  }
  
  @States
  public enum States {
    STRANGERS, INTRODUCED
  }
  
  @Connection(from = "STRANGERS", to="INTRODUCED", signal="SAY_HELLO")
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
stateMachine.send(MyStateMachine.Signal.SAY_HELLO);
```
The two lines will result in "Hello" being printed.

## What if I introduce a typo in the @Connection annotation?
The validity of the statemachine declaration is checked at compile time. You will get a build error if you mistype a state or signal name.
