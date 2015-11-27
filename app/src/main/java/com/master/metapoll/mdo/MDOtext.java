package com.master.metapoll.mdo;

import android.content.ContentValues;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.w3c.dom.Node;


/**
 * A Text input object.
 * Subclass of MDobject. Check {@link MDobject} for further information.
 *
 * Supports the following attributes:
 *
 * preset - presets a text to the text input field
 * hint - presets a hint to the text input field
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
class MDOtext extends MDobject {
    private static final String TAG = "MDOtext";
    private static final String xmlName = "textElement";
    /**
     * The entered text
     */
    private String text;
    /**
     * The preset text
     */
    private String presetText;
    /**
     * The hint
     */
    private String hint;
    /**
     * The Textfield
     */
    private EditText editText;

    public MDOtext(Context context, Node node, int pageNo) {
        super(context,node,pageNo );
        this.presetText = getNodeValue(node, "preset");
        this.hint = getNodeValue(node,"hint");
    }


    public static String getXmlName() {
        return xmlName;
    }


    @Override
    public View getView() {
        if (attributeSet == null) {
            editText = new EditText(context, attributeSet);
        }
        else {
            editText = new EditText(context);
        }
        Log.i(TAG, "***************++getView: " + text);
        editText.setText(presetText);
        editText.setHint(hint);
        return editText;
    }

    /**
     * Saves the contents of the text input field back into the variable, in case the view is
     * destroyed;
     */
    @Override
    public void save() {
        text = editText.getText().toString();
        Log.i(TAG, "saving: " + text);
    }

    /**
     * Clears or sets the TextView to the pre-set text and clears the variable
     * that holds the entered text.
     */
    @Override
    protected void clear() {
        text = "";
        editText.setText(presetText);
        editText.setHint(hint);
    }

    /**
     * Adds the text from the variable into the attribute set. The attribute set goes through all
     * objects off the page this object belongs to.
     * @param values Holds the values of all MDobjects in the same poll.
     * @return
     */
    @Override
    public ContentValues db_save(ContentValues values) {

        values.put(MDpoll.createDbName(name), text);
        clear();
        return values;
    }

    @Override
    public String getDbString() {
        return MDpoll.createDbName(name) + " TEXT,";
    }
}
