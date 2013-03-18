package neo.droid.weight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neo.java.commons.Strings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView listView;
	private SimpleAdapter adapter;

	private TextView createTextView, hintTextView;
	private EditText createUserName;
	private Spinner createSex, createStyle, createColor;

	private MyHandler handler;
	private static final int WHAT_PROFILE_WITH_USER = 0x01;
	private static final int WHAT_PROFILE_WITHOUT_USER = 0x02;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.profile_user_listview);
		hintTextView = (TextView) findViewById(R.id.profile_without_user);
		// [Neo] TODO 期待好图像来替换文本
		createTextView = (TextView) findViewById(R.id.profile_create);

		handler = new MyHandler();
		// [Neo] 初始化各种所需的资源
		new PrivateUtils(MainActivity.this);
		// [Neo] 检查数据库里面的用户，要显示用的哦
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
				// [Neo] 把信息都搞到手，传递给下个界面
				Map<String, String> map = PrivateUtils
						.selectDB2list(
								"SELECT user.id AS id, user.name AS name, user.sex AS sex, style.value AS style, color.value AS color FROM user, style, color WHERE user.name = '"
										+ PrivateUtils.USER_LIST.get(arg2).get(
												"name")
										+ "' AND user.color_id = color.id AND user.style_id = style.id")
						.get(0);
				Intent intent = new Intent(MainActivity.this,
						UserActivity.class);
				intent.putExtra("id", Integer.parseInt(map.get("id")));
				intent.putExtra("name", map.get("name"));
				intent.putExtra("sex", Integer.parseInt(map.get("sex")));
				intent.putExtra("style", map.get("style"));
				intent.putExtra("color", Integer.parseInt(map.get("color")));

				// [Neo] 关闭数据库
				PrivateUtils.DB_UTILS.close();
				startActivity(intent);
			}

		});

		// [Neo] 长按删除用户信息
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
								// [Neo] 这么干的效率可能会有点低下
								int deleteUserId = Integer
										.parseInt(PrivateUtils
												.selectDB2list(
														"SELECT id FROM user WHERE name = '"
																+ username
																+ "'").get(0)
												.get("id"));
								PrivateUtils
										.execSQL("DELETE FROM user WHERE id = "
												+ deleteUserId);
								PrivateUtils
										.execSQL("DELETE FROM tags WHERE user_id = "
												+ deleteUserId);
								PrivateUtils
										.execSQL("DELETE FROM records WHERE user_id = "
												+ deleteUserId);

								// [Neo] 重新检查数据库用户信息，更新界面，以及关闭数据库
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

				View view = LayoutInflater.from(MainActivity.this).inflate(
						R.layout.dialog_create_profile, null);
				createUserName = (EditText) view
						.findViewById(R.id.create_username);
				createSex = (Spinner) view.findViewById(R.id.create_sex);
				createStyle = (Spinner) view.findViewById(R.id.create_style);
				createColor = (Spinner) view.findViewById(R.id.create_color);

				// [Neo] 性别下拉菜单
				ArrayAdapter<String> sexAdapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_spinner_item, new String[] {
								getString(R.string.female),
								getString(R.string.male) });
				sexAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				createSex.setAdapter(sexAdapter);

				// [Neo] 类别下拉菜单
				List<Map<String, String>> list = PrivateUtils
						.selectDB2list("SELECT name FROM style");
				String[] styleStrings = null;
				if (null != list) {
					styleStrings = new String[list.size()];
					for (int i = 0; i < list.size(); i++) {
						styleStrings[i] = list.get(i).get("name");
					}
				} else {
					styleStrings = new String[] { "" };
				}

				ArrayAdapter<String> styleAdapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_spinner_item, styleStrings);
				styleAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				createStyle.setAdapter(styleAdapter);

				// [Neo] 颜色下拉菜单
				list = PrivateUtils.selectDB2list("SELECT name FROM color");
				String[] colorStrings = null;
				if (null != list) {
					colorStrings = new String[list.size()];
					for (int i = 0; i < list.size(); i++) {
						colorStrings[i] = list.get(i).get("name");
					}
				} else {
					colorStrings = new String[] { "" };
				}

				ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_spinner_item, colorStrings);
				colorAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				createColor.setAdapter(colorAdapter);

				// [Neo] 关闭数据库哦
				PrivateUtils.DB_UTILS.close();
				aBuilder.setView(view);

				aBuilder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String username = createUserName.getText()
										.toString().trim();
								if (false == Strings.isEmpty(username)) {
									PrivateUtils.execSQL("INSERT INTO user(name, sex, style_id, color_id) VALUES ('"
											+ username
											+ "', "
											+ createSex
													.getSelectedItemPosition()
											+ ", "
											+ +(createStyle
													.getSelectedItemPosition() + 1)
											+ ", "
											+ (createColor
													.getSelectedItemPosition() + 1)
											+ ")");
									new CheckDB().start();
								} else {
									Toast.makeText(
											MainActivity.this,
											getString(R.string.toast_empty_username),
											Toast.LENGTH_LONG).show();
								}
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
				// [Neo] 动态修改用户表格的尺寸
				if (PrivateUtils.USER_LIST.size() > 0) {
					View item = adapter.getView(0, null, listView);
					item.measure(0, 0);

					ViewGroup.LayoutParams params = listView.getLayoutParams();
					params.height = item.getMeasuredHeight()
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
			// [Neo] TODO 是否有必要一次都查出来，以后都访问静态变量
			List<Map<String, String>> list = PrivateUtils
					.selectDB2list("SELECT name FROM user");
			PrivateUtils.USER_LIST.clear();
			if (null != list && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", list.get(i).get("name"));
					PrivateUtils.USER_LIST.add(map);
				}
				handler.sendEmptyMessage(WHAT_PROFILE_WITH_USER);
			} else {
				handler.sendEmptyMessage(WHAT_PROFILE_WITHOUT_USER);
			}

			PrivateUtils.DB_UTILS.close();
		}
	}

}
