package ro.clockworks.clocclib.core.servotuner.states;

import ro.clockworks.clocclib.core.servotuner.AutoServo;
import ro.clockworks.clocclib.core.servotuner.ServoPositionsManager;

public interface ServoState<T extends Enum<T>> {

    double signal(ServoPositionsManager spm, AutoServo<T> servo);

    boolean enabled();

}
