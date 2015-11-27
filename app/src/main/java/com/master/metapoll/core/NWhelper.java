package com.master.metapoll.core;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.master.metapoll.R;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Helper functins for dealing with network operations. All static except of the asynchronous tasks
 * themselves, because it's always different files to download.
 * It would not make sense to always create an object off this class..
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
public class NWhelper {
    // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    private static final String TAG = "NWhelper";
    /**
     * Users ID. //TODO: this has to be obtained from somewhere.
     */
    private static final String USER_ID = "testuser@example.com";
    /**
     * The webserver containing the ressources and metadata for the application.
     *
     */
    private static final String WEBSERVER_URL = "http://192.168.177.133:8000";
    /**
     * A variable to switch between live mode (HTTPS) and debug (HTTP)
     */
    private static final boolean RUN_LIVE = false;

    /**
     * Downloads a file via HTTP(S) GET to the given path. This function cannot be called from the
     * UI thread. Android does not allow it.
     * @param urlString Url to the ressource to download.
     * @param path path where the file should be saved.
     * @param overwrite if file exists, overwrite?
     * @return flase if download was not successful. If successful, true.
     */
    private static Boolean fileDownloadHttp(String urlString, String path, Boolean overwrite) {
        return fileDownloadHttp(urlString, new File(path), overwrite);
    }

    /**
     * Downloads a file via HTTP(S) GET to the given path. This function cannot be called from the
     * UI thread. Android does not allow it.
     *
     * @param urlString Url to the ressource to download.
     * @param file      file to be written to.
     * @param overwrite if file exists, overwrite?
     * @return flase if download was not successful. If successful, true.
     */
    private static Boolean fileDownloadHttp(String urlString, File file, Boolean overwrite) {
        HashMap<String,String> result = null;
        URL url = null;
//        File temp;

        try {
            url = new URL(urlString);



            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(200000);
            urlConnection.connect();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//            temp = new File(path  +  url.getFile());
//            file = new File(path  +  url.getFile());



            FileOutputStream outputStream =
                    new FileOutputStream(file);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }


        in.close();
        outputStream.close();
        urlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Log.i(TAG, "File download: " + file.getAbsolutePath() + url.getFile() + "overwrite " + overwrite + "exists? " + file.exists());
//        if (overwrite) {
//            Log.i(TAG, " deleting:" + temp.getAbsoluteFile());
//            temp.renameTo(file);
//
//            Log.i(TAG,"exists? " + file.exists());
//        }

        return true;
    }

