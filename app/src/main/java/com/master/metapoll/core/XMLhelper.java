package com.master.metapoll.core;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Helper functins for dealing with the XML documents. All static, because it's always
 * different documents. It would not make sense to always create an object.
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
public class XMLhelper {
    private static final String TAG = "XMLhelper";

    /**
     * Searches in the given file for ressources that have to be downloaded additionally to the
     * metadatapackage itself. This could be a photo or audio or other binaries.
     * It basically searches for a <ressources> tag and downloads all the files given in it's attributes.
     *
     * @param filename path to XML file
     * @return List of all files that have to be downloaded.
     */
    public static ArrayList<String> parseForRessources(String filename) {
        ArrayList<String> result = new ArrayList<>();
        try {
            NodeList nodes = getNodeList(filename,"pollDocument");
            for (int i = 0; i < nodes.getLength(); i++) {
                result.addAll(findUrls(nodes.item(i)));
            }
            for (String s : result) {
                Log.i(TAG, "ENtry: " + s);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * Opens afile on the given path and returns a list of nodes (and subnodes) wit the given tag name.
     * This enables the caller just to get a part of the whole document tree.
     * @param filepath path to xml file
     * @param tagName tag the node list should start from.
     * @return List of found nodes.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static NodeList getNodeList(String filepath, String tagName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();


        File file = new File(filepath);
        if (!file.exists()) {
            Log.e(TAG,"could not open file: " + filepath);
            return null;
        }


        Document doc = db.parse(file);

        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName(tagName);
    }

    /**
     * The UI can obtain a list of all available Poll Names. This function finds the corresponding
     * name to ga given file by parsing it's name tag
     * @param filename file you want the poll name from.
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static String getPollNameFromFile(String filename) throws IOException, SAXException, ParserConfigurationException {
        NodeList nodes = getNodeList(filename, "pollDocument");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node nNode = nodes.item(i);
            if (nNode.getNodeName().equalsIgnoreCase("pollDocument")) {
                return getNodeValue(nNode, "name");
            }
        }
        return null;
    }

    /**
     * Parses the list obtained from the web for the available polls and creates a map, containig
     * the name and the download url.
     * @param filePath path to the XML document containing the list
     * @return a Map containing name, download link pairs
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static HashMap<String,String> parseMetaList(String filePath) throws IOException, SAXException, ParserConfigurationException {
        HashMap<String,String> result = new HashMap<>();
        NodeList nodes = null;

            nodes = getNodeList(filePath, "availableMetadataPackages");
            if (nodes == null) {
                Log.e(TAG, "Could not get nodeList from file. Stop.");
                return result;
            }

        for (int i = 0; i < nodes.getLength(); i++) {
            Node nNode = nodes.item(i);

            int type = nNode.getNodeType();
            Log.i(TAG, "Node Name: " + nNode.getNodeName() + " \nnodes.geLength: " + nodes.getLength());

            switch (type) {
                //Element
                case Node.ELEMENT_NODE:

                    if (nNode.getNodeName().equalsIgnoreCase("availableMetadataPackages")) {
                        // get the pages, means the children
                        for (Node child = nNode.getFirstChild(); child != null;
                             child = child.getNextSibling()) {
                            if (child.getNodeName().equalsIgnoreCase("package")) {

                                result.put(getNodeValue(child,"name"),getNodeValue(child,"url"));
                                Log.i(TAG, "got child. Name: " + getNodeValue(child, "name") + " url: " + getNodeValue(child, "url"));
                                Log.i(TAG, "got child. Name: " + getNodeValue(child, "name") + " url: " + result.get(getNodeValue(child, "name")));
                            }
                        }
                        break;
                    }
            }
        }
        return result;
    }

    /**
     * Sarches for ressources that have to be downloaded and creates a list with all download links.
     * @param node to check for download links
     * @return list with all found download links
     */
    private static ArrayList<String> findUrls(Node node) {


        int type = node.getNodeType();
        ArrayList<String> result = new ArrayList<>();
        String temp = null;

        switch (type) {
            //Element
            case Node.ELEMENT_NODE:
                //TODO: This method is stupid. It just looks for
                // attributes named ressourcepath. What if we have to download several ressources?
                temp = getNodeValue(node,"ressourcepath");
                if (temp != null) {
                    result.add(temp);
                    Log.i(TAG,"added path: " +temp);
                }

                // get the pages, means the children
                for (Node child = node.getFirstChild(); child != null;
                     child = child.getNextSibling()) {
                    ArrayList<String> childres = findUrls(child);
                    if (childres.size() > 0) {
                        result.addAll(childres);
                    }
                }
                break;
        }
        return  result;
    }

    /**
     * Returns the value of a given node attribute as string.
     * In case of failure (attribute does not exist) it retunrs null
     * @param node Node containing the attribute in question
     * @param nodeName name of the attribute
     * @return
     */
    public static String getNodeValue(Node node, String nodeName) {
        Node item;
        if (node == null) {
            Log.e(TAG,"got empty node. Nothing to do.");
            return null;
        }
        NamedNodeMap attribs = node.getAttributes();

        if (attribs == null) {
            Log.e(TAG,"got node without attributes. Name: " + node.getNodeName());
            return null;
        }
        for (int i = 0; i< attribs.getLength();i++) {
            item = attribs.item(i);
            if(item.getNodeName().equalsIgnoreCase(nodeName)) {
                return item.getNodeValue();
            }
        }
        Log.e(TAG, "Nodename " + nodeName + " not found!");
        return null;
    }

    /**
     * Returns the value of a given node attribute as integer.
     * In case of failure (attribute does not exist or is not a number)
     * it retunrs null.
     * @param node Node containing the attribute in question
     * @param nodeName name of the attribute
     * @return
     */
    public static int getNodeValueInt(Node node, String nodeName) {
        NamedNodeMap attribs = node.getAttributes();
        Node item;
        for (int i = 0; i< attribs.getLength();i++) {
            item = attribs.item(i);
            if(item.getNodeName().equalsIgnoreCase(nodeName)) {
                return Integer.parseInt(item.getNodeValue());
            }
        }
        Log.e(TAG, "Nodename " + nodeName + " not found!");
        return -1;
    }

    /**
     * Print a node. Just for debugging purposes.
     * @param n Node to print
     */
    public static void echo(Node n) {
        int type = n.getNodeType();

        switch (type) {
            case Node.ATTRIBUTE_NODE:
                Log.i(TAG, "ATTR:");
                printlnCommon(n);
                break;

            case Node.CDATA_SECTION_NODE:
                Log.i(TAG, "CDATA:");
                printlnCommon(n);
                break;

            case Node.COMMENT_NODE:
                Log.i(TAG, "COMM:");
                printlnCommon(n);
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
                Log.i(TAG, "DOC_FRAG:");
                printlnCommon(n);
                break;

            case Node.DOCUMENT_NODE:
                Log.i(TAG, "DOC:");
                printlnCommon(n);
                break;

            case Node.DOCUMENT_TYPE_NODE:
                Log.i(TAG, "DOC_TYPE:");
                printlnCommon(n);
                NamedNodeMap nodeMap = ((DocumentType)n).getEntities();
//                indent += 2;
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    Entity entity = (Entity)nodeMap.item(i);
                    echo(entity);
                }
//                indent -= 2;
                break;

            case Node.ELEMENT_NODE:
                Log.i(TAG, "ELEM:");
                printlnCommon(n);

                NamedNodeMap atts = n.getAttributes();
//                indent += 2;
                for (int i = 0; i < atts.getLength(); i++) {
                    Node att = atts.item(i);
                    echo(att);
                }
//                indent -= 2;
                break;

            case Node.ENTITY_NODE:
                Log.i(TAG, "ENT:");
                printlnCommon(n);
                break;

            case Node.ENTITY_REFERENCE_NODE:
                Log.i(TAG, "ENT_REF:");
                printlnCommon(n);
                break;

            case Node.NOTATION_NODE:
                Log.i(TAG, "NOTATION:");
                printlnCommon(n);
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                Log.i(TAG, "PROC_INST:");
                printlnCommon(n);
                break;

            case Node.TEXT_NODE:
                Log.i(TAG, "TEXT:");
                printlnCommon(n);
                break;

            default:
                Log.i(TAG, "UNSUPPORTED NODE: " + type);
                printlnCommon(n);
                break;
        }

//        indent++;
        for (Node child = n.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            echo(child);
        }
//        indent--;
    }

    /**
     * Helper for echoNode(n) Just for debugging.
     * @param n node
     */
    public static void printlnCommon(Node n) {
        Log.i(TAG, " nodeName=\"" + n.getNodeName() + "\"");

        String val = n.getNamespaceURI();
        if (val != null) {
            Log.i(TAG, " uri=\"" + val + "\"");
        }

        val = n.getPrefix();

        if (val != null) {
            Log.i(TAG, " pre=\"" + val + "\"");
        }

        val = n.getLocalName();
        if (val != null) {
            Log.i(TAG, " local=\"" + val + "\"");
        }

        val = n.getNodeValue();
        if (val != null) {
            Log.i(TAG, " nodeValue=");
            if (val.trim().equals("")) {
                // Whitespace
                Log.i(TAG, "[WS]");
            }
            else {
                Log.i(TAG, "\"" + n.getNodeValue() + "\"");
            }
        }
//        Log.i(TAG, "\n");
    }


}
