package ro.clockworks.clocclib.core.servotuner;

import android.os.Environment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServoPositionsManager {

    private static ServoPositionsManager singletonInstance = null;

    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<ServoEntry> servosDatabase = new ArrayList<>();

    private final Set<AutoServo<?>> registeredServos = new HashSet<>();

    private final ExecutorService ioService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("ServoPositionsManager IO Writer");
        t.setDaemon(true);
        return t;
    });

    private ServoPositionsManager() {
    }

    public synchronized static ServoPositionsManager instance() {
        if (singletonInstance == null) {
            singletonInstance = new ServoPositionsManager();
        }
        singletonInstance.readDb();
        return singletonInstance;
    }

    private void readDb() {
        File location = getFileDbPath();
        try {
            if (!location.exists()) {
                servosDatabase.clear();
                return;
            }
            servosDatabase.clear();
            servosDatabase.addAll(mapper.readerForArrayOf(ServoEntry.class).readValue(new FileReader(location)));
        } catch (FileNotFoundException fnf) {
            // nop
        } catch (JsonProcessingException jpe) {
            for (int i = 0; i < 999; i++) {
                File newLoc = new File(getFileDbPath().getParentFile(), location.getName() + ".bak." + i);
                if (!newLoc.exists()) {
                    location.renameTo(newLoc);
                    break;
                }
            }
            servosDatabase.clear();
        } catch (IOException ioe) {
            servosDatabase.clear();
        }
    }

    private void writeDb() {
        CompletableFuture.runAsync(() -> {
            File location = getFileDbPath();
            try {
                mapper.writeValue(location, servosDatabase);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, ioService);
    }

    public <T extends Enum<T>> PositionEntry getPosition(AutoServo<T> servo, T position) {
        registeredServos.add(servo);
        if (position == null)
            return null;

        ServoEntry entry = servosDatabase.stream().filter(se -> se.hwname().equals(servo.servoHwName())).findAny().orElse(null);
        if (entry == null) {
            entry = new ServoEntry();
            entry.hwname(servo.servoHwName());
            Random random = new Random(System.currentTimeMillis());
            entry.positions(servo.possiblePoses().stream().map(t -> new PositionEntry(t.name(), 0.4 + random.nextInt(100) / 500.0)).collect(Collectors.toList()));
            servosDatabase.add(entry);
        }
        PositionEntry pos = entry.positions().stream().filter(pp -> pp.name().equals(position.name())).findAny().orElse(null);
        if (pos == null) {
            pos = new PositionEntry(position.name(), 0.5);
            entry.positions().add(pos);
            writeDb();
        }
        return pos;
    }

    public <T extends Enum<T>> void setAnalogInfo(AutoServo<T> servo, T position, double analog) {
        if (position == null)
            return;

        getPosition(servo, position).analog(analog);
        writeDb();
    }

    public <T extends Enum<T>> void setValueInfo(AutoServo<T> servo, T position, double value) {
        if (position == null)
            return;

        getPosition(servo, position).value(value);
        writeDb();
    }

    public void forEachServo(Consumer<AutoServo<?>> action) {
        registeredServos.forEach(action);
    }

    private File getFileDbPath() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/servo-positions.json");
    }


    public static class ServoEntry {
        private String hwname;

        private List<PositionEntry> positions;

        public String hwname() {
            return hwname;
        }

        public ServoEntry hwname(String hwname) {
            this.hwname = hwname;
            return this;
        }

        public List<PositionEntry> positions() {
            return positions;
        }

        public ServoEntry positions(List<PositionEntry> positions) {
            this.positions = positions;
            return this;
        }
    }

    public static class PositionEntry {
        private String name;
        private Double value;

        private Double analog;

        public PositionEntry(String name, Double value) {
            this.name = name;
            this.value = value;
            analog = null;
        }

        public String name() {
            return name;
        }

        public PositionEntry name(String name) {
            this.name = name;
            return this;
        }

        public Double value() {
            return value;
        }

        public PositionEntry value(Double value) {
            this.value = value;
            return this;
        }

        public Double analog() {
            return analog;
        }

        public PositionEntry analog(Double analog) {
            this.analog = analog;
            return this;
        }
    }



}
