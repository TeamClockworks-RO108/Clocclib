package ro.clockworks.clocclib.core.servotuner.states;

import ro.clockworks.clocclib.core.servotuner.AutoServo;
import ro.clockworks.clocclib.core.servotuner.ServoPositionsManager;

import java.util.Objects;

public class InterpolatedState<T extends Enum<T>> implements ServoState<T> {

    private final T from;
    private final T to;
    private final double progress;

    public InterpolatedState(T from, T to, double progress) {
        this.from = from;
        this.to = to;
        this.progress = progress;
    }

    public T from() {
        return from;
    }

    public T to() {
        return to;
    }

    public double progress() {
        return progress;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InterpolatedState)) return false;
        InterpolatedState<?> that = (InterpolatedState<?>) o;
        return progress == that.progress && Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, progress);
    }

    @Override
    public double signal(ServoPositionsManager spm, AutoServo<T> servo) {
        double fs = spm.getPosition(servo, from).value();
        double sn = spm.getPosition(servo, to).value();
        double delta = sn - fs;
        return fs + delta * progress;
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
