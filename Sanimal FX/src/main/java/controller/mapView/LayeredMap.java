package controller.mapView;

import fxmapcontrol.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Special map that keeps track of map layers
 */
public class LayeredMap extends Map
{
	// A cache of each node's Z order field
	private java.util.Map<Node, Integer> zOrder;
	// A sorted list which will be sorted by z-order
	private ObservableList<Node> sortedNodes;
	// A cache to a listener to avoid early garbage collection. This is a bit of a hack...
	private Subscription subscriptionCache;

	/**
	 * The constructor for a layered map just sets up fields
	 */
	public LayeredMap()
	{
		super();

		// Initialize our z-order cache
		this.zOrder = new HashMap<>();
		// Let the base list be our custom observable list, we add to this list not map.getChildren()
		this.sortedNodes = FXCollections.observableArrayList();
		// Sort this custom list of nodes by the z-order, and then assign it to the map's children field. Now any nodes added to the sorted nodes list
		// will be automatically added to the map's children list in sorted order. Store a useless reference to the subscription otherwise it will be
		// garbage collected early
		this.subscriptionCache = EasyBind.listBind(this.getChildren(), new SortedList<>(this.sortedNodes, Comparator.comparing(node -> zOrder.getOrDefault(node, -1))));
	}

	/**
	 * Adds a node into the map using the binding we created earlier. First we add
	 * to our z-order map and then insert into our sorted nodes list which ensures that
	 * the z-order is properly applied
	 *
	 * @param node The node to add to the list
	 * @param zOrder The z-order to assign to the node
	 */
	public void addChild(Node node, MapLayers zOrder)
	{
		this.zOrder.put(node, zOrder.getZLayer());
		this.sortedNodes.add(node);
	}

	/**
	 * Removes the node from the map as well as the z-order hashmap
	 *
	 * @param node The node to remove
	 */
	public void removeChild(Node node)
	{
		this.sortedNodes.remove(node);
		this.zOrder.remove(node);
	}
}
