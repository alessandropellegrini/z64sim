package it.uniroma2.pellegrini.z64sim.queue;

public abstract class EventDispatchable {

    public EventDispatchable() {
        Dispatcher.registerDispatchable(this);
    }

    public abstract boolean dispatch(Events command);
}
