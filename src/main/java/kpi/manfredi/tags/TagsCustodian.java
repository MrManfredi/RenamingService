package kpi.manfredi.tags;

import kpi.manfredi.tags.mapper.Mapper;
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

import static kpi.manfredi.utils.MessageUtil.formatMessage;

/**
 * This class is used to provide methods to read / write tags from/to {@value XML_FILE} file
 */
public abstract class TagsCustodian {
    private static final String SCHEMA_LOCATION = "/tags.xsd";
    private static final String MAPPER_SCHEMA_LOCATION = "/tagsMapper.xsd";
    private static final String XML_FILE = "tags.xml";

    /**
     * This method is used to parse {@value XML_FILE} file (that contains categories and tags)
     * and convert to object view
     *
     * @return {@code TagsStorage} instance which is a container of categories and tags.
     * @throws FileNotFoundException schema file not found
     * @throws JAXBException         validation failed
     */
    public static TagsStorage getTags() throws FileNotFoundException, JAXBException {
        TagsStorage tagsStorage;
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
                throw new JAXBException(formatExceptionMessage(e, XML_FILE));
            }
        } else {
            tagsStorage = new TagsStorage();
        }

        return tagsStorage;
    }

    /**
     * This method is used to save tags to XML file
     *
     * @param tags    instance that contains data
     * @param xmlFile name of file to save into
     * @throws FileNotFoundException schema file not found
     */
    public static void saveTags(Object tags, String xmlFile) throws FileNotFoundException, JAXBException {
        try {

            File file = new File(xmlFile);
            File schemaFile = FileManipulation.getResourceFile(getSchemaLocation(tags));

            JAXBContext jaxbContext = JAXBContext.newInstance(tags.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(schemaFile);
            jaxbMarshaller.setSchema(schema);

            jaxbMarshaller.marshal(tags, file);

        } catch (JAXBException | SAXException e) {
            throw new JAXBException(formatExceptionMessage(e, xmlFile));
        }
    }

    /**
     * This method is used to return appropriate schema location
     *
     * @param tags instance that contains data
     * @return schema location
     */
    private static String getSchemaLocation(Object tags) {
        String schemaLocation = null;
        if (tags instanceof TagsStorage) {
            schemaLocation = SCHEMA_LOCATION;
        } else if (tags instanceof Mapper) {
            schemaLocation = MAPPER_SCHEMA_LOCATION;
        }
        return schemaLocation;
    }

    /**
     * This method is used to show alert about exception cause
     *
     * @param e exception
     */
    private static String formatExceptionMessage(Exception e, String xmlFile) {
        String constraintViolation = e.getCause().getMessage().
                substring(e.getCause().getMessage().indexOf(':') + 2);
        return formatMessage("tags.validation.error.header", xmlFile, constraintViolation);
    }

}
