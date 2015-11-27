package com.master.metapoll.mdo;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import org.w3c.dom.Node;

/**
 * Creates a horizontal line. Helps the user to see through.
 * Subclass of MDobject. Check {@link MDobject} for further information.
 *
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
class MDOseparator extends MDobject {
    private static final String xmlName = "separator";

    // node is not yet used, but maybe in future, to manipulate the separator.
    public MDOseparator(Context context,Node node, int pageNo) {
        super(context,node,pageNo);
    }
    @Override
    public View getView() {
        View separator = new View(context);

        separator.setLayoutParams(new LinearLayout.LayoutParams(-1,1));
        separator.setBackgroundColor(Color.parseColor("#666645"));
        return separator;
    }

    public static String getXmlName() {
        return xmlName;
    }

    @Override
    public void save() {
        //nothing to save
    }

    /**
     *
     * Nothing to clear here.
     */
    @Override
    protected void clear() {

    }

    /**
     * Separator is not being saved
     * @param values
     * @return same as came in.
     */
    @Override
    public ContentValues db_save(ContentValues values) {
        return values;
    }

    /**
     * Is not being saved, so empty db String
     * @return
     */
    @Override
    public String getDbString() {
        return "";
    }
}
