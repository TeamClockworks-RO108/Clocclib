package ro.clockworks.clocclib.core.servotuner.states;

import ro.clockworks.clocclib.core.servotuner.AutoServo;
import ro.clockworks.clocclib.core.servotuner.ServoPositionsManager;

public class DisabledState <T extends Enum<T>> implements ServoState<T> {

    @Override
    public double signal(ServoPositionsManager spm, AutoServo<T> servo) {
        return 0.5;
    }

    @Override
    public boolean enabled() {
        return false;
    }

}
