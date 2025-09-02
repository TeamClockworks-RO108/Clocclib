package ro.clockworks.clocclib.core.gamepads;

import com.qualcomm.robotcore.hardware.Gamepad;

public class HijackedGamepad extends Gamepad {

    private final GamepadMapper mapper;

    @Override
    public void setLedColor(double r, double g, double b, int durationMs) {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return;
        real.setLedColor(r, g, b, durationMs);
    }

    @Override
    public void rumble(double rumble1, double rumble2, int durationMs) {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return;
        real.rumble(rumble1, rumble2, durationMs);
    }

    @Override
    public void rumble(int durationMs) {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return;
        real.rumble(durationMs);
    }

    @Override
    public void runRumbleEffect(RumbleEffect effect) {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return;
        real.runRumbleEffect(effect);
    }

    @Override
    public void runLedEffect(LedEffect effect) {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return;
        real.runLedEffect(effect);
    }

    @Override
    public void rumbleBlips(int count) {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return;
        real.rumbleBlips(count);
    }

    @Override
    public void stopRumble() {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return;
        real.stopRumble();
    }

    @Override
    public boolean isRumbling() {
        Gamepad real = mapper.getRealGamepad(this);
        if (real == null)
            return false;
        return real.isRumbling();
    }



    public HijackedGamepad(GamepadMapper mapper) {
        super();
        this.mapper = mapper;
    }
}
