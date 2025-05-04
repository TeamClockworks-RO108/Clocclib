package ro.clockworks.clocclib.core.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.xmlpull.v1.XmlPullParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Collectors;

import ro.clockworks.clocclib.core.EdgeDetector;
import ro.clockworks.clocclib.core.gamepads.Gamepad1;
import ro.clockworks.clocclib.core.gamepads.Gamepad2;
import ro.clockworks.clocclib.core.gamepads.GamepadConfig;
import ro.clockworks.clocclib.core.gamepads.GamepadMapper;
import ro.clockworks.clocclib.core.gamepads.GamepadTuner;
import ro.clockworks.clocclib.core.servotuner.AutoServo;
import ro.clockworks.clocclib.core.servotuner.AutoServoParameters;
import ro.clockworks.clocclib.core.servotuner.AutoServoParametersImpl;
import ro.clockworks.clocclib.core.servotuner.AutoServoProvider;

public class FtcCommonModule extends AbstractModule {

    private final HardwareMap hardwareMap;

    private final Gamepad gamepad1;

    private final Gamepad gamepad2;

    private final GamepadMapper mapper;

    private final Telemetry telemetry;

    public FtcCommonModule(HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.telemetry = telemetry;
        this.mapper = new GamepadMapper(gamepad1, gamepad2, telemetry);
    }

    @Override
    protected void configure() {
        bind(HardwareMap.class).toInstance(hardwareMap);
        bind(Telemetry.class).toInstance(telemetry);
        bind(GamepadMapper.class).toInstance(mapper);
        bind(Gamepad.class).annotatedWith(Gamepad1.class).toInstance(mapper.getGamepad(Gamepad1.class));
        bind(Gamepad.class).annotatedWith(Gamepad2.class).toInstance(mapper.getGamepad(Gamepad2.class));
        bind(Gamepad.class).annotatedWith(GamepadConfig.class).toInstance(mapper.getGamepad(GamepadConfig.class));
        bind(Gamepad.class).annotatedWith(GamepadTuner.class).toInstance(mapper.getGamepad(GamepadTuner.class));
        bind(EdgeDetector.class).annotatedWith(Gamepad1.class).toInstance(mapper.getGamepadEdger(Gamepad1.class));
        bind(EdgeDetector.class).annotatedWith(Gamepad2.class).toInstance(mapper.getGamepadEdger(Gamepad2.class));
        bind(EdgeDetector.class).annotatedWith(GamepadConfig.class).toInstance(mapper.getGamepadEdger(GamepadConfig.class));
        bind(EdgeDetector.class).annotatedWith(GamepadTuner.class).toInstance(mapper.getGamepadEdger(GamepadTuner.class));
        bindListener(Matchers.any(), new HardwareListener());

        for (String servo : hardwareMap.servo.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())) {
            for (String analog : hardwareMap.analogInput.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())) {
                bind(AutoServo.class).annotatedWith(new AutoServoParametersImpl(servo, analog)).toProvider(new AutoServoProvider(servo, analog));
            }
            bind(AutoServo.class).annotatedWith(new AutoServoParametersImpl(servo, null)).toProvider(new AutoServoProvider(servo, ""));
        }


    }


    private class HardwareListener implements TypeListener {

        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            Class<?> clazz = type.getRawType();
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Hardware.class)) {
                        encounter.register(new HardwareMembersInjector<>(field));
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
    }

    private class HardwareMembersInjector<T> implements MembersInjector<T> {
        private final Field field;

        public HardwareMembersInjector(Field field) {
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        public void injectMembers(Object instance) {
            try {
                Hardware hardware = field.getAnnotation(Hardware.class);
                String hardwareName = hardware.value();
                Class<?> clazz = field.getType();
                Object value = hardwareMap.get(clazz, hardwareName);
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static Injector globalInjector;

    public static Injector globalInjector() {
        return globalInjector;
    }

    public static void configureGlobalInjector(Injector injector) {
        globalInjector = injector;
    }
}
