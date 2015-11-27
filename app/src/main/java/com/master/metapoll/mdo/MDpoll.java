package com.master.metapoll.mdo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.view.View;

import com.master.metapoll.R;
import com.master.metapoll.core.ClassScanner;
import com.master.metapoll.core.Debugging;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * This class parses the given pollDocument and creates the corresponding MDobjects.
 *
 * In case it finds an unknown object it tries to load the corresponding class from the filesystem
 * if it finds a path in the xml document.
 *
 * It provides functions for the GUI to get handle the MDobjects:
 * -save them to a database
 * -obtain the Views of the MDobjects
 * -get information about the minimum quantity of datasets that have to be created
 * -get information about the quantity of datasets already in the database.
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
public class MDpoll {
    /**
     * TAG is used for Log outputs
     */
    private static final String TAG = "MDpoll";
    /**
     * Holds the generated MDobjects.
     */
    private ArrayList<MDobject> mDobjects = null;
    /**
     * Database access.
     */
    private Dbhelper mDbhelper;
    /**
     * Holds the number of pages with MDobjects.
     */
    private int pageCount = 0;
    /**
     * Android specific.
     * Context is needed to create the MDobjects
     */
    private final Context context;

    private HashMap<String, String> foundClasses;



    private XMLhelper xmlHelper;

