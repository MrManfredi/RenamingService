package kpi.manfredi.tags;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import kpi.manfredi.utils.FileManipulation;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static kpi.manfredi.utils.DialogsUtil.showAlert;
import static kpi.manfredi.utils.MessageUtil.formatMessage;
import static kpi.manfredi.utils.MessageUtil.getMessage;

/**
 * This class is used to provide methods to read / write tags from/to {@value XML_FILE} file
 */
public abstract class TagsCustodian {
    private static final String SCHEMA_LOCATION = "/tags.xsd";
    private static final String XML_FILE = "tags.xml";

    /**
     * This method is used to parse {@value XML_FILE} file (that contains categories and tags)
     * and convert to object view
     *
     * @return {@code TagsStorage} instance which is a container of categories and tags.
     * @throws FileNotFoundException schema file not found
     */
    public static TagsStorage getTagsStorage() throws FileNotFoundException {
        TagsStorage tagsStorage = null;
        File file = new File(XML_FILE);
        File schemaFile = FileManipulation.getResourceFile(SCHEMA_LOCATION);

        if (file.exists()) {
            try {

                JAXBContext jaxbContext = JAXBContext.newInstance(TagsStorage.class);

                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(schemaFile);
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

}
