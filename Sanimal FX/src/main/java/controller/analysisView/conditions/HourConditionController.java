package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import model.query.IQueryCondition;
import model.query.conditions.HourCondition;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.time.Month;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HourConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public ListView<Integer> hourFilterListView;

	///
	/// FXML Bound Fields End
	///

	private HourCondition hourCondition;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		if (iQueryCondition instanceof HourCondition)
		{
			this.hourCondition = (HourCondition) iQueryCondition;
			this.hourFilterListView.setItems(this.hourCondition.getHourList());
			this.hourFilterListView.setCellFactory(CheckBoxListCell.forListView(this.hourCondition::hourSelectedProperty));
			this.hourFilterListView.setEditable(true);
		}
	}

	public void selectAllHours(ActionEvent actionEvent)
	{
		if (this.hourCondition != null)
			this.hourCondition.selectAll();
		actionEvent.consume();
	}

	public void selectNoHours(ActionEvent actionEvent)
	{
		if (this.hourCondition != null)
			this.hourCondition.selectNone();
		actionEvent.consume();
	}
}
