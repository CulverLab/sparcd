package library;

import javafx.event.EventHandler;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

/**
 * Utility class designed to make segmented buttons un-deselectable, meaning they cant be deselected.
 */
public class ToggleButtonSelector
{
	// Used below, simply consumes the event if the toggle button is selected so it does not get deselected
	private static final EventHandler<MouseEvent> CONSUME_MOUSE_EVENTFILTER = (MouseEvent mouseEvent) -> {
		if (((Toggle) mouseEvent.getSource()).isSelected()) {
			mouseEvent.consume();
		}
	};

	/**
	 * We add event filters for the click, press, and released events that get consumed if the button is alredy pressed
	 *
	 * @param button The button to make unselectable
	 */
	public static void makeUnselectable(ToggleButton button)
	{
		button.addEventFilter(MouseEvent.MOUSE_PRESSED, CONSUME_MOUSE_EVENTFILTER);
		button.addEventFilter(MouseEvent.MOUSE_CLICKED, CONSUME_MOUSE_EVENTFILTER);
		button.addEventFilter(MouseEvent.MOUSE_RELEASED, CONSUME_MOUSE_EVENTFILTER);
	}
}
