package neo.droid.weight;

import java.util.Date;

import neo.java.commons.Strings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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

		dayPickedString = Strings.getCurrentTimeString("yyyy-MM-dd");

		dayTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Date date = new Date();
				// [Neo] 我偷懒我自豪
				Dialog dialog = new DatePickerDialog(ChartActivity.this,
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								dayPickedString = String.format(
										"%04d-%02d-%02d", year,
										monthOfYear + 1, dayOfMonth);
								// [Neo] 修改图表日期
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

		// [Neo] 姑且叫他 compare 吧，现阶段就是用来显示更多信息用了
		compareTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (false == isCompareShowing) {
					formerInfos = compareTextView.getText().toString();
					isCompareShowing = true;
					// [Neo] 12s 后恢复显示的内容
					handler.sendEmptyMessageDelayed(WHAT_RESTORE_INFOS,
							12 * 1000);
					// [Neo] 生成详细内容
					chartView.compare();
				}
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			// [Neo] 移动至上一个点
			chartView.moveTarget(-1);
			return true;

		case KeyEvent.KEYCODE_VOLUME_DOWN:
			// [Neo] 移动至下一个点
			chartView.moveTarget(+1);
			return true;

		case KeyEvent.KEYCODE_DEL:
			new AlertDialog.Builder(ChartActivity.this)
					.setTitle(R.string.delete_me)
					.setMessage(R.string.delete_me_msg)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									compareTextView
											.setText(getString(R.string.infos));
									chartView.deleteTarget();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// [Neo] Empty

								}
							}).create().show();
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
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
				Toast.makeText(
						ChartActivity.this,
						String.format(getString(R.string.toast_without_data),
								dayPickedString), Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	}
}
