/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.openapi.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Static logger class
 *
 * @author runarbe
 */
public class Logger {

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static void LogToFile(String msg) {
        try (FileWriter fw = new FileWriter(Settings.LOGDIR + "/openapi.log", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println(msg);
        } catch (IOException e) {
            System.out.println("Logging to file failed: " + e.toString());
        }
    }

    /**
     * Log a message
     *
     * @param msg
     */
    public static void Log(String msg) {
        msg = Logger.getCurrentTimeStamp() + " - " + msg;
        System.out.println(msg);
        LogToFile(msg);
    }

    /**
     * Log a message with a title
     *
     * @param title
     * @param msg
     */
    public static void Log(String title, String msg) {
        msg = Logger.getCurrentTimeStamp() + " - " + msg;
        String msg2 = String.format("%s: %s", title, msg);
        Log(msg2);
    }

    /**
     * Log a message with a title and extra
     *
     * @param title
     * @param msg
     * @param extra
     */
    public static void Log(String title, String msg, String extra) {
        title = Logger.getCurrentTimeStamp() + " - " + title;
        Log(String.format("%s: '%s'\n---\n%s", title, msg, extra));
    }

}
