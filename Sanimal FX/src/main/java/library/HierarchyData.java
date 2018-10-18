package library;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

/**
 * Used to mark an object as hierarchical data.
 * This object can then be used as data source for an hierarchical control, like the {@link javafx.scene.control.TreeView}.
 *
 * @author Christian Schudt
 */
public interface HierarchyData<T extends HierarchyData> {
    /**
     * The children collection, which represents the recursive nature of the hierarchy.
     * Each child is again a {@link HierarchyData}.
     *
     * @return A list of children.
     */
    ObservableList<T> getChildren();

    /**
     * Used in grabbing the icon used in the TreeView
     *
     * @return The image to be put as the tree icon
     */
    ObjectProperty<Image> getTreeIconProperty();
}