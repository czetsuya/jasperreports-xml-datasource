package com.czetsuyatech;

import java.net.URL;
import java.net.URLClassLoader;

public class Application {

  public static void main(String[] args) {

    InvoiceReportGenerator rg = new InvoiceReportGenerator(
        "C:\\Projects\\Lab\\jasperreports-xml-datasource\\src\\main\\resources",
        new URLClassLoader(
            new URL[]{InvoiceReportGenerator.class.getClassLoader().getResource("opencell-fonts.jar")})
    );

    try {
      rg.generate("invoice.jasper", "INV00000027.xml");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
