package com.jayway.annostatemachine.multisignalconnectiontests;

import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.multisignalconnectiontests.generated.MultiSignalMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MultiSignalConnectionTests {


  @Mock
  StateMachineEventListener mMockEventListener;

  @Test
  public void firstLocalSignalConnectionCalledOnASignal() {
    MultiSignalMachineImpl machine = spy(new MultiSignalMachineImpl());
    machine.init(MultiSignalMachine.State.First, mMockEventListener);

    machine.send(MultiSignalMachine.Signal.AAAA);

    verify(machine).fromFirstToSecondOnAOrB();
    verify(mMockEventListener).onChangingState(MultiSignalMachine.State.First,
        MultiSignalMachine.State.Second);
  }

  @Test
  public void firstLocalSignalConnectionCalledOnBSignal() {
    MultiSignalMachineImpl machine = spy(new MultiSignalMachineImpl());
    machine.init(MultiSignalMachine.State.First, mMockEventListener);

    machine.send(MultiSignalMachine.Signal.BBBB);

    verify(machine).fromFirstToSecondOnAOrB();
    verify(mMockEventListener).onChangingState(MultiSignalMachine.State.First,
        MultiSignalMachine.State.Second);
  }

  @Test
  public void firstLocalSignalConnectionNotCalledOnCSignal() {
    MultiSignalMachineImpl machine = spy(new MultiSignalMachineImpl());
    machine.init(MultiSignalMachine.State.First, mMockEventListener);

    machine.send(MultiSignalMachine.Signal.CCCC);

    verify(machine, never()).fromFirstToSecondOnAOrB();
  }

  @Test
  public void secondLocalSignalConnectionNotCalledOnAorB() {
    MultiSignalMachineImpl machine = spy(new MultiSignalMachineImpl());
    machine.init(MultiSignalMachine.State.First, mMockEventListener);

    machine.send(MultiSignalMachine.Signal.AAAA);
    verify(machine).fromFirstToSecondOnAOrB();

    machine.send(MultiSignalMachine.Signal.AAAA);
    machine.send(MultiSignalMachine.Signal.BBBB);

    verify(machine, never()).fromSecondToThirdOnC();
  }

  @Test
  public void globalSignalSpyCalledForMultiSignals() {
    MultiSignalMachineImpl machine = spy(new MultiSignalMachineImpl());
    machine.init(MultiSignalMachine.State.First, mMockEventListener);

    machine.send(MultiSignalMachine.Signal.AAAA);
    verify(machine).fromFirstToSecondOnAOrB();
    verify(machine, never()).globalSpyOnBAndC();

    reset(machine);

    machine.send(MultiSignalMachine.Signal.BBBB);
    verify(machine).globalSpyOnBAndC();

    reset(machine);

    machine.send(MultiSignalMachine.Signal.CCCC);
    verify(machine).globalSpyOnBAndC();
    verify(machine).fromSecondToThirdOnC();
  }

  @Test
  public void localSignalSpyCalledForMultiSignals() {
    MultiSignalMachineImpl machine = spy(new MultiSignalMachineImpl());
    machine.init(MultiSignalMachine.State.First, mMockEventListener);

    machine.send(MultiSignalMachine.Signal.EEEE);
    verify(machine).localSpyOnEAndFInFirst();

    machine.send(MultiSignalMachine.Signal.F);
    verify(machine, times(2)).localSpyOnEAndFInFirst();
  }

  @Test
  public void globalSpecificSignalsTransitionCalledForMultiSignals() {
    MultiSignalMachineImpl machine = spy(new MultiSignalMachineImpl());
    machine.init(MultiSignalMachine.State.First, mMockEventListener);

    machine.send(MultiSignalMachine.Signal.U);
    machine.send(MultiSignalMachine.Signal.V);
    verify(machine, times(2)).globalUorVTransition();
  }

  @StateMachine
  public static class MultiSignalMachine {

    @Signals
    public enum Signal {
      AAAA,
      BBBB,
      CCCC,
      EEEE,
      F,
      U,
      V
    }

    @States
    public enum State {
      First, Second, Third, UorVWasReceived
    }

    @Connection(from = "First", to = "Second", on = "AAAA,BBBB")
    protected void fromFirstToSecondOnAOrB() {
    }

    @Connection(from = "Second", to = "Third", on = "CCCC")
    protected void fromSecondToThirdOnC() {
    }

    @Connection(from = "*", to = "*", on="BBBB,CCCC")
    protected void globalSpyOnBAndC() {
    }

    @Connection(from = "First", to = "*", on="  EEEE,    F ")
    protected void localSpyOnEAndFInFirst() {
    }

    @Connection(from="*", to="UorVWasReceived", on="U, V")
    protected void globalUorVTransition() {
    }
  }
}
