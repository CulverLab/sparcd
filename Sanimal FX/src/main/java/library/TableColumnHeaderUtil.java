package library;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

/**
 * Contains utility functions relating to table clumns
 */
public class TableColumnHeaderUtil
{
	/**
	 * Given a table column, this function ensures that the table column header will wrap instead of going off the edge
	 * This function is found here:
	 * https://stackoverflow.com/questions/10952111/javafx-2-0-table-with-multiline-table-header
	 *
	 * @param col The column to update the header of
	 */
	public static void makeHeaderWrappable(TableColumn col)
	{
		Label label = new Label(col.getText());
		label.setStyle("-fx-padding: 8px;");
		label.setWrapText(true);
		label.setAlignment(Pos.CENTER);
		label.setTextAlignment(TextAlignment.CENTER);

		StackPane stack = new StackPane();
		stack.getChildren().add(label);
		stack.prefWidthProperty().bind(col.widthProperty().subtract(5));
		label.prefWidthProperty().bind(stack.prefWidthProperty());
		col.setGraphic(stack);
	}
}
