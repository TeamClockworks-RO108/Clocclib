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

    public AutoServo(String servoHwName, String analogHwName) {
        this.servoHwName = servoHwName;
        this.analogHwName = analogHwName;
        servo = hardwareMap.get(ServoImplEx.class, servoHwName);
        try {
            analog = hardwareMap.get(AnalogInput.class, analogHwName);
        } catch (Exception e) {
            analog = null;
        }
    }

    void init(T initialPose) {
        possiblePoses = Arrays.stream(initialPose.getClass().getEnumConstants()).map(c -> (T)c).collect(Collectors.toList());
        currentPose = initialPose;
        positionsManager = ServoPositionsManager.instance();

        // Force all positions to be created in the manager
        possiblePoses.forEach(p -> positionsManager.getPosition(this, p));
    }

    void goTo(T pose) {

    }

    void disable() {

    }



    void update() {

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
