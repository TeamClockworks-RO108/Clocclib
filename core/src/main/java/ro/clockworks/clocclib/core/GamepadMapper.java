package ro.clockworks.clocclib.core;


import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class GamepadMapper {

    private final Map<Class<? extends Annotation>, Gamepad> availableGamepads = Map.of(
            Gamepad1.class, new Gamepad(),
            Gamepad2.class, new Gamepad(),
            GamepadConfig.class, new Gamepad(),
            GamepadTuner.class, new Gamepad()
    );

    private final List<Class<? extends Annotation>> gamepadOrdering = List.of(
            Gamepad1.class, Gamepad2.class, GamepadConfig.class, GamepadTuner.class
    );

    private final MilticlickDetector[] multiclickDetectors = new MilticlickDetector[] {
            new MilticlickDetector(), new MilticlickDetector()
    };

    private final Class<? extends Annotation>[] mappings = new Class[]{Gamepad1.class, Gamepad2.class};



    private final Gamepad[] realGamepads;

    private final Telemetry telemetry;

    public GamepadMapper(Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        realGamepads = new Gamepad[] { gamepad1, gamepad2 };
        this.telemetry = telemetry;
    }

    public void preUpdate() {

        // Copy from real gamepads to code gamepads
        for (int i = 0; i < realGamepads.length; i++) {
            availableGamepads.get(mappings[i]).copy(realGamepads[i]);
        }

        // Check for buttons for mapping changes
        checkMappingsChange();
        printMappings();
    }

    public void postUpdate() {

        // Copy from code gamepads back to real gamepads (for vibration support)
        for (int i = 0; i < realGamepads.length; i++) {
            realGamepads[i].copy(availableGamepads.get(mappings[i]));
        }
    }

    private void checkMappingsChange() {

        boolean swap = false;
        long time = System.currentTimeMillis();

        for (int i = 0; i < realGamepads.length; i++) {
            int newMapping = multiclickDetectors[i].update(time, realGamepads[i].share);
            if (newMapping != 0) {
                newMapping -= 1;
                try {
                    Class<? extends Annotation> type = gamepadOrdering.get(newMapping);
                    mappings[i] = type;
                } catch (IndexOutOfBoundsException _unused) {
                    // do nothing - invalid command
                }
            }
        }

    }

    private void printMappings() {
        for (int i = 0; i < realGamepads.length; i++) {
            telemetry.addData("Gamepad " + i + " is mapped to ", mappings[i].getSimpleName().replace("Gamepad", "").toUpperCase());
        }
    }

    public Gamepad getGamepad(Class<? extends Annotation> clazz) {
        return availableGamepads.get(clazz);
    }



}


// TODO: Andrei make a better state machine!!!!
// this shit here is a hack
class MilticlickDetector {

    private int counter = 0;
    private long lastTime = 0;

    private long currentTime = 0;

    private final EdgeDetector detector = new EdgeDetector(false);

    public MilticlickDetector() {
        // We need to use functions for these shitz because of lambda capture will lock a single value for RHS of =
        detector.onPress(this::incrementCounter);
        detector.onRelease(this::saveTimestamp);
    }

    int update(long time, boolean state) {
        currentTime = time;
        detector.update(state);

        if (counter != 0 && time - lastTime > 700) {
            int cnt = counter;
            counter = 0;
            return cnt;
        }

        return 0;
    }

    private void incrementCounter() {
        counter++;
    }

    private void saveTimestamp() {
        lastTime = currentTime;
    }


}