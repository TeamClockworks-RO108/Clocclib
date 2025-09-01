package ro.clockworks.clocclib.core.servotuner;

import com.google.inject.Inject;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import ro.clockworks.clocclib.core.EdgeDetector;
import ro.clockworks.clocclib.core.gamepads.GamepadConfig;
import ro.clockworks.clocclib.core.gamepads.GamepadTuner;
import ro.clockworks.clocclib.core.gamepads.TunerApp;
import ro.clockworks.clocclib.core.servotuner.states.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class AutoServo<T extends Enum<T>> implements TunerApp {

    private final String servoHwName;
    private final String analogHwName;

    private final ServoImplEx servo;

    private final AnalogInput analog;

    @Inject
    private HardwareMap hardwareMap;

    @Inject
    private Telemetry telemetry;

    @Inject
    private ServoPositionsManager positionsManager;

    @Inject @GamepadTuner
    private Gamepad gamepad;


    private List<T> possiblePoses;

    private ServoState<T> currentState;

    private boolean inited;

    private boolean appActive;

    public AutoServo(String servoHwName, String analogHwName) {
        this.servoHwName = servoHwName;
        this.analogHwName = analogHwName;
        servo = hardwareMap.get(ServoImplEx.class, servoHwName);
        AnalogInput an = null;
        try {
            an = hardwareMap.get(AnalogInput.class, analogHwName);
        } catch (Exception e) {
            // not found - nothing
        }
        this.analog = an;
        inited = false;
    }

    public void init(T initialPose, boolean enabled) {
        if (initialPose == null)
            throw new IllegalArgumentException("AutoServo initial post MUST NOT ne null. If starting disabled, give false to the second parameter and give any pose as initial");
        if (initialPose.getClass().getEnumConstants() == null)
            throw new IllegalArgumentException("AutoServo type parameter " + initialPose.getClass().getSimpleName() + " is not an enum");

        possiblePoses = Arrays.stream(initialPose.getClass().getEnumConstants()).map(c -> (T)c).collect(Collectors.toList());
        currentState = enabled ? new SimpleState<>(initialPose) : new DisabledState<>();

        // Force all positions to be created in the manager
        possiblePoses.forEach(p -> positionsManager.getPosition(this, p));

        inited = true;
    }

    public void goTo(T pose) {
        currentState = new SimpleState<>(pose);
    }

    public void goInterpolate(T form, T to, double progress) {
        currentState = new InterpolatedState<>(form, to, progress);
    }

    public void goPwm(double signal) {
        currentState = new PwmState<>(signal);
    }

    public void goDisable() {
        currentState = new DisabledState<>();
    }



    public void update() {
        if (!inited)
            return;

        if (appActive)
            return;

        boolean en = currentState.enabled();
        if (en != servo.isPwmEnabled()) {
            if (en) servo.setPwmEnable();
            else servo.setPwmDisable();
        }

        double pos = currentState.signal(positionsManager, this);
        if (en && servo.getPosition() != pos) {
            servo.setPosition(pos);
        }

    }

    public String servoHwName() {
        return servoHwName;
    }

    public String analogHwName() {
        return analogHwName;
    }

    public List<T> possiblePoses() {
        return possiblePoses;
    }

    



    @Override
    public String name() {
        return "ServoTuner for " + servoHwName;
    }

    @Override
    public void updateApp() {

    }

    @Override
    public void enableApp() {
        appActive = true;
    }

    @Override
    public void disableApp() {
        appActive = false;
    }
}
