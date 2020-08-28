package kpi.manfredi.tags;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import kpi.manfredi.tags.map.TagsMap;
import kpi.manfredi.tags.map.Tag;
import kpi.manfredi.tags.tree.Category;
import kpi.manfredi.tags.tree.TagsTree;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to provide methods to represent tags as {@code CheckTreeView}
 */
public abstract class TagsAdapter {

    /**
     * This method is used to represent {@code TagsStorage} in the form of a {@code CheckTreeView}
     * by creating an appropriate hierarchy of tree items
     *
     * @param tagsTree instance that contains categories and tags
     * @return {@code CheckTreeView} root item
     */
    public static CheckBoxTreeItem<Object> getRootItem(TagsTree tagsTree) {
        CheckBoxTreeItem<Object> root = new CheckBoxTreeItem<>("Root");
        ObservableList<TreeItem<Object>> childItems = root.getChildren();
        for (Category category : tagsTree.getCategory()) {
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

    /**
     * This method is used to adapt list of tags to {@code Mapper} structure and
     * create the basis of the mapping.
     *
     * <br><br>
     * Example:
     * <br><br>
     * Input: {#tag1 #tag2}
     * <br><br>
     * Output: Mapper { Tag {#tag1 , {tag1}}, Tag {#tag2 , {tag2}}}
     *
     * @param tags list of tags
     * @return {@code Mapper} instance
     */
    public static TagsMap getMapper(List<String> tags) {
        TagsMap tagsMap = new TagsMap();
        for (String tagStr : tags) {
            Tag tag = new Tag();
            tag.setName(tagStr);
            tag.getAlias().add(tagStr.substring(1));
            tagsMap.getTag().add(tag);
        }
        return tagsMap;
    }

    /**
     * This method is used to adapt {@code Mapper} to map of aliases and tags
     *
     * @param tagsMap {@code Mapper} instance
     * @return map of aliases and tags
     */
    public static HashMap<String, String> getTagsMap(TagsMap tagsMap) {
        HashMap<String, String> map = new HashMap<>();
        for (Tag tag : tagsMap.getTag()) {
            for (String alias : tag.getAlias()) {
                map.put(alias, tag.getName());
            }
        }
        return map;
    }

}
