package com.master.metapoll.mdo;

import android.content.ContentValues;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Node;


/**
 * Creates a label with text extracted from the given DOM Tree.
 * The text is the value of the instruction attribute.
 * Subclass of MDobject. Check {@link MDobject} for further information.
 *
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
class MDOlabel extends MDobject {
    protected static final String TAG = "MDOlabel";
    private static final String xmlName = "instructionElement";
    private static final String attributeName = "instruction";
    /**
     * Saves the label text
     */
    protected String mInstruction;

    public MDOlabel(Context context, Node node, int pageNo) {
        super(context,node,pageNo);
        this.mInstruction = getNodeValue(node, attributeName);
    }
    // This is called by the class scanner when scanning for elements.
    public static String getXmlName() {
        return xmlName;
    }

    @Override
    public View getView() {
        TextView tv;
        // if we have an attributeSet, use it.
        if (attributeSet == null) {
            tv = new TextView(context);
        }
        else {
            tv = new TextView(context, attributeSet);
        }
        tv.setText(mInstruction);
        return tv;
    }

    /**
     * Nothing to save. This object takes no data from user.
     */
    @Override
    public void save() {
        //Nothing to save.
    }

    /**
     * Nothing to clear. This object shows just a text label.
     */
    @Override
    protected void clear() {

    }

    /**
     *
     * @param values Holds the values of all MDobjects in the same poll.
     * @return same as came in.
     */
    @Override
    public ContentValues db_save(ContentValues values) {
        return values;
    }

    // Instructions are not saved in the database
    @Override
    public String getDbString() {
        return "";
    }
}
