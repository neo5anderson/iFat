package neo.droid.weight;

import neo.java.commons.Strings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserActivity extends Activity {

	private TextView infotTextView;
	private TextView addTextView, compareTextView, exportTextView,
			settingsTextView;

	private int currentUserID;

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

		currentUserID = getIntent().getIntExtra("name", 0);
		infotTextView.setText(String.format(
				getString(R.string.user_info_formatter), PrivateUtils.USER_LIST
						.get(currentUserID).get("name")));

		addTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder aBuilder = new AlertDialog.Builder(
						UserActivity.this);
				aBuilder.setTitle(R.string.user_add);

				LayoutInflater inflater = LayoutInflater
						.from(UserActivity.this);
				View view = inflater.inflate(R.layout.dialog_add_records, null);
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
								// [Neo] TODO tags
								String tagString = tagEditText.getText()
										.toString();
								if (false == Strings.isEmpty(tagString)) {
									PrivateUtils
											.execSQL("INSERT INTO tags(name) VALUES ('"
													+ tagEditText.getText()
													+ "')");
								}
								
								PrivateUtils
										.execSQL("INSERT INTO records(user_id, weight, tag_id) VALUES ("
												+ (currentUserID + 1)
												+ ", "
												+ weightEditText.getText()
												+ ", last_insert_rowid())");

								PrivateUtils.DB_UTILS.close();
								// [Neo] TODO
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
				// [Neo] TODO
				startActivity(new Intent(UserActivity.this, ChartActivity.class));
			}
		});

		exportTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				Toast.makeText(UserActivity.this,
						getString(R.string.not_available), Toast.LENGTH_LONG)
						.show();
			}
		});

		settingsTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				Toast.makeText(UserActivity.this,
						getString(R.string.not_available), Toast.LENGTH_LONG)
						.show();
			}
		});

	}
}
