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
        mapper.init();
        robotInit();
    }

    @Override
    public void init_loop() {
        mapper.preUpdate();
        robotInitLoop();
        mapper.postUpdate();
        telemetry.update();
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
        mapper.postUpdate();
        telemetry.update();
    }


    protected abstract void robotInit();

    protected abstract void robotStart();

    protected abstract void robotLoop();

    protected void robotInitLoop() {
        // nothing
    }
}
