package com.master.metapoll.core;

import android.util.Log;

import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Created by ich on 24.11.15.
 */
public class Debugging {
    private static final String TAG = "Debugging";

    public static void echo(Node n) {
        /**
         * Print a node. Just for debugging purposes.
         * @param n Node to print
         */
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
                    NamedNodeMap nodeMap = ((DocumentType) n).getEntities();
//                indent += 2;
                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        Entity entity = (Entity) nodeMap.item(i);
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
