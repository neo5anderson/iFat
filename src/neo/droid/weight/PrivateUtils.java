package neo.droid.weight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neo.droid.commons.Configurations;
import neo.droid.commons.ResUtils;
import neo.droid.commons.SQLiteUtils;
import android.content.Context;
import android.database.Cursor;

public class PrivateUtils {
	protected static Context CONTEXT;

	protected static int USER_LISTVIEW_HEIGHT;
	protected static List<Map<String, String>> USER_LIST;

	protected static SQLiteUtils DB_UTILS;

	private static String DB_NAME;
	private static Map<String, String> TABLES_MAP;
	private static final int DB_VERSION = 1;
	private static final String SCHEMA_USER = "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, sex TEXT, style_id INTEGER, color_id INTEGER";
	private static final String SCHEMA_STYLE = "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT";
	private static final String SCHEMA_COLOR = "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT";
	private static final String SCHEMA_RECORDS = "id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, weight REAL, tag_id INTEGER DEFAULT 0, time DATETIME DEFAULT(datetime('now', 'localtime'))";
	private static final String SCHEMA_TAGS = "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT";

	public PrivateUtils(Context context) {
		ResUtils.make(context);
		Configurations.make(context, context.getString(R.string.app_name));
		CONTEXT = context;
		TABLES_MAP = new HashMap<String, String>();
		TABLES_MAP.put("user", SCHEMA_USER);
		TABLES_MAP.put("style", SCHEMA_STYLE);
		TABLES_MAP.put("color", SCHEMA_COLOR);
		TABLES_MAP.put("records", SCHEMA_RECORDS);
		TABLES_MAP.put("tags", SCHEMA_TAGS);
		DB_UTILS = new SQLiteUtils();
		DB_NAME = context.getString(R.string.app_name) + ".db";
		USER_LIST = new ArrayList<Map<String, String>>();
		USER_LIST.clear();
	}

	public static void openDB() {
		DB_UTILS.open(CONTEXT, DB_NAME, TABLES_MAP, DB_VERSION);
	}

	public static void execSQL(String sql) {
		openDB();
		DB_UTILS.execSQL(sql);
		// [Neo] TODO 延迟一段时间关闭
	}

	public static Cursor selectDB(String sql) {
		openDB();
		return DB_UTILS.select(sql, null);
	}

	public static List<Map<String, String>> selectDB2list(String sql) {
		openDB();
		return DB_UTILS.select2list(sql, null);
	}

}
