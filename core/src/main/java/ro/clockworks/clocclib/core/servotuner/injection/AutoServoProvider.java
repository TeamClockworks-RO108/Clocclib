package ro.clockworks.clocclib.core.servotuner.injection;

import com.google.inject.Provider;

import ro.clockworks.clocclib.core.gamepads.GamepadMapper;
import ro.clockworks.clocclib.core.injection.FtcCommonModule;
import ro.clockworks.clocclib.core.servotuner.AutoServo;

public class AutoServoProvider implements Provider<AutoServo<?>> {

    private final String servo;
    private final String analog;

    private final GamepadMapper mapper;

    public AutoServoProvider(String servo, String analog, GamepadMapper mapper) {
        this.servo = servo;
        this.analog = analog;
        this.mapper = mapper;
    }

    @Override
    public AutoServo<?> get() {
        AutoServo<?> autoServo = new AutoServo<>(servo, analog);
        FtcCommonModule.globalInjector().injectMembers(autoServo);
        mapper.registerTunerApp(autoServo);
        return autoServo;
    }
}