    /**
     * Function, that loads a file to the server asynchronously. Special
     * in the way that a file is transferred.
     *
     * @param context
     *            The current Activity context.
     */
    private static Boolean fileUpload(String filepath, Context context) {


        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String postString;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "----------------------------------78567874345678976548976857";
        String serverResponseRead = null;
        String serverResponseMessage = null;
        String serverResponse = "";
        File file = null;
        int serverResponseCode = 0;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        FileInputStream fileInputStream = null;
        URL url;

        // file to upload
        file = new File(filepath);
        Log.d(TAG, "uploading file: " + filepath);
        // Build the POST string.
//        postString = "Content-Disposition: form-data; name=\"account\""
//                + lineEnd + lineEnd + USER_ID + lineEnd
//                + twoHyphens + boundary + lineEnd;
        postString = "Content-Disposition: form-data; name=\"file\"; filename=\""
                + file.getName()
                + "\""
                + lineEnd
                + "Content-Type: application/octet-stream" + lineEnd;


        // Load the file and transfer it to the server.
        try {
            fileInputStream = new FileInputStream(file);

            url = new URL(WEBSERVER_URL);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
//            connection = (HttpURLConnection) url.openConnection();
//            if (!RUN_LIVE) {
//                trustAllHosts();
//                connection.setHostnameVerifier(DO_NOT_VERIFY);
//            }
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Enable POST method
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Origin", "metapoll_app");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("charset", "utf-8");
            bytesAvailable = fileInputStream.available();
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            connection.connect();

//            Log.d(TAG, "connectionmethod:: " + connection.getRequestMethod() + " url: " + url.toString() + connection.getErrorStream());
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

//            if (RUN_LIVE) {
            outputStream.writeBytes(postString);
//            }
//            outputStream
//                    .writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
//                            + file.getName()
//                            + "\""
//                            + lineEnd
//                            + "Content-Type: application/octet-stream" + lineEnd + lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read picture file and send it to the server.
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            try {
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);
            } catch (Exception e) {
                Log.e(TAG, "fileUpload(): Out of memory while parsing result!");
                e.printStackTrace();
                fileInputStream.close();
                // Abort operation since we don't have a proper response.
                return false;
            }

            // Send closing bytes.
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            // Load responses from the server (code and message)
            serverResponseCode = connection.getResponseCode();
            serverResponseMessage = connection.getResponseMessage();

            fileInputStream.close();
            outputStream.flush();

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            // Read the server's response.
            while ((serverResponseRead = rd.readLine()) != null) {
                serverResponse = serverResponseRead;
            }

            rd.close();
            outputStream.close();
            buffer = null;
        } catch (Exception e) {
            Log.e(TAG, "something went wrong with the upload.\n"
                    + "serverResponseCode: " + serverResponseCode + "\n"
                    + "serverResponseMessage " + serverResponseMessage + "\n"
                    + "serverResponse" + serverResponse);
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Trust every server - don't check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches in the given file for ressources that have to be downloaded additionally to the
     * metadatapackage itself. This could be a photo or audio or other binaries.
     * It basically searches for a <ressources> tag and downloads all the files given in it's attributes.
     *
     * @return List of all files that have to be downloaded.
     */
    public static ArrayList<String> parseForRessources(String filepath) throws ParserConfigurationException, IOException, SAXException {
        Document xmlDocument;
        ArrayList<String> result = new ArrayList<>();

        File file = new File(filepath);
        if (!file.exists()) {
            Log.e(TAG, "could not open file: " + filepath);
            return null;
        } else {
            //parse XML Document.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            xmlDocument = db.parse(file);
            xmlDocument.getDocumentElement().normalize();
        }

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


//    private static boolean fileUpload(String filepath) {
//
//    }

    /**
     * Sarches for ressources that have to be downloaded and creates a list with all download links.
     *
     * @param node to check for download links
     * @return list with all found download links
     */
    private static ArrayList<String> findUrls(Node node) {


        int type = node.getNodeType();
        ArrayList<String> result = new ArrayList<>();
        String temp = null;
        NamedNodeMap atts = node.getAttributes();
        Log.i(TAG, "parsing for ressources.  node: " + node.getNodeName() + " value: " + node.getNodeValue() + " atts length: " + atts.getLength() + "type: " + type);
        switch (type) {
            //Element
            case Node.ELEMENT_NODE:
                //TODO: This method is stupid. It just looks for
                // attributes named ressourcepath. What if we have to download several ressources?


                for (int j = 0; j < atts.getLength(); j++) {
                    Log.i(TAG, "atts: " + atts.item(j).getNodeName() + " value: " + atts.item(j).getNodeValue());
                    temp = atts.item(j).getNodeValue();

                    if (temp != null) {
                        result.add(temp);
                        Log.i(TAG, "added path: " + temp);
                    }

                    Log.i(TAG, "parent node:" + node.getParentNode().getNodeName());
                    Element parent = (Element) node.getParentNode();
                    parent.setAttribute("TESTITEST", "dllink");

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
        return result;
    }

    	/*
     * SSL related stuff
	 */

    /**
     * Asynchronous task, that uploads a file to the webserver.
     * Wha Asynchronus task? Android does not allow network operations on the UI thread.
     *
     *
     */
    public static class FileUploadTask extends
            AsyncTask<Object, Void, Boolean> {
        Context context;

        protected Boolean doInBackground(Object... params) {

            String serverResponse = "";
            String action = "";
            String postString = "";
            String filepath;

            JSONObject responseJSON;

            // Read parameters and cast them to something usable.
            try {
                // The unchecked warning here is fine. We are the only ones
                // calling this method and it receives different kinds of
                // objects as args, so we can't do anything about that.
                // The @suppressWarnings is not here so we don't overlook
                // another situation where this might occur.
                filepath = (String) params[0];
                context = (Context) params[1];

            } catch (Exception e) {
                Log.e(TAG, "Error initializing params for file upload.", e);
                return false;
            }
            if (fileUpload(filepath, context)) {
                return true;
            } else {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            String message = "File upload not successful.";
            if (aBoolean) {
                message = "File upload usccessful";
            }
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.show();

        }
    }

    /**
     * Asynchronous task to download a list of files.
     *
     */
    public static class FileDownloadTask extends
            AsyncTask<Object, Void, Boolean> {
        Context context;

        protected Boolean doInBackground(Object... params) {

            String destinationPath;
            ArrayList<String> urls;

            JSONObject responseJSON;

            // Read parameters and cast them to something usable.
            try {
                destinationPath = (String) params[0];
                urls = (ArrayList<String>) params[1];
                context = (Context) params[2];

            } catch (Exception e) {
                Log.e(TAG, "Error initializing params for file upload.", e);
                return false;
            }
            // No path means default path.
            if (destinationPath == null) {
                destinationPath = context.getFilesDir().getAbsolutePath();
            }
            Boolean success = false;
            for (String url : urls) {
                try {
                    Log.d(TAG, "downloading file: " + destinationPath + new URL(url).getFile());
                    // Call download function. Use HTTPS when running in live mode.
//                    if (LIVE) {
//                        success = fileDownloadHttps(url, destinationPath + new URL(url).getFile(), true);
//                    } else {
                    success = fileDownloadHttp(url, destinationPath + new URL(url).getFile(), true);
//                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            String message = "File download not successful.";
            if (aBoolean) {
                message = "File download sccessful";
            }
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Asynchronous task,t download a poll metadatapackage.
     * After downloading it searches the document for further ressources to download and
     * downloads them by calling FileDownload task with the list of found ressources to download.
     *
     *
     *
     */
    public static class PollDownloadTask extends
            AsyncTask<Object, Void, Boolean> {
        Context context;
        DataManager dataManager;
        String url;
        File pollDir;
        String dirName;


        protected Boolean doInBackground(Object... params) {

            String filePath = null;


            JSONObject responseJSON;

            // Read parameters and cast them to something usable.
            try {
                dataManager = (DataManager) params[0];
                url = (String) params[1];
                context = (Context) params[2];

            } catch (Exception e) {
                Log.e(TAG, "Error initializing params for file upload.", e);
                return false;
            }


            // No path means default path.


            try {
                dirName = dataManager.getPollNameFromUrl(url).replace(" ", "_");
                // we don't want spaces in path. We replace them by _
                pollDir = context.getDir(dirName, Context.MODE_PRIVATE);
                Log.i(TAG, pollDir.getAbsolutePath() + " exists? " + pollDir.exists());
                Log.i(TAG, "saving file to:" + pollDir.getAbsolutePath() + new URL(url).getFile());
//                if (destinationPath == null) {
//                    destinationPath = context.getFilesDir().getAbsolutePath();
//                }
                filePath = pollDir.getAbsolutePath() + "/" + new URL(url).getFile();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return fileDownloadHttp(url, filePath, true);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            String message = "File download not successful.";
            if (aBoolean) {
                message = "File download successful";
                try {
                    Log.i(TAG, pollDir.getAbsolutePath() + " exists? " + pollDir.exists());
                    // if folder does not exist, we create it.
//                    File f = new File(destinationPath);
//                    if (!f.exists()) {
//                        Log.i(TAG, "folder created? " + f.mkdirs());
//                    }
                    ArrayList<String> dlList = parseForRessources(pollDir.getAbsolutePath() + new URL(url).getFile());
                    new FileDownloadTask().execute(pollDir.getAbsolutePath(), dlList, context);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }

            }
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Asynchronous task, to download the list of metadata packages available on the server.
     *
     *
     */
    public static class MetadataListDownloadTask extends
            AsyncTask<Object, Void, Void> {

        protected Void doInBackground(Object... params) {
            Context context;

            // Read parameters and cast them to something usable.
            try {
                // The unchecked warning here is fine. We are the only ones
                // calling this method and it receives different kinds of
                // objects as args, so we can't do anything about that.
                // The @suppressWarnings is not here so we don't overlook
                // another situation where this might occur.
                context = (Context) params[0];

            } catch (Exception e) {
                Log.e(TAG, "Error initializing params for file upload.", e);
                return null;
            }
            Boolean result = fileDownloadHttp(WEBSERVER_URL + "/" + context.getString(R.string.doc_metalist), context.getFilesDir().getAbsolutePath() + "/" + context.getString(R.string.doc_metalist), true);
            Log.i(TAG, "Metalist download successful? " + result);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    }


