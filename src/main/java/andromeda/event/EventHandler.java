package andromeda.event;

import java.util.ArrayList;
import java.util.List;

public class EventHandler {

    private static EventHandler eventHandler;
    private List<WindowResizeCallback> windowResizeCallbacks;

    public EventHandler() {
        windowResizeCallbacks = new ArrayList<>();
    }

    public static EventHandler get() {
        if(eventHandler == null) {
            eventHandler = new EventHandler();
        }
        return eventHandler;
    }

    public void onWindowResize(int width, int height) {
        for (var callback : windowResizeCallbacks) {
            callback.resize(width, height);
        }
    }

    public void addWindowResizeCallback(WindowResizeCallback callback) {
        windowResizeCallbacks.add(callback);
    }
}
