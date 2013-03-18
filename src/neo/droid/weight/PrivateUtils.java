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
//	protected static int USER_LISTVIEW_HEIGHT;
	protected static Context CONTEXT;
	protected static SQLiteUtils DB_UTILS;
	private static boolean IS_DB_INIT = false;
	protected static List<Map<String, String>> USER_LIST;

	private static String DB_NAME;
	private static Map<String, String> TABLES_MAP;
	private static final int DB_VERSION = 5;
	private static final String SCHEMA_STYLE = "id INTEGER PRIMARY KEY, name TEXT, value TEXT";
	private static final String SCHEMA_COLOR = "id INTEGER PRIMARY KEY, name TEXT, value INTEGER";
	private static final String SCHEMA_USER = "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, sex INTEGER, style_id INTEGER, color_id INTEGER";
	private static final String SCHEMA_TAGS = "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, user_id INTEGER";
	private static final String SCHEMA_RECORDS = "id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, weight REAL, tag_id INTEGER DEFAULT 0, time DATETIME DEFAULT(datetime('now', 'localtime'))";

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

		if (false == IS_DB_INIT) {
			IS_DB_INIT = true;
			DB_UTILS.execSQL("INSERT OR IGNORE INTO tags(id, name, user_id) VALUES (0, '', 0)");

			// [Neo] TODO 替换方案和颜色
			DB_UTILS.execSQL("INSERT OR IGNORE INTO style(id, name, value) VALUES (1, '"
					+ CONTEXT.getString(R.string.standrad) + "', 'dummy')");

			DB_UTILS.execSQL("INSERT OR IGNORE INTO color(id, name, value) VALUES (1, '"
					+ CONTEXT.getString(R.string.orange)
					+ "', "
					+ 0xEE5500
					+ ")");
			DB_UTILS.execSQL("INSERT OR IGNORE INTO color(id, name, value) VALUES (2, '"
					+ CONTEXT.getString(R.string.purple)
					+ "', "
					+ 0xCC54EF
					+ ")");
			DB_UTILS.execSQL("INSERT OR IGNORE INTO color(id, name, value) VALUES (3, '"
					+ CONTEXT.getString(R.string.green)
					+ "', "
					+ 0x66DD00
					+ ")");
		}
	}

	public static void execSQL(String sql) {
		openDB();
		DB_UTILS.execSQL(sql);
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
