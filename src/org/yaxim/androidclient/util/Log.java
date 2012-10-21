package org.yaxim.androidclient.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Process;

public class Log {
	private static final int LOG_BUFFER_SIZE = 2000;

	private static String[] logBuffer = new String[LOG_BUFFER_SIZE];
	private static int logBufferPos = 0;
	private static SimpleDateFormat dateFormater = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");

	private static synchronized void log(char type, String tag, String msg) {
		logBuffer[logBufferPos++] = dateFormater.format(new Date()) + " " + Process.myTid() + " " + type + " " + tag + ": " + msg + "\n";
		if (logBufferPos >= LOG_BUFFER_SIZE)
			logBufferPos = 0;
	}

	public static synchronized String getLog() {
		int pos = logBufferPos;
		String result = "";
		do {
			if (logBuffer[pos] != null)
				result += logBuffer[pos];
			pos++;
			if (pos >= LOG_BUFFER_SIZE)
				pos = 0;
		} while (pos != logBufferPos);
		return result;
	}

	public static int d(String tag, String msg) {
		log('D', tag, msg);
		return android.util.Log.d(tag, msg);
	}

	public static int i(String tag, String msg) {
		log('I', tag, msg);
		return android.util.Log.i(tag, msg);
	}

	public static int e(String tag, String msg) {
		log('E', tag, msg);
		return android.util.Log.e(tag, msg);
	}

	public static int e(String tag, String msg, Throwable tr) {
		log('E', tag, msg + "\n" + getStackTraceString(tr));
		return android.util.Log.e(tag, msg, tr);
	}

	public static String getStackTraceString(Throwable tr) {
		return android.util.Log.getStackTraceString(tr);
	}
}
