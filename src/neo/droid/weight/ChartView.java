package neo.droid.weight;

import java.util.List;
import java.util.Map;

import neo.java.commons.Strings;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

public class ChartView extends View {

	private int chartRange;
	private Paint chartLinePaint, chartTitlePaint, fingerInPaint,
			fingerOutPaint, checkOutPaint, dummyPaint;

	private Point currentPos, fingerPos;
	private boolean isShowFinger, isCancelAutoHide, isCached, isErrShowed;
	private List<Map<String, String>> list;
	private int index;
	private float fingerOutR;

	private MyHanlder hanlder;
	private Handler layoutHandler;

	private static final int WHAT_INVALIDATE = 0x01;
	private static final int WHAT_AUTO_HIDE_FINGER = 0x51;

	public static final int RANGE_DAY = 0x01;
	public static final int RANGE_WEEK = 0x02;
	public static final int RANGE_MONTH = 0x03;

	private final float chartLeftFactor = 0.1f;
	private final float chartRightFactor = 0.95f;
	private final float chartTopFactor = 0.1f;
	private final float chartBottomFactor = 0.9f;

	private final float chartArrowWidthFactor = 0.5f;
	private final float chartArrowHeightFactor = 0.85f;

	private final float chartVTitleXFactor = 1.2f;
	private final float chartVTitleYFactor = 0.8f;

	private final float chartHTitleXFactor = 1.8f;
	private final float chartHTitleYFactor = 1.6f;

	private final float chartXHeightFactor = 0.5f;

	private float chartLeftPix = 0;
	private float chartTopPix = 0;
	private float chartRightPix = 0;
	private float chartBottomPix = 0;

	private float[] weights;
	private float maxWeight, minWeight, avgWeight;
	private String dateString;

	public ChartView(Context context, Handler handler) {
		super(context);
		layoutHandler = handler;

		currentPos = new Point();
		fingerPos = new Point();
		hanlder = new MyHanlder();

		chartLinePaint = new Paint();
		chartLinePaint.setAntiAlias(true);
		chartLinePaint.setSubpixelText(true);
		chartLinePaint.setStrokeWidth(1);
		chartLinePaint.setColor(Color.LTGRAY);

		fingerInPaint = new Paint();
		fingerInPaint.setAntiAlias(true);
		fingerInPaint.setSubpixelText(true);
		fingerInPaint.setColor(0xFF44C2FF);

		fingerOutPaint = new Paint();
		fingerOutPaint.setAntiAlias(true);
		fingerOutPaint.setSubpixelText(true);
		fingerOutPaint.setColor(0xC000ACFF);

		checkOutPaint = new Paint();
		checkOutPaint.setAntiAlias(true);
		checkOutPaint.setSubpixelText(true);
		checkOutPaint.setColor(0xFFFF4644);

		chartTitlePaint = new Paint();
		chartTitlePaint.setAntiAlias(true);
		chartTitlePaint.setSubpixelText(true);
		chartTitlePaint.setTextSize(12);
		chartTitlePaint.setColor(Color.LTGRAY);

		dummyPaint = new Paint();
		dummyPaint.setAntiAlias(true);
		dummyPaint.setSubpixelText(true);
		dummyPaint.setStrokeWidth(1);
		dummyPaint.setColor(Color.RED);

		dateString = Strings.getCurrentTimeString("yyyy-MM-dd ");
		switchRange(RANGE_DAY);
	}

	public void switchRange(int range) {
		isCached = false;
		isShowFinger = false;
		isErrShowed = false;
		index = -1;

		boolean isValiade = true;

		switch (range) {
		case RANGE_DAY:
			break;

		case RANGE_WEEK:
			break;

		case RANGE_MONTH:
			break;

		default:
			isValiade = false;
			break;
		}

		if (false != isValiade) {
			chartRange = range;
		} else {
			chartRange = RANGE_DAY;
		}

		invalidate();
	}

