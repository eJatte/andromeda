package andromeda.input;

import org.joml.Vector2f;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Input {

    private enum KeyState {
        RELEASED, DOWN, PRESSED, UP
    }

    private class KeyEvent {
        public int key;
        public boolean pressed;

        public KeyEvent(int key, boolean pressed) {
            this.key = key;
            this.pressed = pressed;
        }
    }

    private static Input input_ref;

    public static Input get() {
        if (input_ref == null) {
            input_ref = new Input();
        }
        return input_ref;
    }

    private KeyState[] keys;
    private Queue<KeyEvent> keyEvents;

    private Queue<Vector2f> mousePosEvents;
    private Vector2f mousePosition, mouseDelta;

    private Input() {
        keys = new KeyState[KeyCode.KEY_LAST + 1];
        Arrays.fill(keys, KeyState.RELEASED);
        keyEvents = new LinkedList<>();
        mousePosEvents = new LinkedList<>();

        mousePosition = new Vector2f(0);
        mouseDelta = new Vector2f(0);
    }

    public void addKeyboardEvent(int key, boolean pressed) {
        keyEvents.add(new KeyEvent(key, pressed));
    }

    public void addMouseButtonEvent(int key, boolean pressed) {
        addKeyboardEvent(key + KeyCode.MOUSE_BUTTON_OFFSET, pressed);
    }

    public void addMousePositionEvent(Vector2f position) {
        mousePosEvents.add(position);
    }

    public void update() {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == KeyState.DOWN) {
                keys[i] = KeyState.PRESSED;
            } else if (keys[i] == KeyState.UP) {
                keys[i] = KeyState.RELEASED;
            }
        }

        while (!keyEvents.isEmpty()) {
            var event = keyEvents.remove();

            if (event.pressed && keys[event.key] == KeyState.RELEASED) {
                keys[event.key] = KeyState.DOWN;
            } else if (!event.pressed && keys[event.key] == KeyState.PRESSED) {
                keys[event.key] = KeyState.UP;
            }
        }

        var newMousePosition = mousePosition;

        while (!mousePosEvents.isEmpty()) {
            newMousePosition = mousePosEvents.remove();
        }

        mouseDelta = newMousePosition.sub(mousePosition, new Vector2f());
        mousePosition = newMousePosition;
    }

    public boolean key(int key) {
        return keys[key] == KeyState.PRESSED || keys[key] == KeyState.DOWN;
    }

    public boolean keyUp(int key) {
        return keys[key] == KeyState.UP;
    }

    public boolean keyDown(int key) {
        return keys[key] == KeyState.DOWN;
    }

    public Vector2f getMousePosition() {
        return mousePosition;
    }

    public Vector2f getMouseDelta() {
        return mouseDelta;
    }
}
