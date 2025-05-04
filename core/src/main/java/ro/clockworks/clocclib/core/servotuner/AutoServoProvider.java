package ro.clockworks.clocclib.core.servotuner;

import com.google.inject.Provider;

import ro.clockworks.clocclib.core.injection.FtcCommonModule;

public class AutoServoProvider implements Provider<AutoServo<?>> {

    private final String servo;
    private final String analog;

    public AutoServoProvider(String servo, String analog) {
        this.servo = servo;
        this.analog = analog;
    }

    @Override
    public AutoServo<?> get() {
        AutoServo<?> autoServo = new AutoServo<>(servo, analog);
        FtcCommonModule.globalInjector().injectMembers(autoServo);
        return autoServo;
    }
}
