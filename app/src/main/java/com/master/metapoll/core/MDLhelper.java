package com.master.metapoll.core;

/**
 * Created by ich on 24.11.15.
 */

import android.content.Context;
import android.util.Log;

import com.master.metapoll.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Helper Class for dealing with the XML documents.
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
class MDLhelper {
    private static final String TAG = "UrlExtractor";
    private Document xmlDocument;
    /**
     * Holds the name of the specific poll.
     */
    private String name = "generic";
    /**
     * Holds the id of the specific poll.
     */
    private int id = -1;

    private Context mContext;


    /**
     * Constructor
     *
     * @param filepath Path to file metadata list to work with.
     */
    public MDLhelper(String filepath, Context mContext) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        this.mContext = mContext;
        File file = new File(filepath);
        if (!file.exists()) {
            Log.e(TAG, "could not open file: " + filepath);
            return;
        } else {
            //parse XML Document.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            xmlDocument = db.parse(file);
            xmlDocument.getDocumentElement().normalize();
        }
    }

    /************************************
     * public methods
     ****************************************/

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Searches in the given file for ressources that have to be downloaded additionally to the
     * metadatapackage itself. This could be a photo or audio or other binaries.
     * It basically searches for a <ressources> tag and downloads all the files given in it's attributes.
     *
     * @return List of all files that have to be downloaded.
     */
    public ArrayList<String> parseForRessources() {
        ArrayList<String> result = new ArrayList<>();

        NodeList nodes = xmlDocument.getElementsByTagName("ressources");
        for (int i = 0; i < nodes.getLength(); i++) {
            Log.i(TAG, "found ressource node. finding urls: " + nodes.item(i).getNodeName());
            result.addAll(findUrls(nodes.item(i)));
        }
        for (String s : result) {
            Log.i(TAG, "ENtry: " + s);
        }


        return result;

    }

    /**
     * Sarches for ressources that have to be downloaded and creates a list with all download links.
     * @param node to check for download links
     * @return list with all found download links
     */
    private ArrayList<String> findUrls(Node node) {


        int type = node.getNodeType();
        ArrayList<String> result = new ArrayList<>();
        String temp = null;
        NamedNodeMap atts = node.getAttributes();
        Log.i(TAG, "parsing for ressources.  node: " + node.getNodeName() + " value: " + node.getNodeValue() + " atts length: " + atts.getLength() + "type: " + type);
        switch (type) {
//            //Element
            case Node.ELEMENT_NODE:
                //TODO: This method is stupid. It just looks for
                // attributes named ressourcepath. What if we have to download several ressources?


                for (int j=0; j < atts.getLength();j++) {
                    Log.i(TAG, "atts: " + atts.item(j).getNodeName() + " value: " +  atts.item(j).getNodeValue());
                    temp = atts.item(j).getNodeValue();

                    if (temp != null) {
                        result.add(temp);
                        Log.i(TAG,"added path: " +temp);
                    }

                    Log.i(TAG, "parent node:" + node.getParentNode().getNodeName());
                    Element parent = (Element) node.getParentNode();
                    parent.setAttribute("TESTITEST","dllink");

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


    /************************************
     * private functions*************************************
     * <p/>
     * <p/>
     * /**
     * The UI can obtain a list of all available Poll Names. This function finds the corresponding
     * name to a given file by parsing it's name tag
     *
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public String getPollNameFromFile(String filepath) throws IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        File file = new File(filepath);
        Document xmlFile = null;


        try {
            xmlFile = db.parse(file);
        } catch (SAXException e) {
            //This happens, if we find a file which is not valid xml.
            Log.e(TAG,"parse error. File is no valid xml file.");
            return null;
        }
        xmlFile.getDocumentElement().normalize();
        NodeList nodes = xmlFile.getElementsByTagName(mContext.getString(R.string.tag_root_polldoc));

        for (int i = 0; i < nodes.getLength(); i++) {
            Node nNode = nodes.item(i);
            if (nNode.getNodeName().equalsIgnoreCase(mContext.getString(R.string.tag_root_polldoc))) {
                return getNodeValue(nNode, "name");
            }
        }
        return null;
    }

    /**
     * Parses the list obtained from the web for the available polls and creates a map, containig
     * the name and the download url.
     *
     * @return a Map containing name, download link pairs
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public HashMap<String, String> getPollsFromWebList() throws IOException, SAXException, ParserConfigurationException {
        HashMap<String, String> result = new HashMap<>();
        NodeList nodes;
        try {
            nodes = xmlDocument.getElementsByTagName(mContext.getString(R.string.tag_root_metalist));
        } catch (Exception e) {
            Log.e(TAG, "Could not get nodeList. Stop.");
            e.printStackTrace();
            return result;
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            Node nNode = nodes.item(i);

            int type = nNode.getNodeType();
            Log.i(TAG, "Node Name: " + nNode.getNodeName() + " \nnodes.geLength: " + nodes.getLength());

            switch (type) {
                //Element
                case Node.ELEMENT_NODE:

                    if (nNode.getNodeName().equalsIgnoreCase(mContext.getString(R.string.tag_root_metalist))) {
                        // get the pages, means the children
                        for (Node child = nNode.getFirstChild(); child != null;
                             child = child.getNextSibling()) {
                            if (child.getNodeName().equalsIgnoreCase(mContext.getString(R.string.tag_package_metalist))) {

                                result.put(getNodeValue(child, "name"), getNodeValue(child, "url"));
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
     * Returns the value of a given node attribute as string.
     * In case of failure (attribute does not exist) it retunrs null
     *
     * @param node     Node containing the attribute in question
     * @param nodeName name of the attribute
     * @return
     */
    public String getNodeValue(Node node, String nodeName) {
        Node item;
        if (node == null) {
            Log.e(TAG, "got empty node. Nothing to do.");
            return null;
        }
        NamedNodeMap attribs = node.getAttributes();

        if (attribs == null) {
            Log.e(TAG, "got node without attributes. Name: " + node.getNodeName());
            return null;
        }
        for (int i = 0; i < attribs.getLength(); i++) {
            item = attribs.item(i);
            if (item.getNodeName().equalsIgnoreCase(nodeName)) {
                return item.getNodeValue();
            }
        }
        Log.e(TAG, "Nodename " + nodeName + " not found!");
        return null;
    }
}
