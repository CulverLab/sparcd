package controller.analysisView;

import controller.analysisView.conditions.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import model.SanimalData;
import model.location.Location;
import model.query.IQueryCondition;
import model.query.conditions.*;
import model.util.FXMLLoaderUtils;

public class QueryConditionsListCellController extends ListCell<IQueryCondition>
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public StackPane mainPane;
	@FXML
	public BorderPane contentPane;
	@FXML
	public Button btnRemoveCondition;

	///
	/// FXML Bound Fields End
	///

	@FXML
	public void initialize()
	{

	}

	@Override
	protected void updateItem(IQueryCondition queryCondition, boolean empty)
	{
		// Update the cell first
		super.updateItem(queryCondition, empty);

		// Set the text to null
		this.setText(null);

		// If the cell is empty we have no graphic
		if (empty && queryCondition == null)
		{
			this.setGraphic(null);
		}
		// if the cell is not empty, set the field's values and set the graphic
		else
		{
			if (queryCondition instanceof AddQueryCondition)
				this.btnRemoveCondition.setVisible(false);
			else
				this.btnRemoveCondition.setVisible(true);

			FXMLLoader fxml = FXMLLoaderUtils.loadFXML("analysisView/conditions/" + queryCondition.getFXMLConditionEditor());
			fxml.<IConditionController> getController().initializeData(queryCondition);
			contentPane.setCenter(fxml.getRoot());

			this.setGraphic(mainPane);
		}
	}

	public void clearCondition(ActionEvent actionEvent)
	{
		this.getListView().getItems().remove(this.getItem());
	}
}
