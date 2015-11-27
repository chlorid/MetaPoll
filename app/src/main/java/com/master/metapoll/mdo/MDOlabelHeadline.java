package com.master.metapoll.mdo;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Node;

/**
 * Creates a headline for the corresponding page.
 * Subclass of MDOlabel, which is a subclass of MDobject.
 * Check {@link MDobject} for further information.
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
class MDOlabelHeadline extends MDOlabel {
    private static final String xmlName = "headlineElement";


    public MDOlabelHeadline(Context context, Node node, int pageNo) {
        super(context,node,pageNo);
        this.mInstruction = getNodeValue(node, "headline");
        Log.i(TAG, "constructor if instrucion elment called");
    }

    public static String getXmlName() {
        return xmlName;
    }
}
