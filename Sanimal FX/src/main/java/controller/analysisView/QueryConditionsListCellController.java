package controller.analysisView;

import controller.analysisView.conditions.EndDateConditionController;
import controller.analysisView.conditions.SpeciesFilterConditionController;
import controller.analysisView.conditions.StartDateConditionController;
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
			{
				contentPane.setCenter(FXMLLoaderUtils.loadFXML("analysisView/conditions/AddQueryCondition.fxml").getRoot());
				this.btnRemoveCondition.setVisible(false);
			}
			else
			{
				this.btnRemoveCondition.setVisible(true);
			}

			if (queryCondition instanceof SpeciesFilterCondition)
				contentPane.setCenter(FXMLLoaderUtils.loadFXML("analysisView/conditions/SpeciesFilterCondition.fxml").getRoot());
			else if (queryCondition instanceof LocationFilterCondition)
				contentPane.setCenter(FXMLLoaderUtils.loadFXML("analysisView/conditions/LocationFilterCondition.fxml").getRoot());
			else if (queryCondition instanceof StartDateCondition)
			{
				FXMLLoader fxml = FXMLLoaderUtils.loadFXML("analysisView/conditions/StartDateCondition.fxml");
				fxml.<StartDateConditionController> getController().initializeData((StartDateCondition) queryCondition);
				contentPane.setCenter(fxml.getRoot());
			}
			else if (queryCondition instanceof EndDateCondition)
			{
				FXMLLoader fxml = FXMLLoaderUtils.loadFXML("analysisView/conditions/EndDateCondition.fxml");
				fxml.<EndDateConditionController> getController().initializeData((EndDateCondition) queryCondition);
				contentPane.setCenter(fxml.getRoot());
			}
			this.setGraphic(mainPane);
		}
	}

	public void clearCondition(ActionEvent actionEvent)
	{
		this.getListView().getItems().remove(this.getItem());
	}
}
