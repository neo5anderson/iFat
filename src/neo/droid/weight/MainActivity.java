package neo.droid.weight;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ListView listView;
	private SimpleAdapter adapter;

	private TextView createTextView, hintTextView;
	private EditText createUserName, createSex, createStyle, createColor;

	private MyHandler handler;
	private static final int WHAT_PROFILE_WITH_USER = 0x01;
	private static final int WHAT_PROFILE_WITHOUT_USER = 0x02;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.profile_user_listview);
		hintTextView = (TextView) findViewById(R.id.profile_without_user);
		// [Neo] TODO ImageButton
		createTextView = (TextView) findViewById(R.id.profile_create);

		handler = new MyHandler();
		new PrivateUtils(MainActivity.this);
		PrivateUtils.openDB();
		new CheckDB().start();

		adapter = new SimpleAdapter(MainActivity.this, PrivateUtils.USER_LIST,
				R.layout.listview_user_profile, new String[] { "name" },
				new int[] { R.id.profile_users });
		listView.setAdapter(adapter);
		listView.setCacheColorHint(0);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(MainActivity.this,
						UserActivity.class);
				intent.putExtra("name", arg2);
				startActivity(intent);
			}

		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				final String username = PrivateUtils.USER_LIST.get(arg2).get(
						"name");
				AlertDialog.Builder aBuilder = new AlertDialog.Builder(
						MainActivity.this);
				aBuilder.setTitle(R.string.delete);
				aBuilder.setMessage(String.format(
						getString(R.string.profile_delete_msg_formatter),
						username));

				aBuilder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								PrivateUtils
										.execSQL("DELETE FROM user WHERE name='"
												+ username + "'");
								new CheckDB().start();
							}
						});

				aBuilder.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// [Neo] Empty

							}
						});

				aBuilder.create().show();

				return true;
			}

		});

		createTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder aBuilder = new AlertDialog.Builder(
						MainActivity.this);
				aBuilder.setTitle(R.string.create);
				LayoutInflater inflater = LayoutInflater
						.from(MainActivity.this);
				View view = inflater.inflate(R.layout.dialog_create_profile,
						null);
				createUserName = (EditText) view
						.findViewById(R.id.create_username);
				createSex = (EditText) view.findViewById(R.id.create_sex);
				createStyle = (EditText) view.findViewById(R.id.create_style);
				createColor = (EditText) view.findViewById(R.id.create_color);

				aBuilder.setView(view);

				aBuilder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								PrivateUtils
										.execSQL("INSERT INTO user(name, sex, style_id, color_id) VALUES ('"
												+ createUserName.getText()
														.toString()
												+ "', '"
												+ createSex.getText()
														.toString()
												+ "', 1, 1)");
								// [Neo] TODO
								createStyle.toString();
								createColor.toString();
								new CheckDB().start();
							}
						});

				aBuilder.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// [Neo] Empty

							}
						});

				aBuilder.create().show();
			}
		});

	}

	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_PROFILE_WITHOUT_USER:
				listView.setVisibility(View.GONE);
				hintTextView.setVisibility(View.VISIBLE);
				break;

			case WHAT_PROFILE_WITH_USER:
				if (PrivateUtils.USER_LIST.size() > 0) {
					View item = adapter.getView(0, null, listView);
					item.measure(0, 0);
					PrivateUtils.USER_LISTVIEW_HEIGHT = item
							.getMeasuredHeight();

					ViewGroup.LayoutParams params = listView.getLayoutParams();
					params.height = PrivateUtils.USER_LISTVIEW_HEIGHT
							* Math.min(PrivateUtils.USER_LIST.size(), 3);
					listView.setLayoutParams(params);
				}

				listView.setVisibility(View.VISIBLE);
				hintTextView.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
				break;

			default:
				break;

			}
			super.handleMessage(msg);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// [Neo] TODO 菜单
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class CheckDB extends Thread {

		@Override
		public void run() {
			// [Neo] TODO 信息的选择
			Cursor cursor = PrivateUtils.selectDB("select name from user");
			PrivateUtils.USER_LIST.clear();
			if (cursor.getCount() > 0) {
				for (cursor.moveToFirst(); false == cursor.isAfterLast(); cursor
						.moveToNext()) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", cursor.getString(0));
					PrivateUtils.USER_LIST.add(map);
				}
				handler.sendEmptyMessage(WHAT_PROFILE_WITH_USER);
			} else {
				handler.sendEmptyMessage(WHAT_PROFILE_WITHOUT_USER);
			}

			cursor.close();
			PrivateUtils.DB_UTILS.close();
		}
	}

}
