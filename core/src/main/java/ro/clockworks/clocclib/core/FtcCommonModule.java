package ro.clockworks.clocclib.core;

import com.google.inject.AbstractModule;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

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
        bind(Gamepad.class).annotatedWith(Gamepad1.class).toInstance(mapper.getGamepad(Gamepad1.class));
        bind(Gamepad.class).annotatedWith(Gamepad2.class).toInstance(mapper.getGamepad(Gamepad2.class));
        bind(Gamepad.class).annotatedWith(GamepadConfig.class).toInstance(mapper.getGamepad(GamepadConfig.class));
        bind(Gamepad.class).annotatedWith(GamepadTuner.class).toInstance(mapper.getGamepad(GamepadTuner.class));
    }




}
