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
 * @author David Kauderer
 * @version 0.1
 *
 */
class MDOtext extends MDobject {
    private static final String TAG = "MDOtext";
    private static final String xmlName = "textElement";
    private String mText;
    private String mPresetText;

    private AttributeSet mAttributeSet;
    private EditText mEditText;

    public MDOtext(Context context, Node node, int pageNo) {
        super(context,node,pageNo );
        this.mPresetText = getNodeValue(node, "preset");


    }

    public void setText(String text) {
        this.mText = text;
    }

    public void setAttributeSet(AttributeSet attributeSet) {
        this.mAttributeSet = attributeSet;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String getXmlName() {
        return xmlName;
    }


    @Override
    public View getView() {
        if (mAttributeSet == null) {
            mEditText = new EditText(context, mAttributeSet);
        }
        else {
            mEditText = new EditText(context);
        }
        Log.i(TAG, "***************++getView: " + mText);
        mEditText.setText(mPresetText);
        return mEditText;
    }

    /**
     * Saves the contents of the text input field back into the variable, in case the view is
     * destroyed;
     */
    @Override
    public void save() {
        mText = mEditText.getText().toString();
        Log.i(TAG, "saving: " + mText);
    }

    /**
     * Clears or sets the TextView to the pre-set text and clears the variable
     * that holds the entered text.
     */
    @Override
    protected void clear() {
        mText = "";
        mEditText.setText(mPresetText);
    }

    /**
     * Adds the text from the variable into the attribute set. The attribute set goes through all
     * objects off the page this object belongs to.
     * @param values Holds the values of all MDobjects in the same poll.
     * @return
     */
    @Override
    public ContentValues db_save(ContentValues values) {

        values.put(MDpoll.createDbName(name), mText);
        clear();
        return values;
    }

    @Override
    public String getDbString() {
        return MDpoll.createDbName(name) + " TEXT,";
    }
}
