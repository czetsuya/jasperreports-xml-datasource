package com.czetsuyatech;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class InvoiceReportGenerator {

  private final String jasperReportsPath;
  private ClassLoader cl;
  private String INVOICE_TAG_NAME = "invoice";

  public InvoiceReportGenerator(String jasperReportsPath, URLClassLoader cl) {

    this.jasperReportsPath = jasperReportsPath;
    this.cl = cl;
  }

  public void generate(String jasperPath, String xmlDataSource)
      throws IOException, ParserConfigurationException, SAXException, TransformerException, JRException {

    File jasperFile = new File(jasperReportsPath, jasperPath);
    String pdfFullFilename = jasperReportsPath + "/output.pdf";
    File invoiceXmlFile = new File(jasperReportsPath, xmlDataSource);
    Map<String, Object> parameters = getParameters();

    InputStream reportTemplate = new FileInputStream(jasperFile);
    Node invoiceNode = getInvoiceNode(invoiceXmlFile);

    JRXmlDataSource dataSource = new JRXmlDataSource(getJasperReportContext(invoiceNode), "/" + INVOICE_TAG_NAME);

    Map<String, JasperReport> jasperReportMap = new HashMap<>();

    String fileKey = jasperFile.getPath() + jasperFile.lastModified();
    JasperReport jasperReport = jasperReportMap.get(fileKey);
    if (jasperReport == null) {
      jasperReport = (JasperReport) JRLoader.loadObject(reportTemplate);
      jasperReportMap.put(fileKey, jasperReport);
    }

    DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
    JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.xpath.executer.factory",
        "net.sf.jasperreports.engine.util.xml.JaxenXPathExecuterFactory");

    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

    JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFullFilename);
  }

  private Node getInvoiceNode(File invoiceXmlFile)
      throws TransformerException, ParserConfigurationException, IOException, SAXException {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    dbf.setNamespaceAware(true);
    Document xmlDocument = db.parse(invoiceXmlFile);
    xmlDocument.getDocumentElement().normalize();
    Node invoiceNode = xmlDocument.getElementsByTagName(INVOICE_TAG_NAME).item(0);
    Transformer trans = TransformerFactory.newInstance().newTransformer();
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    StringWriter writer = new StringWriter();
    trans.transform(new DOMSource(xmlDocument), new StreamResult(writer));

    return invoiceNode;
  }

  private ByteArrayInputStream getJasperReportContext(Node invoiceNode) throws TransformerException {

    return new ByteArrayInputStream(getNodeXmlString(invoiceNode).getBytes(
        StandardCharsets.UTF_8));
  }

  private Map<String, Object> getParameters() {

    return new HashMap<>() {{
      put(JRParameter.REPORT_CLASS_LOADER, cl);
    }};
  }

  protected String getNodeXmlString(Node node) throws TransformerException {

    TransformerFactory transFactory = TransformerFactory.newInstance();
    Transformer transformer = transFactory.newTransformer();
    StringWriter buffer = new StringWriter();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(new DOMSource(node), new StreamResult(buffer));

    return buffer.toString();
  }
}
