package com.jayway.annostatemachine.errorhandling;

import com.jayway.annostatemachine.AnnoStateMachine;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.TestHelper;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.errorhandling.generated.MachineWithExceptionsImpl;
import com.jayway.annostatemachine.utils.SystemOutLogger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionsInConnectionsTests {

  @Mock
  private StateMachineEventListener mockEventListener;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    TestHelper.setLoggerForTest(new SystemOutLogger());
  }

  @Test
  public void npeReportedIfThrownInConnection() {
    MachineWithExceptionsImpl machine = new MachineWithExceptionsImpl();
    machine.init(MachineWithExceptions.State.Init, mockEventListener);

    machine.send(MachineWithExceptions.Signal.NPE);

    verify(mockEventListener).onThrowable(any(NullPointerException.class));
  }

  @Test
  public void iaeReportedIfThrownInConnection() {
    MachineWithExceptionsImpl machine = new MachineWithExceptionsImpl();
    machine.init(MachineWithExceptions.State.Init, mockEventListener);

    machine.send(MachineWithExceptions.Signal.IAE);

    verify(mockEventListener).onThrowable(any(IllegalArgumentException.class));
  }

  @StateMachine
  public static class MachineWithExceptions {

    private static final String TAG = MachineWithExceptions.class.getSimpleName();

    @Signals
    public enum Signal {
      NPE, IAE
    }

    @States
    public enum State {
      Init, NPE, IAE
    }

    public MachineWithExceptions() {
    }

    @Connection(from = "Init", to = "NPE", on = "NPE")
    protected void throwNPE() {
      throw new NullPointerException("Test exception");
    }

    @Connection(from = "Init", to = "IAE", on = "IAE")
    protected void throwIllegalArgumentException() {
      throw new IllegalArgumentException("Test exception");
    }

  }

}
