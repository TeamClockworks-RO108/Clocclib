package ro.clockworks.clocclib.core.algo;


public class PID {
    protected double kp, ki, kd; // Constants which we choose experimentally
    protected double lastError;
    protected double lastTime;
    protected double integral;
    protected double target;

    protected double integralLimit;



    public double target() {
        return target;
    }

    public void target(double TARGET) {
        this.target = TARGET;
    }

    /**
     * Initialization
     * @param kp constant for the proportional
     * @param ki constant for the integral
     * @param kd constant for the derivative
     * @param integralLimit limit integral term to a fraction of the possible output
     */
    public PID(double kp, double ki, double kd, double integralLimit) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.integralLimit = integralLimit;
        target = 0;
        reset();
    }

    /**
     * setting the target
     * @param target the target for the PID controller
     */
    public void setTarget(double target) {
        this.target = target;
    }

    /**
     * The derivative measures the error in the elapsed time
     * The integral approximates the area of error (how far the robot has deviated)
     * @param value input value
     * @param time the elapsed time
     * @return returning the correction
     */
    public double feed(double value, double time) {
        double error = target - value;
        double derivative = (error - lastError) / (time - lastTime);
        integral += (error * time);
        if (integral != 0) {
            double limit = Math.abs(integralLimit / integral);
            if (integral < -limit) {
                integral = - limit;
            }
            if (integral > limit) {
                integral = limit;
            }
        }

        lastError = error;
        lastTime = time;
        return error * kp + integral * ki + derivative * kd; //The equation
    }

    public void reset() {
        lastError = 0;
        lastTime = 0;
        integral = 0;
    }

    public double kp() {
        return kp;
    }

    public void kp(double kp) {
        this.kp = kp;
    }

    public double ki() {
        return ki;
    }

    public void ki(double ki) {
        this.ki = ki;
    }

    public double kd() {
        return kd;
    }

    public void kd(double kd) {
        this.kd = kd;
    }

    public void integralLimitFactor(double factor) {
        this.integralLimit = factor;
    }

    public double integralLimit() {
        return integralLimit;
    }
}