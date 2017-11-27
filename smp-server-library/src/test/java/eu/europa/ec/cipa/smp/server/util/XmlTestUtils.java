/*
 * Copyright 2017 European Commission | CEF eDelivery
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 * or file: LICENCE-EUPL-v1.1.pdf
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.cipa.smp.server.util;

import eu.europa.ec.cipa.smp.server.security.SignatureUtil;
import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceGroup;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Created by gutowpa on 05/01/2017.
 */
public class XmlTestUtils {

    private static final String UTF_8 = "UTF-8";

    public static String loadDocumentAsString(String docResourcePath) throws IOException {
        InputStream inputStream = XmlTestUtils.class.getResourceAsStream(docResourcePath);
        return IOUtils.toString(inputStream, UTF_8);
    }

    public static Document loadDocument(String docResourcePath) throws ParserConfigurationException, SAXException, IOException {
        InputStream inputStream = SignatureUtil.class.getResourceAsStream(docResourcePath);
        return getDocumentBuilder().parse(inputStream);
    }

    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder();
    }

    public static String marshal(Node doc) throws TransformerException, UnsupportedEncodingException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        trans.transform(new DOMSource(doc), new StreamResult(stream));
        return stream.toString(UTF_8);
    }

    public static String marshall(ServiceMetadata serviceMetadata) throws JAXBException {
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(ServiceMetadata.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal(serviceMetadata, sw);
        return sw.toString();
    }

    public static String marshall(ServiceGroup serviceGroup) throws JAXBException {
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(ServiceGroup.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal(serviceGroup, sw);
        return sw.toString();
    }
}
