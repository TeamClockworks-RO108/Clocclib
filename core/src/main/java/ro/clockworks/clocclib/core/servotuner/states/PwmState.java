package ro.clockworks.clocclib.core.servotuner.states;

import ro.clockworks.clocclib.core.servotuner.AutoServo;
import ro.clockworks.clocclib.core.servotuner.ServoPositionsManager;

import java.util.Objects;

public class PwmState<T extends Enum<T>> implements ServoState<T> {

    private final double pwm;

    public PwmState(double pwm) {
        this.pwm = pwm;
    }

    public double pwm() {
        return pwm;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PwmState)) return false;
        PwmState<?> pwmState = (PwmState<?>) o;
        return Double.compare(pwm, pwmState.pwm) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pwm);
    }

    @Override
    public double signal(ServoPositionsManager spm, AutoServo servo) {
        return pwm;
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
