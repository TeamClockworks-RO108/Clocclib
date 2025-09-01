package ro.clockworks.clocclib.core.servotuner.states;

import ro.clockworks.clocclib.core.servotuner.AutoServo;
import ro.clockworks.clocclib.core.servotuner.ServoPositionsManager;

import java.util.Objects;

public class SimpleState<T extends Enum<T>> implements ServoState<T> {

    private final T state;

    public SimpleState(T state) {
        this.state = state;
    }

    public T state() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleState)) return false;
        SimpleState<?> that = (SimpleState<?>) o;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(state);
    }

    @Override
    public double signal(ServoPositionsManager spm, AutoServo<T> servo) {
        return spm.getPosition(servo, state).value();
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
