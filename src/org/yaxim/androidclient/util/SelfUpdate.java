package org.yaxim.androidclient.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class SelfUpdate {
	public static void trigger(Context ctx) {
		try {
			final String filename = "/mnt/sdcard/yaxim-selfupdate.apk";
			byte[] buffer = new byte[4096];
			URL url = new URL("http://www.kiszka.org/downloads/yaxim-selfupdate.apk");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			InputStream in = conn.getInputStream();
			FileOutputStream out = new FileOutputStream(filename);
			while (true) {
				int len = in.read(buffer);
				if (len < 0)
					break;
				out.write(buffer, 0, len);
			}
			out.close();
			in.close();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.parse("file://" + filename),
					"application/vnd.android.package-archive");
			ctx.startActivity(intent);
		} catch (Exception e) {
			Log.e("SelfUpdate", Log.getStackTraceString(e));
		}
	}
}
