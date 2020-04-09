package kpi.manfredi.tags;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.List;

import static kpi.manfredi.utils.DialogsUtil.showAlert;
import static kpi.manfredi.utils.MessageUtil.formatMessage;
import static kpi.manfredi.utils.MessageUtil.getMessage;

/**
 * This class is used to provide methods to read / write tags from/to {@value XML_FILE} file
 * and represent it as {@code CheckTreeView}
 */
public class TagsHandler {
    private static final String SCHEMA_LOCATION = "src/main/resources/tags.xsd";
    private static final String XML_FILE = "tags.xml";

    /**
     * This method is used to parse {@value XML_FILE} file (that contains categories and tags)
     * and convert to object view
     *
     * @return {@code TagsStorage} instance which is a container of categories and tags
     */
    public static TagsStorage getTagsStorage() {
        TagsStorage tagsStorage = null;
        File file = new File(XML_FILE);
        if (file.exists()) {
            try {

                JAXBContext jaxbContext = JAXBContext.newInstance(TagsStorage.class);

                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new File(SCHEMA_LOCATION));
                jaxbUnmarshaller.setSchema(schema);
                tagsStorage = (TagsStorage) jaxbUnmarshaller.unmarshal(file);

            } catch (JAXBException | SAXException e) {
                handleParsingException(e);
            }
        } else {
            tagsStorage = new TagsStorage();
        }
        return tagsStorage;
    }

    /**
     * This method is used to write categories and tags from {@code TagsStorage} instance to {@value XML_FILE} file
     *
     * @param storage instance that contains categories and tags
     */
    public static void saveTags(TagsStorage storage) {
        try {

            File file = new File(XML_FILE);
            JAXBContext jaxbContext = JAXBContext.newInstance(TagsStorage.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(new File(SCHEMA_LOCATION));
            jaxbMarshaller.setSchema(schema);

            jaxbMarshaller.marshal(storage, file);
            jaxbMarshaller.marshal(storage, System.out);

        } catch (JAXBException | SAXException e) {
            handleParsingException(e);
        }
    }


    /**
     * This method is used to show alert about exception cause
     *
     * @param e exception
     */
    private static void handleParsingException(Exception e) {
        String constraintViolation = e.getCause().getMessage().
                substring(e.getCause().getMessage().indexOf(':') + 2);

        showAlert(
                Alert.AlertType.ERROR,
                getMessage("error.title"),
                formatMessage("tags.validation.error.header", XML_FILE, constraintViolation),
                getMessage("tags.validation.error.content")
        );
    }

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
