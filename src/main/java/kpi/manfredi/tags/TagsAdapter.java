package kpi.manfredi.tags;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * This class is used to provide methods to represent tags as {@code CheckTreeView}
 */
public abstract class TagsAdapter {

    /**
     * This method is used to represent {@code TagsStorage} in the form of a {@code CheckTreeView}
     * by creating an appropriate hierarchy of tree items
     *
     * @param tagsStorage instance that contains categories and tags
     * @return {@code CheckTreeView} root item
     */
    public static CheckBoxTreeItem<Object> getRootItem(TagsStorage tagsStorage) {
        CheckBoxTreeItem<Object> root = new CheckBoxTreeItem<>("Root");
        ObservableList<TreeItem<Object>> childItems = root.getChildren();
        for (Category category : tagsStorage.getCategory()) {
            childItems.add(handleCategory(category));
        }
        return root;
    }

    /**
     * This method is used to recursively bypass the hierarchy of each category
     * to represent it as {@code CheckBoxTreeItem} elements
     *
     * @param category the category that will be processed
     * @return {@code CheckBoxTreeItem} element that represent the {@code category}
     */
    private static CheckBoxTreeItem<Object> handleCategory(Category category) {
        CheckBoxTreeItem<Object> categoryTreeItem = new CheckBoxTreeItem<>(category);
        categoryTreeItem.setIndependent(true);
        ObservableList<TreeItem<Object>> childItems = categoryTreeItem.getChildren();

        for (Category childCategory : category.getCategory()) {
            childItems.add(handleCategory(childCategory));
        }

        childItems.addAll(handleTags(category.getTag()));

        return categoryTreeItem;
    }

    /**
     * This method is used to to represent tags as {@code CheckBoxTreeItem} elements
     *
     * @param tags list of items
     * @return list of tree items
     */
    private static ObservableList<TreeItem<Object>> handleTags(List<String> tags) {
        ObservableList<TreeItem<Object>> treeItems = FXCollections.observableArrayList();
        for (String tag : tags) {
            treeItems.add(new CheckBoxTreeItem<>(tag, null, false, true));
        }
        return treeItems;
    }

}