	public void pickDate(String dateString) {
		this.dateString = dateString;
		switchRange(RANGE_DAY);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int height = getHeight();
		int width = getWidth();

		// [Neo] 计算实际图表边界坐标
		chartLeftPix = (chartLeftPix > 1) ? chartLeftPix : (Math.max(width
				* chartLeftFactor, 10));
		chartRightPix = (chartRightPix > 1) ? chartRightPix : (Math.min(width
				* chartRightFactor, width - 5));
		chartTopPix = (chartTopPix > 1) ? chartTopPix : (Math.max(height
				* chartTopFactor, 10));
		chartBottomPix = (chartBottomPix > 1) ? chartBottomPix : (Math.min(
				height * chartBottomFactor, height - 10));

		fingerOutR = Math.min(width - chartRightPix, height - chartBottomPix);
		float arrowWidth = fingerOutR * chartArrowWidthFactor;
		float arrowHeight = fingerOutR * chartArrowHeightFactor;

		// [Neo] 绘制图表边界
		canvas.drawLines(new float[] { chartLeftPix, chartTopPix, chartLeftPix,
				chartBottomPix, chartLeftPix, chartBottomPix, chartRightPix,
				chartBottomPix, chartRightPix - arrowHeight,
				chartBottomPix - arrowWidth, chartRightPix, chartBottomPix,
				chartRightPix, chartBottomPix, chartRightPix - arrowHeight,
				chartBottomPix + arrowWidth, chartLeftPix - arrowWidth,
				chartTopPix + arrowHeight, chartLeftPix, chartTopPix,
				chartLeftPix, chartTopPix, chartLeftPix + arrowWidth,
				chartTopPix + arrowHeight, }, chartLinePaint);

		switch (chartRange) {
		case RANGE_DAY:
			canvas.drawText("hours", width - chartLeftPix * chartHTitleXFactor,
					height - chartTopPix * chartHTitleYFactor, chartTitlePaint);
			float perHLength = (chartRightPix - chartLeftPix) / 27.0f;
			for (int i = 2; i < 26; i++) {
				canvas.drawLine(chartLeftPix + perHLength * i, chartBottomPix
						- perHLength * chartXHeightFactor, chartLeftPix
						+ perHLength * i, chartBottomPix, chartLinePaint);
			}

			canvas.drawText("12a", chartLeftPix + perHLength * 1, height
					- perHLength, chartTitlePaint);
			canvas.drawText("12p", chartLeftPix + perHLength * 13, height
					- perHLength, chartTitlePaint);
			canvas.drawText("11p", chartLeftPix + perHLength * 24, height
					- perHLength, chartTitlePaint);

			canvas.drawText("weight", chartLeftPix * chartVTitleXFactor,
					chartTopPix * chartVTitleYFactor, chartTitlePaint);
			float perVLength = (chartBottomPix - chartTopPix) / 10.0f;
			for (int i = 1; i < 10; i++) {
				canvas.drawLine(chartLeftPix, chartTopPix + perVLength * i,
						chartLeftPix + 5, chartTopPix + perVLength * i,
						chartLinePaint);
			}

			if (null == list || false == isCached) {
				list = PrivateUtils
						.selectDB2list("SELECT records.weight as weight, (strftime('%s', records.time) - strftime('%s', '"
								+ dateString
								+ "') ) AS time, tags.name as tag FROM records INNER JOIN tags ON records.tag_id = tags.id WHERE records.time BETWEEN '"
								+ dateString
								+ "00:00:00' AND '"
								+ dateString
								+ "23:59:59' ORDER BY records.time ASC");
				isCached = true;
			}

			// [Neo] TODO
			if (null != list) {
				weights = new float[list.size()];
				int[] times = new int[list.size()];

				maxWeight = Float.parseFloat(list.get(0).get("weight"));
				minWeight = maxWeight;
				avgWeight = maxWeight;

				for (int i = 0; i < list.size(); i++) {
					weights[i] = Float.parseFloat(list.get(i).get("weight"));
					times[i] = Integer.parseInt(list.get(i).get("time"));

					avgWeight += weights[i];

					if (maxWeight < weights[i]) {
						maxWeight = weights[i];
					}
					if (minWeight > weights[i]) {
						minWeight = weights[i];
					}
				}

				avgWeight /= list.size();

				int maxW = (int) Math.ceil(maxWeight);
				int minW = (int) Math.floor(minWeight);

				canvas.drawText(String.format("%d.0", maxW), 0, chartTopPix
						+ perVLength * 1.2f, chartTitlePaint);
				canvas.drawText(
						String.format("%.1f", (maxW - minW) / 2.0f + minW), 0,
						chartTopPix + perVLength * 5.2f, chartTitlePaint);
				canvas.drawText(String.format("%d.0", minW), 0, chartTopPix
						+ perVLength * 9.2f, chartTitlePaint);

				if (false != isShowFinger
						&& currentPos.x > chartLeftPix + perHLength * 2
						&& currentPos.x < chartLeftPix + perHLength * 26) {
					int currentTime = (int) ((currentPos.x - chartLeftPix - perHLength * 2)
							/ perHLength * 3600);
					int minLength = Math.abs(currentTime - times[0]);
					index = 0;
					for (int i = 1; i < times.length; i++) {
						if (minLength > Math.abs(currentTime - times[i])) {
							index = i;
							minLength = Math.abs(currentTime - times[i]);
						}
					}

					layoutHandler
							.sendMessage(layoutHandler.obtainMessage(
									ChartActivity.WHAT_CHECK_POINT,
									0,
									0,
									Strings.getFormattedTimeString(
											times[index], "HH:mm:ss")
											+ " - "
											+ weights[index] + "kg"));
				}

				for (int i = 0; i < list.size(); i++) {
					Paint paint = fingerInPaint;
					float dotx = chartLeftPix + perHLength
							* (2 + times[i] / 3600.0f);
					float doty = chartTopPix
							+ perVLength
							* (1 + 8 * (1 - (weights[i] - minW) / (maxW - minW)));

					if (i == index) {
						paint = checkOutPaint;
						float y = 0;
						if (doty < height / 3) {
							y = doty + perVLength;
						} else {
							y = doty - perVLength * 0.5f;
						}
						canvas.drawText(list.get(i).get("tag"), dotx
								- perHLength, y, chartTitlePaint);
					}

					canvas.drawCircle(dotx, doty, fingerOutR / 3, paint);
				}
			} else {
				if (false == isErrShowed) {
					isErrShowed = true;
					layoutHandler
							.sendEmptyMessage(ChartActivity.WHAT_ERR_NO_DATA);
				}
			}

			break;

		case RANGE_WEEK:
			break;

		case RANGE_MONTH:
			break;

		default:
			break;
		}

		// [Neo] TODO 绘制手指
		if (false != isShowFinger) {
			canvas.drawCircle(fingerPos.x, fingerPos.y, fingerOutR,
					fingerOutPaint);
			canvas.drawCircle(fingerPos.x, fingerPos.y,
					Math.max(fingerOutR / 4, 2), fingerInPaint);
		}

		PrivateUtils.DB_UTILS.close();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			// redrawFinger(false);
			currentPos.set((int) event.getX(), (int) event.getY());
			if (currentPos.x < chartLeftPix) {
				fingerPos.x = (int) chartLeftPix;
			} else if (currentPos.x > chartRightPix) {
				fingerPos.x = (int) chartRightPix;
			} else {
				fingerPos.x = currentPos.x;
			}

			if (currentPos.y < chartTopPix) {
				fingerPos.y = (int) chartTopPix;
			} else if (currentPos.y > chartBottomPix) {
				fingerPos.y = (int) chartBottomPix;
			} else {
				fingerPos.y = currentPos.y;
			}

			isCancelAutoHide = true;
			redrawFinger(true);
			break;

		case MotionEvent.ACTION_UP:
			isCancelAutoHide = false;
			hanlder.sendEmptyMessageDelayed(WHAT_AUTO_HIDE_FINGER, 1 * 1000);
			break;

		default:
			System.out.println("other " + event.getX() + ", " + event.getY());
			break;
		}

		return true;
	}

	public void compare() {
		if (null != weights && weights.length > 0) {
			layoutHandler.sendMessage(layoutHandler.obtainMessage(
					ChartActivity.WHAT_SHOW_COMPARE, 0, 0, String.format(
							"Max: %.1fkg, Min: %.1fkg, AVG: %.1fkg", maxWeight,
							minWeight, avgWeight)));
		}
	}

	private void redrawFinger(boolean isShowFinger) {
		this.isShowFinger = isShowFinger;
		invalidate();
		// [Neo] TODO 局部刷新的效率也未必很高
		// int dirtyR = (int) Math.ceil(fingerOutR);
		// invalidate(fingerPos.x - dirtyR, fingerPos.y - dirtyR, fingerPos.x
		// + dirtyR, fingerPos.y + dirtyR);
	}

	class MyHanlder extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_INVALIDATE:
				invalidate();
				break;

			case WHAT_AUTO_HIDE_FINGER:
				if (false == isCancelAutoHide) {
					redrawFinger(false);
				}
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	}

}
