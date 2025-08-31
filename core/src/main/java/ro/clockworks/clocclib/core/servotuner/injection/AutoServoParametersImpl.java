package ro.clockworks.clocclib.core.servotuner.injection;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;

public class AutoServoParametersImpl implements AutoServoParameters, Serializable {

    private static final long serialVersionUID = 0;
    private final String servoHardwareName;
    private final String analogHardwareName;

    public AutoServoParametersImpl(String servoHardwareName, String analogHardwareName) {
        this.servoHardwareName = servoHardwareName;
        this.analogHardwareName = analogHardwareName;
    }

    @Override
    public String servoHardwareName() {
        return servoHardwareName;
    }

    @Override
    public String analogHardwareName() {
        return analogHardwareName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AutoServoParameters)) return false;
        AutoServoParameters that = (AutoServoParametersImpl) o;
        return Objects.equals(servoHardwareName, that.servoHardwareName()) && Objects.equals(analogHardwareName, that.analogHardwareName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(servoHardwareName, analogHardwareName);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return AutoServoParameters.class;
    }
}
