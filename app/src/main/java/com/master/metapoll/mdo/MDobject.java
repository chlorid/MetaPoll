package com.master.metapoll.mdo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.master.metapoll.R;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * MDobject ist eine abstrakte Klasse. Zu jedem Subelement eines <page>
 * Elementes muss eine korrespondierende Klasseexistieren, die von MDobject erbt.
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
public abstract class MDobject {
    private static final String TAG = "MDobject";
    /**
     * Every object hast to hold it's own page number. That way the GUI knows where to show
     * the element.
     */
    protected int pageNo;
    /**
     * The name of the element. It's parsed from XML. (In the subclass(es))
     */
    protected String name;
    /**
     * Android specific. needed to generate the Views for the UI.
     */
    protected Context context;
    /**
     * ID of the element. It's parsed from XML. (In the subclass(es))
     */
    protected int id;
    /**
     * The attribute set can be used to manipulate the style prefernces of a view.
     * This might be useful if the poll should get a different look, than standard.
     */
    protected AttributeSet attributeSet;

    private static final String xmlName = null;

    /**
     * Constructor. Every Subclass has to call this constructor to initialize the common values.
     * @param context The applications context
     * @param pageNo page number this object belongs to
     * @param node xml node to extract additional information.
     */
    protected MDobject(Context context,Node node, int pageNo) {
        this.context = context;
        this.pageNo = pageNo;
        this.id = getNodeValueInt(node, "id");
        this.name = getNodeValue(node, "name");
        NodeList children =  node.getChildNodes();

        // retrieve attribute set.
        for (int i = 0; i < children.getLength(); i++) {
            Node nNode = children.item(i);
            if (nNode.getNodeName().equalsIgnoreCase(context.getString(R.string.tag_attribute_set))) {
                attributeSet = (AttributeSet) nNode.getAttributes();
            }
        }
    }

    public static String getXmlName() {
        return xmlName;
    }

    /**
     * Has to be implemented by the subclass. returns the View for the GUI
     * @return View with (input) elements encapsulated
     */
    public abstract View getView();

    /**
     * Has to be implemented by the subclass. Pulls the data from the generated input elements
     * and saves them to variable(s)
     */
    public abstract void save();

    /**
     *  Has to be implemented by the subclass.
     *  Should clear all graphical objects.
     */
    protected abstract void clear();
    /**
     * Has to be implemented by the subclass. adds the user input data from the variables to
     * a ContentValues object and returns it.
     * @param values Holds the values of all MDobjects in the same poll.
     * @return Same Object as came in, but with additional data.
     */
    public abstract ContentValues db_save(ContentValues values);

    /**
     *  Has to be implemented by the subclass.
     *  Creates a database suitable name from the name variable. In the database it's used as
     *  column name.
     *  This function should call clear() in the end when implemented.
     * @return
     */
    public abstract String getDbString();

    /**
     * Returns the page number. Page number was set in the constructor.
     * @return page number
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * Returns the value of a given node attribute as string.
     * In case of failure (attribute does not exist) it retunrs null
     * @param node Node containing the attribute in question
     * @param nodeName name of the attribute
     * @return
     */
    protected String getNodeValue(Node node, String nodeName) {
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
        for (int i = 0; i< attribs.getLength();i++) {
            item = attribs.item(i);
            if(item.getNodeName().equalsIgnoreCase(nodeName)) {
                return item.getNodeValue();
            }
        }
        Log.e(TAG, "Nodename: " + nodeName + " not found!");
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
    protected int getNodeValueInt(Node node, String nodeName) {
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
     /**
     * Callback function. The embedded activity calls the callback function when
     * OnActivityResult(int reqestCode, int resultCode, Intent data) gets called.
     * callBack(int requestCode, int resultCode, Intent data, int page)
     * in MDPoll. The function dispatches a call to all MDobject subclasses that
     * are on the corresponding page. The functions who implemented the callback
     * the call and can decide by request code if it's relevant or not.
     * @param requestCode given request code in call dispatched Event.
     * @param resultCode positive if success, negative if no success.
     * @param data Contains the data from the externally called app.
     */
    public void activityCallback(int requestCode, int resultCode, Intent data) {

    }


}
