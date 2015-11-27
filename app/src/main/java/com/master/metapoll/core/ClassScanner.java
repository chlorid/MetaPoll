package com.master.metapoll.core;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Enumeration;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * This class scans for classes and subcallses of the given classtype.
 * Thanks to fantouch:
 * http://stackoverflow.com/questions/15446036/find-all-classes-in-a-package-in-android
 *
 * @author David Kauderer
 * @version 0.1
 *
 */
public abstract class ClassScanner {

    private static final String TAG = "ClassScanner";
    private Context mContext;
    private String dexFilePath;

    public ClassScanner(Context context) {

        mContext = context;
        dexFilePath = getContext().getPackageCodePath();
    }

    /**
     * Constructor with different dex path to search in.
     * @param context
     * @param dexFilePath
     */
    public ClassScanner(Context context, String dexFilePath) {

        mContext = context;
        this.dexFilePath = dexFilePath;
    }

    public Context getContext() {
        return mContext;
    }

    public void scan() throws IOException, ClassNotFoundException, NoSuchMethodException {
        long timeBegin = System.currentTimeMillis();

        PathClassLoader classLoader = (PathClassLoader) getContext().getClassLoader();
        //PathClassLoader classLoader = (PathClassLoader) Thread.currentThread().getContextClassLoader();//This also works good
        DexFile dexFile = new DexFile(dexFilePath);
        Enumeration<String> classNames = dexFile.entries();
        while (classNames.hasMoreElements()) {
            String className = classNames.nextElement();
            if (isTargetClassName(className)) {
                //Class<?> aClass = Class.forName(className);//java.lang.ExceptionInInitializerError
                //Class<?> aClass = Class.forName(className, false, classLoader);//tested on 魅蓝Note(M463C)_Android4.4.4 and Mi2s_Android5.1.1
                Class<?> aClass = classLoader.loadClass(className);//tested on 魅蓝Note(M463C)_Android4.4.4 and Mi2s_Android5.1.1
                if (isTargetClass(aClass)) {
                    onScanResult(aClass);
                }
            }
        }

        long timeEnd = System.currentTimeMillis();
        long timeElapsed = timeEnd - timeBegin;
        Log.d(TAG, "scan() cost " + timeElapsed + "ms");
    }

    protected abstract boolean isTargetClassName(String className);

    protected abstract boolean isTargetClass(Class clazz);

    protected abstract void onScanResult(Class clazz);
}