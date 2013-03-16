package neo.droid.weight;

import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChartActivity extends Activity {

	private TextView dayTextView, weekTextView, monthTextView;
	private LinearLayout chartLayout;
	private ChartView chartView;
	private TextView compareTextView;
	private MyHandler handler;

	private String formerInfos;
	private String dayPickedString;
	private boolean isCompareShowing;

	public static final int WHAT_CHECK_POINT = 0x01;
	public static final int WHAT_SHOW_COMPARE = 0x02;
	public static final int WHAT_RESTORE_INFOS = 0x03;
	public static final int WHAT_ERR_NO_DATA = 0x11;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chart);

		dayTextView = (TextView) findViewById(R.id.chart_day);
		weekTextView = (TextView) findViewById(R.id.chart_week);
		monthTextView = (TextView) findViewById(R.id.chart_month);
		chartLayout = (LinearLayout) findViewById(R.id.chart_view);
		compareTextView = (TextView) findViewById(R.id.chart_compare);

		handler = new MyHandler();
		chartView = new ChartView(ChartActivity.this, handler);
		chartLayout.removeAllViews();
		chartLayout.addView(chartView);

		dayTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Date date = new Date();
				Dialog dialog = new DatePickerDialog(ChartActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								dayPickedString = String.format(
										"%04d-%02d-%02d ", year,
										monthOfYear + 1, dayOfMonth);
								chartView.pickDate(dayPickedString);
							}
						}, date.getYear() + 1900, date.getMonth(), date
								.getDate());

				dialog.show();

			}
		});

		weekTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根

			}
		});

		monthTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根

			}
		});

		compareTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (false == isCompareShowing) {
					formerInfos = compareTextView.getText().toString();
					isCompareShowing = true;
					handler.sendEmptyMessageDelayed(WHAT_RESTORE_INFOS,
							9 * 1000);
					chartView.compare();
				}
			}
		});

	}

	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_CHECK_POINT:
				isCompareShowing = false;
				formerInfos = (String) msg.obj;
				compareTextView.setText(formerInfos);
				break;

			case WHAT_SHOW_COMPARE:
				compareTextView.setText((String) msg.obj);
				break;

			case WHAT_RESTORE_INFOS:
				isCompareShowing = false;
				compareTextView.setText(formerInfos);
				break;

			case WHAT_ERR_NO_DATA:
				Toast.makeText(ChartActivity.this,
						"No data on " + dayPickedString, Toast.LENGTH_LONG)
						.show();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	}
}
