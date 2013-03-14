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
	private Paint chartLinePaint, titlePaint, fingerInPaint, fingerOutPaint,
			dummyPaint;

	private Point currentPos, fingerPos;
	private boolean isShowFinger, isCancelAutoHide;
	private float fingerOutR;

	private MyHanlder hanlder;

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

	private float chartLeftPix = 0;
	private float chartTopPix = 0;
	private float chartRightPix = 0;
	private float chartBottomPix = 0;

	public ChartView(Context context) {
		super(context);

		currentPos = new Point();
		fingerPos = new Point();

		hanlder = new MyHanlder();
		isShowFinger = false;
		chartRange = RANGE_DAY;

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

		titlePaint = new Paint();
		titlePaint.setAntiAlias(true);
		titlePaint.setSubpixelText(true);
		titlePaint.setTextSize(12);
		titlePaint.setColor(Color.LTGRAY);

		dummyPaint = new Paint();
		dummyPaint.setAntiAlias(true);
		dummyPaint.setSubpixelText(true);
		dummyPaint.setStrokeWidth(1);
		dummyPaint.setColor(Color.RED);

	}

	public void switchRange(int range) {
		boolean isValiade = true;

		// [Neo] TODO
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
			canvas.drawText("weight", chartLeftPix * chartVTitleXFactor,
					chartTopPix * chartVTitleYFactor, titlePaint);
			canvas.drawText("hours", width - chartLeftPix * chartHTitleXFactor,
					height - chartTopPix * chartHTitleYFactor, titlePaint);

			String dateString = Strings.getCurrentTimeString("yyyy-MM-dd ");

			List<Map<String, String>> list = PrivateUtils
					.selectDB2list("SELECT records.weight, tags.name FROM records INNER JOIN tags ON records.tag_id = tags.id WHERE records.time BETWEEN '"
							+ dateString
							+ "00:00:00' AND '"
							+ dateString
							+ "23:59:59'");

			for (int i = 0; i < list.size(); i++) {
				Strings.sysoutMaps(list.get(i));
			}
			// [Neo] TODO

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
					Math.max(fingerOutR / 4.0f, 2), fingerInPaint);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
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
			isShowFinger = true;
			isCancelAutoHide = true;
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
			isCancelAutoHide = false;
			hanlder.sendEmptyMessageDelayed(WHAT_AUTO_HIDE_FINGER, 1 * 1000);
			break;

		default:
			System.out.println("other " + event.getX() + ", " + event.getY());
			break;
		}

		// [Neo] TODO
		return true;
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
					isShowFinger = false;
					invalidate();
				}
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	}

}
