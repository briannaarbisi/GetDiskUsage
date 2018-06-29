package com.company;

import java.lang.System;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.StringBuilder;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Main {

    public static void main(String[] args) {
        Document doc;
        File directory = new File(args[0]);

        if (directory.length() == 0) {
            System.out.println("No files in the mount point.");
            System.exit(9);
        }

        String os = (System.getProperty("os.name").split(" ")[0]).toLowerCase();
        char dirSeparator = ' ';

        if (new String("linux").equals(os)) {
            dirSeparator = '/';
        } else if ((new String("mac").equals(os)) || (new String("darwin").equals(os))) {
            dirSeparator = '/';
        } else if (new String("windows").equals(os)) {
            dirSeparator = '\\';
        } else {
            //invalid format
            System.out.println("Error: getdiskusage was not designed to run on " + os);
        }

        String directoryName = directory.getName();
        String fileName;

        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();
            Element rootElement = doc.createElement(directoryName);
            doc.appendChild(rootElement);

            for (File file : directory.listFiles()) {
                if (file.isFile()) {

                    int bytes = ((int) file.length());
                    fileName = file.getName();

                    //  be careful of \ vs /
                    StringBuilder builder = new StringBuilder();
                    builder.append(directoryName).append(dirSeparator).append(fileName);
                    String result = builder.toString();

                    System.out.println(result);

                    Element item = doc.createElement("files");
                    Attr path = doc.createAttribute("path");
                    path.setValue(result);
                    item.setAttributeNode(path);
                    Attr size = doc.createAttribute("bytes");
                    size.setValue(String.valueOf(bytes));
                    item.setAttributeNode(size);
                    rootElement.appendChild(item);

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();

                    File xmlfile = new File(args[0] + dirSeparator + "garbage.xml");

                    DOMSource source = new DOMSource(doc);
                    StreamResult result2 = new StreamResult(xmlfile);
                    transformer.transform(source, result2);

                    System.out.println("File saved!");

                    //XMLToJSON
                    FileReader xmlreader = new FileReader(xmlfile);
                    BufferedReader xmlbuffer = new BufferedReader(xmlreader);
                    String xmlstringout = "";
                    String xmlstring;

                    while ((xmlstring = xmlbuffer.readLine()) != null) {
                        xmlstringout = xmlstringout.concat(xmlstring);
                    }
                    System.out.println("XML buffered");


                    JSONObject xmlJSONObj = XML.toJSONObject(xmlstringout);
                    String jsonFormatted = xmlJSONObj.toString(3);
                    System.out.println(jsonFormatted);

                    File jsonfile = new File(args[0] + dirSeparator + "json.txt");
                    FileWriter jsonwriter = new FileWriter(jsonfile);
                    jsonwriter.write(jsonFormatted);
                    jsonwriter.flush();
                    jsonwriter.close();

                    xmlreader.close();
                    xmlbuffer.close();
                    xmlfile.deleteOnExit();

                }
            }

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }catch (Exception e) {
            System.exit(1);
        }

    }

    static JSONArray printNote_1(NodeList nodeList) {
        String temp = "";
        try {
            JSONArray dataArr = new JSONArray();
            JSONObject dataObject = new JSONObject();
            for (int count = 0; count < nodeList.getLength(); count++) {
                Node tempNode = nodeList.item(count);
                if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                    if (tempNode.hasChildNodes() && tempNode.getChildNodes().getLength() > 1) {
                        JSONArray temArr = printNote_1(tempNode.getChildNodes());
                        temp = tempNode.getNodeName();
                        if (dataObject.getBoolean(temp)) {
                            dataObject.getJSONArray(tempNode.getNodeName()).put(temArr.getJSONObject(0));
                        } else {
                            dataObject.put(tempNode.getNodeName(), temArr);
                        }
                    } else {
                        dataObject.put(tempNode.getNodeName(), tempNode.getTextContent());
                    }
                }
            }
            dataArr.put(dataObject);
            return dataArr;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}



