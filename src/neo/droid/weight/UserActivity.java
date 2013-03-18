package neo.droid.weight;

import java.util.List;
import java.util.Map;

import neo.java.commons.Strings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class UserActivity extends Activity {

	private TextView infotTextView;
	private TextView addTextView, compareTextView, exportTextView,
			settingsTextView;

	protected static int CURRENT_USER_ID;
	protected static int CURRENT_SEX;
	protected static String CURRENT_STYLE;
	protected static int CURRENT_COLOR;

	private int styleIndex = 0, colorIndex = 0;

	private TextView addDateTextView;
	private EditText weightEditText, tagEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);

		infotTextView = (TextView) findViewById(R.id.user_info);
		addTextView = (TextView) findViewById(R.id.user_add);
		compareTextView = (TextView) findViewById(R.id.user_compare);
		exportTextView = (TextView) findViewById(R.id.user_export);
		settingsTextView = (TextView) findViewById(R.id.user_settings);

		Intent intent = getIntent();
		infotTextView.setText(String.format(
				getString(R.string.user_info_formatter),
				intent.getStringExtra("name")));

		CURRENT_USER_ID = intent.getIntExtra("id", 0);
		CURRENT_SEX = intent.getIntExtra("sex", 1);
		CURRENT_STYLE = intent.getStringExtra("style");
		// [Neo] TODO alpha settings here
		CURRENT_COLOR = intent.getIntExtra("color", 0) + 0xFF000000;

		addTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder aBuilder = new AlertDialog.Builder(
						UserActivity.this);
				aBuilder.setTitle(R.string.user_add);

				View view = LayoutInflater.from(UserActivity.this).inflate(
						R.layout.dialog_add_records, null);
				addDateTextView = (TextView) view.findViewById(R.id.add_date);
				weightEditText = (EditText) view.findViewById(R.id.add_weight);
				tagEditText = (EditText) view.findViewById(R.id.add_tags);

				addDateTextView.setText(String.format(
						getString(R.string.add_date_formatter),
						Strings.getCurrentTimeString("yyyy-MM-dd hh:mm")));

				aBuilder.setView(view);
				aBuilder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// [Neo] 要对两个编辑框中的数据负责
								String dummyString = weightEditText.getText()
										.toString();
								if (false != Strings.isEmpty(dummyString)) {
									Toast.makeText(
											UserActivity.this,
											getString(R.string.err_without_weight),
											Toast.LENGTH_LONG).show();
									return;
								}

								dummyString = tagEditText.getText().toString()
										.trim();
								if (false == Strings.isEmpty(dummyString)) {
									PrivateUtils
											.execSQL("INSERT INTO tags(name, user_id) VALUES ('"
													+ dummyString
													+ "', "
													+ CURRENT_USER_ID + ")");
								}

								// [Neo] a little trick: last_insert_rowid()
								PrivateUtils
										.execSQL("INSERT INTO records(user_id, weight, tag_id) VALUES ("
												+ CURRENT_USER_ID
												+ ", "
												+ weightEditText.getText()
												+ ", last_insert_rowid())");

								PrivateUtils.DB_UTILS.close();
								startActivity(new Intent(UserActivity.this,
										ChartActivity.class));
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

		compareTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// [Neo] TODO 姑且直接看图表吧
				startActivity(new Intent(UserActivity.this, ChartActivity.class));
			}
		});

		exportTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// [Neo] TODO
				Toast.makeText(UserActivity.this,
						getString(R.string.not_available), Toast.LENGTH_LONG)
						.show();
			}
		});

		settingsTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final List<Map<String, String>> styleList, colorList;
				styleList = PrivateUtils.selectDB2list("SELECT * FROM style");
				colorList = PrivateUtils.selectDB2list("SELECT * FROM color");
				PrivateUtils.DB_UTILS.close();

				String[] styleStrings, colorStrings;

				styleStrings = new String[styleList.size()];
				for (int i = 0; i < styleList.size(); i++) {
					styleStrings[i] = styleList.get(i).get("name");
					if (styleList.get(i).get("value").equals(CURRENT_STYLE)) {
						styleIndex = i;
					}
				}

				colorStrings = new String[colorList.size()];
				for (int i = 0; i < colorList.size(); i++) {
					colorStrings[i] = colorList.get(i).get("name");
					if (Integer.parseInt(colorList.get(i).get("value")) + 0xFF000000 == CURRENT_COLOR) {
						colorIndex = i;
					}
				}

				AlertDialog.Builder aBuilder = new AlertDialog.Builder(
						UserActivity.this);
				aBuilder.setTitle(R.string.user_settings);

				View view = LayoutInflater.from(UserActivity.this).inflate(
						R.layout.dialog_user_settings, null);
				final Spinner styleSpinner = (Spinner) view
						.findViewById(R.id.settings_style);
				final Spinner colorSpinner = (Spinner) view
						.findViewById(R.id.settings_color);

				ArrayAdapter<String> styleAdapter = new ArrayAdapter<String>(
						UserActivity.this,
						android.R.layout.simple_spinner_item, styleStrings);
				styleAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				styleSpinner.setAdapter(styleAdapter);

				ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(
						UserActivity.this,
						android.R.layout.simple_spinner_item, colorStrings);
				colorAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				colorSpinner.setAdapter(colorAdapter);

				styleSpinner.setSelection(styleIndex);
				colorSpinner.setSelection(colorIndex);
				aBuilder.setView(view);

				aBuilder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								styleIndex = styleSpinner
										.getSelectedItemPosition();
								colorIndex = colorSpinner
										.getSelectedItemPosition();

								PrivateUtils
										.execSQL("UPDATE user SET style_id = "
												+ styleList.get(styleIndex)
														.get("id")
												+ ", color_id = "
												+ colorList.get(colorIndex)
														.get("id")
												+ " WHERE id = "
												+ CURRENT_USER_ID);

								PrivateUtils.DB_UTILS.close();
								CURRENT_STYLE = styleList.get(styleIndex).get(
										"value");
								CURRENT_COLOR = Integer.parseInt(colorList.get(
										colorIndex).get("value")) + 0xFF000000;
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
}
