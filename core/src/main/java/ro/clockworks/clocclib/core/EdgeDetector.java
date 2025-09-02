package ro.clockworks.clocclib.core;


import java.util.ArrayList;
import java.util.List;

public class EdgeDetector {

    private final List<Runnable> onPress = new ArrayList<>();
    private final List<Runnable> onRelease = new ArrayList<>();
    private final List<Runnable> onHold = new ArrayList<>();

    private boolean hasReleased = false, hasPressed = false;

    private boolean state;



    public EdgeDetector(boolean initialState) {
        this.state = initialState;
    }

    public void update(boolean newState) {

        hasReleased = false;
        hasPressed = false;

        // Update the state variable early on
        // To make calls to getter return the correct variable
        // When calling from a lambda function
        boolean oldState = state;
        state = newState;

        // Notify event handlers
        if (newState && !oldState) {
            hasPressed = true;
            onPress.forEach(Runnable::run);
        }
        if (!newState && oldState) {
            hasReleased = true;
            onRelease.forEach(Runnable::run);
        }
        if(newState && oldState){
            onHold.forEach(Runnable::run);
        }

    }

    public boolean hasReleased() {
        return hasReleased;
    }

    public boolean hasPressed() {
        return hasPressed;
    }

    public boolean consumeReleased() {
        boolean ans = hasReleased;
        hasReleased = false;
        return ans;
    }

    public boolean consumePressed() {
        boolean ans = hasPressed;
        hasPressed = false;
        return ans;
    }
    public void onPress(Runnable action) {
        onPress.add(action);
    }

    public void onHold(Runnable action){
        onHold.add(action);
    }

    public void onRelease(Runnable action) {
        onRelease.add(action);
    }

    public boolean state() {
        return state;
    }

}
