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

        setupAppEdgers();

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



    private T editingPosition;
    private double editingValue;
    private double originalValue;

    private final EdgeDetector upEdge = new EdgeDetector(false);
    private final EdgeDetector downEdge = new EdgeDetector(true);
    private final EdgeDetector leftEdge = new EdgeDetector(false);
    private final EdgeDetector rightEdge = new EdgeDetector(true);

    @Override
    public String name() {
        return "ServoTuner for " + servoHwName;
    }

    private void setupAppEdgers() {
        rightEdge.onPress(() -> positionOffset(1));
        leftEdge.onPress(() -> positionOffset(-1));
        upEdge.onPress(() -> editingValue = Math.min(editingValue + .01, 1));
        downEdge.onPress(() -> editingValue = Math.max(editingValue - .01, 0));
    }

    private void positionOffset(int count) {
        positionsManager.setValueInfo(this, editingPosition, editingValue);
        int index = possiblePoses.indexOf(editingPosition) + count;
        while (index > possiblePoses.size()) index -= possiblePoses.size();
        while (index < 0) index += possiblePoses.size();
        editingPosition = possiblePoses.get(index);
        originalValue = positionsManager.getPosition(this, editingPosition).value();
        editingValue = originalValue;
    }

    @Override
    public void updateApp() {
        if (analog != null) {
            telemetry.addLine("Auto tuning is available. Press X to start.");
        } else {
            telemetry.addLine("Auto tuning is not available. No analog input configured.");
        }
        telemetry.addLine("Press LEFT and RIGHT to select which position to tune");
        telemetry.addLine("Press UP and DOWN to adjust the current position");

        leftEdge.update(gamepad.dpad_left);
        rightEdge.update(gamepad.dpad_right);
        upEdge.update(gamepad.dpad_up);
        downEdge.update(gamepad.dpad_down);

        servo.setPosition(editingValue);
    }

    @Override
    public void enableApp() {
        appActive = true;
        editingPosition = possiblePoses.get(0);
        originalValue = positionsManager.getPosition(this, editingPosition).value();
        editingValue = originalValue;
    }

    @Override
    public void disableApp() {
        appActive = false;
    }
}
