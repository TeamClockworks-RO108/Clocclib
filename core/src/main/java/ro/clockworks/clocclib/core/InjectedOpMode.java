package ro.clockworks.clocclib.core;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public abstract class InjectedOpMode extends OpMode {

    protected Injector injector;

    @Inject
    private GamepadMapper mapper;

    @Override
    public final void init() {
        injector = Guice.createInjector(
                new FtcCommonModule(hardwareMap, gamepad1, gamepad2, telemetry)
        );
        injector.injectMembers(this);
        robotInit();
    }

    @Override
    public final void start() {
        super.start();
        robotStart();
    }

    @Override
    public final void loop() {
        mapper.preUpdate();
        robotLoop();
        telemetry.update();
        mapper.postUpdate();
    }

    protected abstract void robotInit();

    protected abstract void robotStart();

    protected abstract void robotLoop();
}
