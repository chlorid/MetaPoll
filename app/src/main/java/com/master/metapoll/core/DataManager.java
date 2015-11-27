package com.master.metapoll.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

/**
 *Class that provides all functionalities for the ui to show available polls,
 *  to import them, to export them and to download and import new polls.
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
public class DataManager {
    private static final String TAG = "DataManager";
    /**
     * The directory app internal files are stored.
     */
    private String filesDirectory;
    /**
     * The directory app internal database files are stored.
     */
    private String dbDirectory;
    private Context context;
    /**
     * A map containing the name of the availaple polls on the device.
     * The key is the human readable name and the value is the path to the file.
     */
    private HashMap<String,String> pollsOnDisk = null;
    /**
     * A map containing the name of the saved databases on the device.
     * The key is the human readable name and the value is the path to the file.
     */
    private HashMap<String,String> dBsOnDisk = null;
    /**
     * A map containing the name of the availaple polls on the webserver.
     * The key is the human readable name and the value is the url to the ressource.
     */
    private HashMap<String,String> pollsOnWeb = null;
    /**
     * Helper object. Deals with metalist.xml
     */
    private MDLhelper mdlHelper;

    /**
     * The files and database directory are set in the constructor,
     * They might change in case of different paths in different environments.
     * @param context
     */
    public DataManager(Context context) {
        this.context = context;
        filesDirectory = context.getFilesDir().getAbsolutePath();
        dbDirectory = context.getFilesDir().getAbsolutePath().replace("files", "databases");
        pollsOnDisk = new HashMap<>();
        dBsOnDisk = new HashMap<>();
        pollsOnWeb = new HashMap<>();
        // Trying to retrieve List of available MetadataPackages.
        new NWhelper.MetadataListDownloadTask().execute(this.context);
        try {
            mdlHelper = new MDLhelper(filesDirectory + "/" + "metalist.xml", context);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A wrapper function for obtaining an ArrayList of all available
     * polls on the webserver.
     * @return Names of available polls
     */
    public ArrayList<String> getDlPollList() {
        ArrayList<String> result = new ArrayList<>();
        if (pollsOnWeb.size() == 0 ) {
            Log.i(TAG,"parse meta list()");
            try {
                pollsOnWeb = mdlHelper.getPollsFromWebList();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
            Iterator entries = pollsOnWeb.entrySet().iterator();
            while (entries.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) entries.next();

                result.add((String) entry.getKey());
            }

        return result;
    }

    /**
     * A wrapper function for obtaining an ArrayList of all available
     * polls in the app internal storage.
     * @return Names of available polls
     */
    public ArrayList<String> getStoredPollList() {
        ArrayList<String> result = new ArrayList<>();
        String pollName  = null;
        // if we didn't already fill pollsOnDisk we do it now.
        if (pollsOnDisk.size() == 0) {
            String[] filenames = getFileNames(filesDirectory, ".xml");
            for (String fileName : filenames) {
                Log.i(TAG, ">>>>>>>>>>>>>>Filename : " + fileName);
                try {
                    pollName = mdlHelper.getPollNameFromFile(filesDirectory + "/" + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, ">>>>>>>>>>>>>>Evalename: " + pollName);
                if (pollName != null) {
                    result.add(pollName);
                    pollsOnDisk.put(pollName,fileName);
                } else {
                    Log.e(TAG, "no evals");
                }
            }
        }
        else {
            Iterator entries = pollsOnDisk.entrySet().iterator();
            while (entries.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) entries.next();

                result.add((String) entry.getKey());
            }
        }
        return result;
    }

    /**
     * A wrapper function for obtaining an ArrayList of all available
     * databases  on the app internal database directory.
     * @return Names of available databses
     */
    public ArrayList<String> getStoredDbList() {
        ArrayList<String> result = new ArrayList<>();
        String dbName  = null;
        // if we didn't already get filled pollsOnDisk we do it now.
        if (dBsOnDisk.size() == 0) {
            String[] filenames = getFileNames(dbDirectory, ".db");
            for (String fileName : filenames) {
                Log.i(TAG, ">>>>>>>>>>>>>>Filename : " + fileName);
                dbName = fileName.replace("db_","").replace("_"," ");
                if (dbName != null) {
                    result.add(dbName);
                    dBsOnDisk.put(dbName,fileName);
                } else {
                    Log.e(TAG, "no dbs");
                }
            }
        }
        else {
            Iterator entries = dBsOnDisk.entrySet().iterator();
            while (entries.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) entries.next();

                result.add((String) entry.getKey());
            }
        }
        return result;
    }

    /**
     * Gives the matching name of a poll by the given URL.
     * @param url
     * @return
     */
    public String getPollNameFromUrl(String url) {
        Iterator entries = pollsOnWeb.entrySet().iterator();
        while (entries.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) entries.next();

            if (entry.getValue().equals(url)) {
                return (String) entry.getKey();
            }

        }
        return null;
    }

    /**
     * Returns the path to the XML metadata by giving the name of the name attribute in the xml file
     * This converts basically the entries of the list you obtain by calling getStoredPollList
     * back to an absolute file path.
     * @param name of poll
     * @return path to poll
     */
    public String getPollPathByName(String name) {

        return filesDirectory + "/" + pollsOnDisk.get(name);
    }

    public void exportDbToFs(String path, String name) {
        String toastText = "File successfully exported.";
        try {
            copyFile(new File(dbDirectory + "/" + name), new File(path + "/" + name));
        } catch (IOException e) {
            toastText = "Error. could not save file.";

            e.printStackTrace();
        }
        finally {
            Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Imports an XML metadata file to the apps internal directory.
     * A basic check for validity is made by checking if it at least has a <pollDocument>
     *     with a name attribute.
     * @param path file to import
     */
    public void importToFs(String path) {
        String toastText = "File successfully imported.";
        try {
            File source = new File(path);
            if (mdlHelper.getPollNameFromFile(path) != null) {
                copyFile(source, new File(filesDirectory + "/" + source.getName()));
            }
            else {
                Log.e(TAG,"Error. could not import file. File is invalid" );
            }

        } catch (IOException e) {
            toastText = "Error. could not import file.";

            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally {
            Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Wrapper function for executing the upload of a databse to the webserver.
     * @param name databse name
     */
    public void exportDbToWeb(String name) {

        new NWhelper.FileUploadTask().execute(dbDirectory + "/" + dBsOnDisk.get(name), context);
    }

    /**
     * Wrapper for starting the download of a XML metadata document.
     * @param name
     */
    public void downloadFile(String name) {
        if (pollsOnWeb.size() == 0 ) {
            try {
                pollsOnWeb = mdlHelper.getPollsFromWebList();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "dwnloading : " + name + " from URL: " + pollsOnWeb.get(name));
        new NWhelper.PollDownloadTask().execute(null, pollsOnWeb.get(name), context);
    }

    /*****************************file management*******************************/
    /**
     * Returns a list of all filenames in the given directory with the given extension.
     * @param directory to search in
     * @param fileExtension to search for
     * @return
     */
    public String[]  getFileNames(String directory, final String fileExtension) {
        File path = new File(directory);

        FilenameFilter xmlFilter = new FilenameFilter() {
            File f;

            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(fileExtension.toLowerCase())) {
                    return true;
                }
                f = new File(dir.getAbsolutePath() + "/" + name);

                return f.isDirectory();
            }
        };
        return  path.list(xmlFilter);
    }

    /**
     * Copies a file from a given source to a given destination.
     * @param src source file
     * @param dst destination file
     * @throws IOException
     */
    public void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
/*******************************************/
    public void deletePoll(String pollName) {
        String pollpath = pollsOnDisk.get(pollName);
        Log.d(TAG, "deleting: " + pollpath + "test: " + new File(pollpath).getParent());
//        deleteRecursive(new File(pollpath).getParent());
        pollsOnDisk.remove(pollName);
    }

    /**
     * Full directories cannot be deleted. so we cruise through everything and delete everything.
     * @param fileOrDirectory to delete
     */
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}

