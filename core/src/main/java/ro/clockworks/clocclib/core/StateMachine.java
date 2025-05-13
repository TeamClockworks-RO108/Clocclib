package ro.clockworks.clocclib.core;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class StateMachine <E extends Enum<?>> {

    boolean runningUpdate = false;

    private E currentState = null;
    private E initialState;

    private E forceStateTo = null;

    private long zeroTime = 0;
    private final Map<EventType, Map<E, EventHandler<E>>> events = new HashMap<>();

    private final List<StateChangeListener<E>> stateChangeListener = new ArrayList<>();

    public StateMachine(E initialState) {
        this.initialState = initialState;
    }

    public void init() {
        currentState = initialState;
        _update(true);
    }

    public void onStateEnter(E state, EventHandler<E> handler) {
        events.computeIfAbsent(EventType.ENTER, __ -> new HashMap<>()).put(state, handler);
    }
    public void onStateExit(E state, EventHandler<E> handler) {
        events.computeIfAbsent(EventType.EXIT, __ -> new HashMap<>()).put(state, handler);
    }
    public void onStateUpdate(E state, EventHandler<E> handler) {
        events.computeIfAbsent(EventType.UPDATE, __ -> new HashMap<>()).put(state, handler);
    }

    public void onStateEnter(E state, EventHandlerNoParameters<E> handler) {
        events.computeIfAbsent(EventType.ENTER, __ -> new HashMap<>()).put(state, (__, ___) -> handler.handle());
    }
    public void onStateExit(E state, EventHandlerNoParameters<E> handler) {
        events.computeIfAbsent(EventType.EXIT, __ -> new HashMap<>()).put(state, (__, ___) -> handler.handle());
    }
    public void onStateUpdate(E state, EventHandlerNoParameters<E> handler) {
        events.computeIfAbsent(EventType.UPDATE, __ -> new HashMap<>()).put(state, (__, ___) -> handler.handle());
    }

    public void onStateEnter(E state, EventHandlerNoParametersNoTransition<E> handler) {
        events.computeIfAbsent(EventType.ENTER, __ -> new HashMap<>()).put(state, (__, ___) -> { handler.handle(); return null; });
    }
    public void onStateExit(E state, EventHandlerNoParametersNoTransition<E> handler) {
        events.computeIfAbsent(EventType.EXIT, __ -> new HashMap<>()).put(state, (__, ___) -> { handler.handle(); return null; });
    }
    public void onStateUpdate(E state, EventHandlerNoParametersNoTransition<E> handler) {
        events.computeIfAbsent(EventType.UPDATE, __ -> new HashMap<>()).put(state, (__, ___) -> { handler.handle(); return null; });
    }

    public void onStateUpdateDoChange(E state, E to, Supplier<Boolean> condition) {
        events.computeIfAbsent(EventType.UPDATE, __ -> new HashMap<>()).put(state, (__, ___) -> condition.get() ? to : null);
    }
    public void onStateEnterDoChange(E state, E to, Supplier<Boolean> condition) {
        events.computeIfAbsent(EventType.ENTER, __ -> new HashMap<>()).put(state, (__, ___) -> condition.get() ? to : null);
    }

    public void onStateUpdateDoChange(E state, E to, EventHandlerTransition condition) {
        events.computeIfAbsent(EventType.UPDATE, __ -> new HashMap<>()).put(state, (__, time) -> condition.handle(time) ? to : null);
    }
    public void onStateEnterDoChange(E state, E to, EventHandlerTransition condition) {
        events.computeIfAbsent(EventType.ENTER, __ -> new HashMap<>()).put(state, (__, time) -> condition.handle(time) ? to : null);
    }


    public void onAnyStateChange(StateChangeListener<E> listener) {
        stateChangeListener.add(listener);
    }

    public void forceTransitionTo(E state) {
        if (runningUpdate)
            throw new IllegalStateException("Trying to force a state machine to run a transition while update is running.");
        forceStateTo = state;
        _update(false);
    }

    public void update() {
        _update(false);
    }

    private void _update(boolean fst) {
        if (currentState == null)
            return;

        E nextState = null;

        if (forceStateTo != null)
            nextState = forceStateTo;

        runningUpdate = true;
        while (true) {
            if (nextState != null) {
                dispatchEvent(EventType.EXIT);
                E lastState = currentState;
                currentState = nextState;
                zeroTime = System.currentTimeMillis();
                for (StateChangeListener<E> l : stateChangeListener) l.handle(lastState, nextState, 0L);
                fst = true;
            }

            if (fst) {
                fst = false;
                nextState = dispatchEvent(EventType.ENTER);
                if (nextState != null)
                    continue;
            }

            nextState = dispatchEvent(EventType.UPDATE);

            if (nextState == null)
                break;
        }
        runningUpdate = false;
    }

    private E dispatchEvent(EventType type) {
        if (!events.containsKey(type))
            return null;
        if (!Objects.requireNonNull(events.get(type)).containsKey(currentState))
            return null;

        long time = System.currentTimeMillis();
        E newState = Objects.requireNonNull(Objects.requireNonNull(events.get(type)).get(currentState)).handle(currentState, time - zeroTime);
        return type.allowTransitions() ? newState : null;
    }

    public E getCurrentState() {
        return currentState;
    }

    private enum EventType {
        ENTER(true), UPDATE(true), EXIT(false);

        private final boolean allowTransitions;

        public boolean allowTransitions() {
            return allowTransitions;
        }

        EventType(boolean allowTransitions) {
            this.allowTransitions = allowTransitions;
        }

    }

    public interface EventHandler<EE> {
        /**
         *
         * @return the new state the machine should transition, or null if no transition is required.
         * If called for an exit event, return values is ignored.
         */
        EE handle(EE current, long timeSinceTransition);
    }

    public interface EventHandlerNoParameters<EE> {
        /**
         *
         * @return the new state the machine should transition, or null if no transition is required.
         * If called for an exit event, return values is ignored.
         */
        EE handle();
    }

    public interface EventHandlerNoParametersNoTransition<EE> {
        /**
         *
         * @return the new state the machine should transition, or null if no transition is required.
         * If called for an exit event, return values is ignored.
         */
        void handle();
    }

    public interface EventHandlerNoTransition<EE> {
        void handle(EE current, long timeSinceTransition);
    }

    public interface EventHandlerTransition {
        boolean handle(long timeSinceTransition);
    }

    public interface StateChangeListener<EE> {
        void handle(EE last, EE current, long timeSinceTransition);
    }

}