    /**
     * Constructor.
     *
     * @param context Application context.
     * @param filepath Path to a valid poll XML document
     */
    public MDpoll(Context context, String filepath)  {
        this.context = context;
        Log.d(TAG,"created mdPoll. Filepath: " + filepath);
        try {
            xmlHelper = new XMLhelper(filepath);
            mDobjects = xmlHelper.getmDobjectList();
            Log.d(TAG,"mdobjects counet: " + mDobjects.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*******************************public methods***********************************************/
    /**
     * Returns the amount of already created datasets. This might be interesting for the user if he
     * hast to create a certain amount of datasets.
     * In case there is no valid dataset, value is negative. This gives the Ui the chance to handle
     * an error case.
     * @return number of already created datasets.
     */
    public long getDatasetCount() {
        SQLiteDatabase db = mDbhelper.getReadableDatabase();
        if (db == null) {
            return -1;
        }
        SQLiteStatement s = db.compileStatement("select count(*) from " + createDbName(xmlHelper.getName()) + ";");

        return s.simpleQueryForLong();
    }

    /**
     *
     * Returns the page quantity of this poll.
     * @return page count
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Returns the quantity of datasets that have to be created. This might be interesting for the user if he
     * hast to create a certain amount of datasets.
     * In case there is no valid dataset, value is negative. This gives the Ui the chance to not showing
     * an invalid value.
     * @return The optional minimum quantity of datasets that have to be created. -1 if not set.
     */
    public int getMinQty() {
        return xmlHelper.getMinQty();
    }

    /**
     * When a poll is running, the UI might destroy the corresponding views. The function tells every
     * containiing object to pull the values introduced in the corresponding views and save them to a variable.
     *
     * @param page Number of the page that hast to be saved.
     */
    public void savePage(int page) {
        Log.i(TAG, "Saving page: " + page);
        for (MDobject o : mDobjects) {
            if (o.getPageNo() == page) {
                o.save();
            }
        }
    }

    /**
     * Saves the data of all MDobjects to the database. This is called on the end of a poll, when
     * all values are taken.
     *
     * @return ID of the dataset saved.
     */
    public long db_save() {

        if (mDbhelper == null) {
            createDb();
        }
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbhelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        //Tell all Objects to save their values.
        for (MDobject o : mDobjects) {
            o.db_save(values);
        }
        long newRowId;
        newRowId = db.insert(
                createDbName(xmlHelper.getName()),
                null,
                values);

        Log.d(TAG, "Added new Table entry. ID is: " + newRowId);
        return newRowId;
    }

    /**
     * Returns the View objects for the corresponding page. The functin picks up the View objects
     * for the UI and stores them to a list. The UI decides then, how to show them to the user.
     *
     * @param page The number of the page
     * @return The list of views that the given page contains.
     */
    public ArrayList<View> getViews(int page) {
        ArrayList<View> pageViews = new ArrayList<>();
        if (mDobjects != null) {
            for (MDobject e : mDobjects) {
                if (e.getPageNo() == page) {
                    pageViews.add(e.getView());
                }
            }
        } else {
            Log.e(TAG, "Error. No Views returning empty list");
        }
        return pageViews;
    }

    public void callBack(int requestCode, int resultCode, Intent data, int page) {
        for (MDobject o :mDobjects) {
            o.activityCallback(requestCode,resultCode,data);
        }
    }
/*************************************private ***************************************/
    /**
     * Creates a database using the name of the MDpoll as name of db file and table name.
     * Creates automatically primary key column and adds by calling the sub functions of the
     * containing Pages and their UiElements the corresponding columns in the database
     */
    private void createDb() {
        String name = createDbName(xmlHelper.getName());
        StringBuilder tables = new StringBuilder("CREATE TABLE " + name + " ( id  INTEGER PRIMARY KEY NOT NULL,");

        // pick up the column descriptions from each MDobject.
        for (MDobject o : mDobjects) {
            tables.append(o.getDbString());
        }
        // fix the database string: If picked up from the MDobjects, last character is alwas comma,
        // because there might follow more. In case of the last comma, we have to replace it with );
        tables.replace(tables.length() - 1, tables.length(), ")");
        mDbhelper = new Dbhelper(context, name + ".db", tables.toString() + ";");
    }




    /**
     * Private class to load a MDobject class on runtime.
     * Thanks to:
     * http://stackoverflow.com/questions/20068040/accessing-to-classes-of-app-from-dex-file-by-classloader
     * @param path path to the class on file system
     * @return Class that is loaded
     * @throws ClassNotFoundException
     */
    private Class loadDexClass(String path) throws ClassNotFoundException {
            Log.d(TAG,"trynig to load: " + path);
        String optimizedDex = context.getCacheDir().getAbsolutePath();
        DexClassLoader classLoader = new DexClassLoader(
                path, optimizedDex, null, getClass().getClassLoader());
//        return (Class<Object>)Class.forName("com.master.imageelement.MDOimage");
       return classLoader.loadClass("com.master.metapoll.mdo.MDOimage");
    }

    private Class loadClassLocal(String mClassName) throws ClassNotFoundException {
        PathClassLoader classLoader = (PathClassLoader) context.getClassLoader();

        return classLoader.loadClass(mClassName);
    }

    public int getId() {
        return xmlHelper.getId();
    }

    /**
     * Creates a databse suitable name of a given string by replacing whitespaces and points.
     * @param name
     * @return
     */
    public static String createDbName(String name) {
        // Table name cannot contain whitespaces, so we remove them.
        name = name.replace(" ","_").replace(".", "");
        // Table name cannot start with numbers and special characters. so we add on the beginning always db_
        return "db_" + name;
    }


    /**
     * Helper functions for dealing with the database. It mainly provides the database helper object.
     *
     * Adapted from sample on http://developer.android.org
     * @author David Kauderer
     * @version 0.1
     *
     */
    private class Dbhelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        private String mEntries;

        public Dbhelper(Context context, String databaseName, String entries) {
            super(context, databaseName, null, DATABASE_VERSION);
            this.mEntries = entries;
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(mEntries);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: hanlde this.
        }
    }

    /**
     * Helper functins for dealing with the XML documents. All static, because it's always
     * different documents. It would not make sense to always create an object.
     *
     * @author David Kauderer
     * @version 0.1
     *
     */
    private class XMLhelper {
        private static final String TAG = "UrlExtractor";
        private String pollFilePath;
        private Document xmlDocument;
        /**
         * Holds the name of the specific poll.
         */
        private String name = "generic";
        /**
         * Holds the id of the specific poll.
         */
        private int id = -1;
        /**
         * Holds the minimum quantity of datasets needed, if available.
         */
        private int minQty = -1;

        private ArrayList<MDobject> mDobjectList;

        /**
         * Constructor
         * @param filepath Path to file to work with.
         */
        public XMLhelper(String filepath) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            this.pollFilePath = filepath;
            File file = new File(pollFilePath);
            mDobjectList = new ArrayList<>();
            if (!file.exists()) {
                Log.e(TAG,"could not open file: " + pollFilePath);
                return;
            }
            else{
                //parse XML Document.
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                xmlDocument = db.parse(file);
                xmlDocument.getDocumentElement().normalize();
                handleDocument();
            }
        }

        /************************************public ****************************************/

        public int getId() {
           return id;
        }

        public String getName() {
            return name;
        }

        public int getMinQty() {
            return minQty;
        }

        public ArrayList<MDobject> getmDobjectList() {
            return mDobjectList;
        }

        /************************************private *************************************
        /**
         * Parses the document values.
         * Sets the values in the present class,
         * generates MDobject instances and adds them to the
         * mdObjects ArrayList.
         *
         *
         */
        private void handleDocument() throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

            NodeList nodes = xmlDocument.getElementsByTagName(context.getString(R.string.tag_root_polldoc));
            if (nodes == null) {
                Log.e(TAG, "Cannot parse document. The document " + pollFilePath + " is invalid! Stop.");
                return;
            }
            // There should be only one Document Tag, if there are more, we just handle the first one.
            if (nodes.getLength() > 1) {
                Log.e(TAG, "document has more than one document tag. Omitting other document tags.");
            } else if (nodes.getLength() < 1) {
                Log.e(TAG, "document has no document tag. We are finished here.");
                return;
            } else {
                Node nNode = nodes.item(0);
                Log.i(TAG, "Document name:" + nodes.item(0).getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    // handle Document
                    this.id = getNodeValueInt(nNode, "id");
                    this.name = getNodeValue(nNode, "name");
                    Log.i(TAG, "Name set to:" + name);
                    this.minQty = getNodeValueInt(nNode, "quantity");

                    // get the pages, means the children
                    for (Node child = nNode.getFirstChild(); child != null;
                         child = child.getNextSibling()) {
                        if (child.getNodeName().equalsIgnoreCase("page")) {
                            handlePage(child, pageCount);
                            pageCount++;
                        }
                    }
                }
            }
        }

        /**
         * Internal not accessible function that creates the MDobjects from XML.
         *
         * @param node Node of DOM Tree containing a page.
         * @param pageNo The number of the page. Needed to tell the MDobjects to which page they belong.
         */

        private void handlePage(Node node, int pageNo) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, MalformedURLException {
            Log.i(TAG, "handling page. Title: " + getNodeValue(node, "title"));
            Class mdObjectClass = null;

            //Label for page headline
            String headline = getNodeValue(node, "title");
            // if null, no headline.
            if (headline != null) {
                mDobjectList.add(new MDOlabelHeadline(context, node, pageNo));
            }
            if (foundClasses == null) {
                foundClasses = getMdObjectClasses();
            }
            //handle others
            for (Node child = node.getFirstChild(); child != null;
                 child = child.getNextSibling()) {

                if (child.getNodeType() == Node.ELEMENT_NODE) {

                    // The xml Element name.
                    String clname = child.getNodeName().toLowerCase();
                    if (clname != null) {
//                        Iterator entries = foundClasses.entrySet().iterator();
//
//                        while (entries.hasNext()) {
//                            HashMap.Entry entry = (HashMap.Entry) entries.next();
//
//                        }
                        // if a class in the local class path was found, this is true.
                        if (foundClasses.get(clname.toLowerCase()) != null) {
                            mdObjectClass = loadClassLocal(foundClasses.get(clname));
                            Log.i(TAG, "Class added: " + foundClasses.get(clname) + " Classname: " + clname);

                        }
                        else {
                            //If we didn't find the class locally, we search for classes in the DOM tree.
                            for (Node gchild = child.getFirstChild(); gchild != null;
                                 gchild = gchild.getNextSibling()) {
                                Debugging.echo(gchild);
                                if (gchild.getNodeType() == Node.ELEMENT_NODE) {
                                    String dexurl = getNodeValue(gchild, "dexpath");
                                    Log.d(TAG, "dex url " + dexurl);
                                    String dexFileName = new URL(dexurl).getFile();
                                    String dexFilePath = new File(pollFilePath).getParent() + dexFileName;
                                    Log.d(TAG, "dex file path: " + dexFilePath);
                                    if (dexFilePath != null) {

                                        mdObjectClass = loadDexClass(dexFilePath);
                                    } else {
                                        Log.i(TAG, "Invalid Tag and no path to load class: " + child.getNodeName().toLowerCase());
                                    }
                                }
                            }

                        }
                        if (mdObjectClass != null) {
                            Constructor constructor = null;
                            // TODO: getDeclaredConstructor() fails, even though the constructor exists! Workaround is the
                            // the following not very nice code:
                            Constructor[] consts = mdObjectClass.getDeclaredConstructors();
                            constructor = consts[0];
                            constructor.setAccessible(true);
                            MDobject obj = (MDobject) constructor.newInstance(context, child, pageNo);
                            mDobjectList.add(obj);
                        }
                    }
                }
            }
        }



        /**
         * Returns the value of a given node attribute as string.
         * In case of failure (attribute does not exist) it retunrs null
         * @param node Node containing the attribute in question
         * @param nodeName name of the attribute
         * @return
         */
        public String getNodeValue(Node node, String nodeName) {
            Node item;
            if (node == null) {
                Log.e(TAG,"got empty node. Nothing to do.");
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
        public int getNodeValueInt(Node node, String nodeName) {
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
    }

    /**
     * This function scans for menu entries and creates a map, containing
     * The menu entries obtained by invoking getMenuEntry of the subclasses of MenuEntry
     * as keys and the class name as value.
     *
     * @return map. key: menu entry value: class name.
     */
    private HashMap<String,String> getMdObjectClasses() {
        final HashMap<String,String> classes = new HashMap<String,String>();
        try {
            new ClassScanner(context) {

                @Override
                protected boolean isTargetClassName(String className) {
                    return className.startsWith(getContext().getPackageName())//I want classes under my package
                            && !className.contains("$");//I don't need none-static inner classes
                }

                @Override
                protected boolean isTargetClass(Class clazz) {
                    return MDobject.class.isAssignableFrom(clazz)//I want subclasses of MDobject
                            && !Modifier.isAbstract(clazz.getModifiers());//I don't want abstract classes
                }

                @Override
                protected void onScanResult(Class clazz)  {
                    try {
                        String menuName = (String) clazz.getDeclaredMethod("getXmlName").invoke(null,null);

                        if (menuName != null) {
                            classes.put(menuName.toLowerCase(), clazz.getName());
                        }
                    } catch (NoSuchMethodException e) {
                        Log.e(TAG,"No such method.");
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }.scan();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return classes;
    }
}







