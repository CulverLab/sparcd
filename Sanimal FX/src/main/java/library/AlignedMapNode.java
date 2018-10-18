package library;

import fxmapcontrol.MapNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

/**
 * Utility class used to align a map node on the map on its given latitude and longitude coordinate
 */
public class AlignedMapNode extends MapNode
{
	private ObjectProperty<Pos> alignment = new SimpleObjectProperty<>(Pos.CENTER);

	/**
	 * Constructor does nothing
	 */
	public AlignedMapNode()
	{
		super();
	}

	/**
	 * Constructor sets the alignment property
	 *
	 * @param alignment The alignment to use for this node
	 */
	public AlignedMapNode(Pos alignment)
	{
		this();
		this.setAlignment(alignment);
	}

	/**
	 * This function is called whenever the viewport is changed
	 *
	 * @param viewportPosition The new viewport position
	 */
	@Override
	protected void viewportPositionChanged(Point2D viewportPosition)
	{
		// The body of this function is mostly copied except for the content of this if statement
		if (viewportPosition != null)
		{
			// Here we grab the width and height of our component as a bounds object
			Bounds boundsInParent = this.getBoundsInParent();

			switch (this.getAlignment())
			{
				case CENTER:
					// Instead of just translating by X and Y, we translate by x - width/2 and y - width/2 which ensures our marker is centered
					setTranslateX(viewportPosition.getX() - boundsInParent.getWidth() / 2);
					setTranslateY(viewportPosition.getY() - boundsInParent.getHeight() / 2);
					break;
				case TOP_CENTER:
					// Align the node on the top and centered
					setTranslateX(viewportPosition.getX() - boundsInParent.getWidth() / 2);
					setTranslateY(viewportPosition.getY() - boundsInParent.getHeight());
					break;
				case BOTTOM_RIGHT:
					// Align the node on the bottom and right
					setTranslateX(viewportPosition.getX());
					setTranslateY(viewportPosition.getY());
					break;
				default:
					throw new UnsupportedOperationException("Alignment " + this.getAlignment().toString() + " not yet supported by aligned map node");
			}
		}
		else
		{
			// Same as the base function
			setTranslateX(0d);
			setTranslateY(0d);
		}
	}

	public void setAlignment(Pos alignment)
	{
		this.alignment.setValue(alignment);
	}

	public Pos getAlignment()
	{
		return this.alignment.getValue();
	}

	public ObjectProperty<Pos> alignmentProperty()
	{
		return this.alignment;
	}
}