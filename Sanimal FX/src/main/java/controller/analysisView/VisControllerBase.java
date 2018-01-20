package controller.analysisView;

import javafx.fxml.Initializable;
import model.image.ImageEntry;

import java.util.List;

public interface VisControllerBase extends Initializable
{
	void visualize(List<ImageEntry> images, Integer eventInterval);
}
