package neo.droid.weight;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChartActivity extends Activity {

	private TextView dayTextView, weekTextView, monthTextView;
	private LinearLayout chartLayout;
	private ChartView chartView;
	private TextView compareTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chart);

		dayTextView = (TextView) findViewById(R.id.chart_day);
		weekTextView = (TextView) findViewById(R.id.chart_week);
		monthTextView = (TextView) findViewById(R.id.chart_month);
		chartLayout = (LinearLayout) findViewById(R.id.chart_view);
		compareTextView = (TextView) findViewById(R.id.chart_compare);

		// [Neo] TODO
		chartView = new ChartView(ChartActivity.this);
		chartLayout.removeAllViews();
		chartLayout.addView(chartView);

		dayTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// [Neo] TODO

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
				// TODO 自动生成的方法存根

			}
		});

	}

}
