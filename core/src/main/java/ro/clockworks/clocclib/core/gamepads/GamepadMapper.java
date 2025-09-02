package ro.clockworks.clocclib.core.gamepads;


import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.clockworks.clocclib.core.EdgeDetector;

public class GamepadMapper {

    private final Map<Class<? extends Annotation>, HijackedGamepad> availableGamepads = Map.of(
            Gamepad1.class, new HijackedGamepad(this),
            Gamepad2.class, new HijackedGamepad(this),
            GamepadConfig.class, new HijackedGamepad(this),
            GamepadTuner.class, new HijackedGamepad(this)
    );

    private final Map<Class<? extends Annotation>, EdgeDetector> availableGamepadsEdgers = Map.of(
            Gamepad1.class, new EdgeDetector(false),
            Gamepad2.class, new EdgeDetector(false),
            GamepadConfig.class, new EdgeDetector(false),
            GamepadTuner.class, new EdgeDetector(false)
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

    private boolean enableBackcopy = false;

    private final List<TunerApp> apps = new ArrayList<>();

    private int selectedApp = 0;

    private final EdgeDetector nextApp = new EdgeDetector(false);
    private final EdgeDetector prevApp = new EdgeDetector(false);

    public GamepadMapper(Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        realGamepads = new Gamepad[] { gamepad1, gamepad2 };
        this.telemetry = telemetry;

        for (int i = 0; i < realGamepads.length; i++) {
            final int finali = i;
            multiclickDetectors[i].onIncrement(c -> realGamepads[finali].rumbleBlips(c));
        }

        configureTunerApps();
    }

    public void init() {
        updateEdgers();
    }

    public void registerTunerApp(TunerApp app) {
        apps.add(app);
    }

    public void preUpdate() {

        // Copy from real gamepads to code gamepads
        for (int i = 0; i < realGamepads.length; i++) {
            availableGamepads.get(mappings[i]).copy(realGamepads[i]);
        }

        // Check for buttons for mapping changes
        checkMappingsChange();
        updateEdgers();
        updateTunerApps();
        printMappings();
    }

    private void checkMappingsChange() {

        long time = System.currentTimeMillis();

        for (int i = 0; i < realGamepads.length; i++) {
            int newMapping = multiclickDetectors[i].update(time, realGamepads[i].share ^ realGamepads[i].guide);
            if (newMapping != 0) {
                newMapping -= 1;
                try {
                    Class<? extends Annotation> type = gamepadOrdering.get(newMapping);
                    for (int j = 0; j < mappings.length; j++) {
                        if (i != j && type == mappings[j]) {
                            mappings[j] = mappings[i];
                            realGamepads[i].rumble(1, 1, 200);
                            break;
                        }
                    }
                    mappings[i] = type;
                } catch (IndexOutOfBoundsException _unused) {
                    // do nothing - invalid command
                }
            }
        }

    }

    private void printMappings() {
        for (int i = 0; i < realGamepads.length; i++) {
            telemetry.addData("Gamepad " + (i + 1) + " is mapped to", mappings[i].getSimpleName().replace("Gamepad", "").toUpperCase());
            //telemetry.addData("Gamepad " + (i + 1) + " counter at", multiclickDetectors[i].counter());
        }
    }

    private void updateEdgers() {
        Map<Class<? extends Annotation>, EdgeDetector> copyAvailableGamepadsEdgers = new HashMap<>(availableGamepadsEdgers);
        for (int i = 0; i < realGamepads.length; i++) {
            copyAvailableGamepadsEdgers.get(mappings[i]).update(true);
            copyAvailableGamepadsEdgers.remove(mappings[i]);
        }
        copyAvailableGamepadsEdgers.forEach((aClass, edgeDetector) -> edgeDetector.update(false));
        copyAvailableGamepadsEdgers.clear();
    }

    private void configureTunerApps() {
        nextApp.onPress(() -> {
            if (apps.isEmpty())
                return;
            apps.get(selectedApp).disableApp();
            selectedApp++;
            if (selectedApp == apps.size())
                selectedApp = 0;
            apps.get(selectedApp).enableApp();
        });
        prevApp.onPress(() -> {
            if (apps.isEmpty())
                return;
            apps.get(selectedApp).disableApp();
            selectedApp--;
            if (selectedApp < 0)
                selectedApp = apps.size() - 1;
            apps.get(selectedApp).enableApp();
        });
        getGamepadEdger(GamepadTuner.class).onPress(() -> {
            if (apps.isEmpty())
                return;
            apps.get(selectedApp).enableApp();
        });
        getGamepadEdger(GamepadTuner.class).onRelease(() -> {
            if (apps.isEmpty())
                return;
            apps.get(selectedApp).disableApp();
        });
    }

    private void updateTunerApps() {
        if (getGamepadEdger(GamepadTuner.class).state() && !apps.isEmpty()) {
            Gamepad tuner = getGamepad(GamepadTuner.class);
            nextApp.update(tuner.right_trigger > 0.05);
            prevApp.update(tuner.left_trigger > 0.05);
            telemetry.addLine("Tuner app " + selectedApp + ": " + apps.get(selectedApp).name());
            apps.get(selectedApp).updateApp();
        }
    }

    public Gamepad getGamepad(Class<? extends Annotation> clazz) {
        return availableGamepads.get(clazz);
    }

    public EdgeDetector getGamepadEdger(Class<? extends Annotation> clazz) {
        return availableGamepadsEdgers.get(clazz);
    }

    protected Gamepad getRealGamepad(HijackedGamepad hijackedGamepad) {
        for (int i = 0; i < realGamepads.length; i++) {
            if (availableGamepads.get(mappings[i]) == hijackedGamepad)
                return realGamepads[i];
        }
        return null;
    }



}


// TODO: Andrei make a better state machine!!!!
// this shit here is a hack
class MilticlickDetector {

    public int counter() {
        return counter;
    }

    private int counter = 0;
    private long lastTime = 0;

    private long currentTime = 0;

    private final EdgeDetector detector = new EdgeDetector(false);

    private final List<Consumer<Integer>> onInc = new ArrayList<>();

    public MilticlickDetector() {
        // We need to use functions for these shitz because of lambda capture will lock a single value for RHS of =
        detector.onPress(this::incrementCounter);
        detector.onRelease(this::saveTimestamp);
    }

    int update(long time, boolean state) {
        currentTime = time;
        detector.update(state);

        if (!state && counter != 0 && time - lastTime > 300) {
            int cnt = counter;
            counter = 0;
            return cnt;
        }

        return 0;
    }

    public void onIncrement(Consumer<Integer> action) {
        onInc.add(action);
    }

    private void incrementCounter() {
        counter++;
        for (Consumer<Integer> action : onInc) action.accept(counter);
    }

    private void saveTimestamp() {
        lastTime = currentTime;
    }


}