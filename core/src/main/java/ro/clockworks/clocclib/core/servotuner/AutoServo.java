package ro.clockworks.clocclib.core.servotuner;

import com.google.inject.Inject;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AutoServo<T extends Enum<T>> {

    private final String servoHwName;
    private final String analogHwName;

    @Inject
    private HardwareMap hardwareMap;

    @Inject
    private Telemetry telemetry;

    @Inject
    private ServoPositionsManager positionsManager;

    private ServoImplEx servo;

    private AnalogInput analog;

    private List<T> possiblePoses;

    private T currentPose;

    private Double cachedPwmPower;

    private boolean inited;

    public AutoServo(String servoHwName, String analogHwName) {
        this.servoHwName = servoHwName;
        this.analogHwName = analogHwName;
        servo = hardwareMap.get(ServoImplEx.class, servoHwName);
        try {
            analog = hardwareMap.get(AnalogInput.class, analogHwName);
        } catch (Exception e) {
            analog = null;
        }
        inited = false;
    }

    public void init(T initialPose, boolean enabled) {
        if (initialPose == null)
            throw new IllegalArgumentException("AutoServo initial post MUST NOT ne null. If starting disabled, give false to the second parameter and give any pose as initial");
        if (initialPose.getClass().getEnumConstants() == null)
            throw new IllegalArgumentException("AutoServo type parameter " + initialPose.getClass().getSimpleName() + " is not an enum");

        possiblePoses = Arrays.stream(initialPose.getClass().getEnumConstants()).map(c -> (T)c).collect(Collectors.toList());
        currentPose = enabled ? initialPose : null;
        cachedPwmPower = null;

        // Force all positions to be created in the manager
        possiblePoses.forEach(p -> positionsManager.getPosition(this, p));

        inited = true;
    }

    public void goTo(T pose) {
        currentPose = pose;
    }

    public void disable() {
        currentPose = null;
    }



    public void update() {
        if (!inited)
            return;

        ServoPositionsManager.PositionEntry pos = positionsManager.getPosition(this, currentPose);
        if (pos == null) {
            if (cachedPwmPower != null) {
                servo.setPwmDisable();
                cachedPwmPower = null;
            }
        } else {
            if (cachedPwmPower == null || !cachedPwmPower.equals(pos.value())) {
                if (cachedPwmPower == null)
                    servo.setPwmEnable();
                servo.setPosition(pos.value());
                cachedPwmPower = pos.value();
            }
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
}
