package neo.droid.weight;

import java.util.List;
import java.util.Map;

import neo.java.commons.Strings;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

public class ChartView extends View {

	// [Neo] 检出点索引
	private int index;

	// [Neo] 手指外圈半径
	private float fingerOutR;

	// [Neo] 存储体重的各种数值
	private float[] weights;
	private float maxWeight, minWeight, avgWeight;

	// [Neo] 时间值
	private String dateString;

	// private Path path = new Path();

	// [Neo] 各种画笔
	private Paint chartLinePaint, chartTitlePaint, fingerInPaint,
			fingerOutPaint, checkOutPaint, dotPaint, dashPaint;

	// [Neo] 手指点击位置、绘制手指点击位置
	private Point currentPos, fingerPos;
	private boolean isShowFinger, isCancelAutoHide, isCached, isErrShowed;

	// [Neo] 查询结果集
	private List<Map<String, String>> list;

	private Handler layoutHandler;

	private MyHanlder hanlder;
	private static final int WHAT_INVALIDATE = 0x01;
	private static final int WHAT_AUTO_HIDE_FINGER = 0x51;

	// [Neo] 图表显示范围
	private int chartRange;
	public static final int RANGE_DAY = 0x01;
	public static final int RANGE_WEEK = 0x02;
	public static final int RANGE_MONTH = 0x03;

	// [Neo] 各种位置变量因子
	private final float chartLeftFactor = 0.07f;
	private final float chartRightFactor = 0.93f;
	private final float chartTopFactor = 0.07f;
	private final float chartBottomFactor = 0.93f;

	private final float chartArrowWidthFactor = 0.35f;
	private final float chartArrowHeightFactor = 0.6f;

	private final float chartVTitleXFactor = 1.2f;
	private final float chartVTitleYFactor = 0.8f;

	private final float chartTitleSizeFactor = 1.65f;
	private final float chartHTitleXFactor = 3f;
	private final float chartHTitleYFactor = 1.6f;

	private final float chartXHeightFactor = 0.2f;
	private final float chartYWidthFactor = 0.2f;

	// [Neo] 实际表格坐标基数
	private float chartLeftPix = 0;
	private float chartTopPix = 0;
	private float chartRightPix = 0;
	private float chartBottomPix = 0;

	public ChartView(Context context, Handler handler) {
		super(context);
		layoutHandler = handler;

		currentPos = new Point();
		fingerPos = new Point();
		hanlder = new MyHanlder();

		chartLinePaint = new Paint();
		chartLinePaint.setAntiAlias(true);
		chartLinePaint.setSubpixelText(true);
		chartLinePaint.setColor(Color.DKGRAY);

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
		checkOutPaint.setColor(Color.RED);

		chartTitlePaint = new Paint();
		chartTitlePaint.setAntiAlias(true);
		chartTitlePaint.setSubpixelText(true);
		chartTitlePaint.setColor(Color.LTGRAY);

		dotPaint = new Paint();
		dotPaint.setAntiAlias(true);
		dotPaint.setSubpixelText(true);
		dotPaint.setColor(UserActivity.CURRENT_COLOR);

		dashPaint = new Paint();
		dashPaint.setStyle(Paint.Style.STROKE);
		dashPaint.setColor(Color.LTGRAY);
		dashPaint.setPathEffect(new DashPathEffect(new float[] { 8, 5, 8, 5 },
				1));

		switchRange(RANGE_DAY);
		dateString = Strings.getCurrentTimeString("yyyy-MM-dd");
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

	public void moveTarget(int offset) {
		if (null != list) {
			index = (list.size() + index + offset) % list.size();
			invalidate();
		}
	}

	public boolean deleteTarget() {
		if (index > -1) {
			PrivateUtils.execSQL("DELETE FROM records WHERE id = "
					+ list.get(index).get("id"));
			switchRange(RANGE_DAY);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int height = getHeight();
		int width = getWidth();

		// [Neo] 计算实际图表边界坐标
		chartLeftPix = (chartLeftPix > 1) ? chartLeftPix : (Math.max(width
				* chartLeftFactor, 25));
		chartRightPix = (chartRightPix > 1) ? chartRightPix : (Math.min(width
				* chartRightFactor, width - 10));
		chartTopPix = (chartTopPix > 1) ? chartTopPix : (Math.max(height
				* chartTopFactor, 20));
		chartBottomPix = (chartBottomPix > 1) ? chartBottomPix : (Math.min(
				height * chartBottomFactor, height - 20));

		// [Neo] 计算手指半径、箭头长宽
		fingerOutR = Math.min(width - chartRightPix, height - chartBottomPix);
		float arrowWidth = fingerOutR * chartArrowWidthFactor;
		float arrowHeight = fingerOutR * chartArrowHeightFactor;

		// [Neo] 指定线宽
		chartLinePaint.setStrokeWidth(fingerOutR / 10);
		dashPaint.setStrokeWidth(fingerOutR / 10);

		// [Neo] 绘制图表边界
		canvas.drawLines(new float[] { chartLeftPix, chartTopPix, chartLeftPix,
				chartBottomPix + fingerOutR / 8, chartLeftPix, chartBottomPix,
				chartRightPix, chartBottomPix, chartRightPix - arrowHeight,
				chartBottomPix - arrowWidth, chartRightPix, chartBottomPix,
				chartRightPix, chartBottomPix, chartRightPix - arrowHeight,
				chartBottomPix + arrowWidth, chartLeftPix - arrowWidth,
				chartTopPix + arrowHeight, chartLeftPix, chartTopPix,
				chartLeftPix, chartTopPix, chartLeftPix + arrowWidth,
				chartTopPix + arrowHeight, }, chartLinePaint);

		switch (chartRange) {
		case RANGE_DAY:
			// [Neo] 静态 24 小时横坐标
			float perHLength = (chartRightPix - chartLeftPix) / 27.0f;
			chartTitlePaint.setTextSize(perHLength * chartTitleSizeFactor);
			canvas.drawText("hours", chartRightPix - perHLength
					* chartHTitleXFactor, height - chartTopPix
					* chartHTitleYFactor, chartTitlePaint);
			for (int i = 2; i < 26; i++) {
				canvas.drawLine(chartLeftPix + perHLength * i, chartBottomPix
						- chartTopPix * chartXHeightFactor, chartLeftPix
						+ perHLength * i, chartBottomPix, chartLinePaint);
			}

			canvas.drawText("12a", chartLeftPix + perHLength * 1, height
					- perHLength, chartTitlePaint);
			canvas.drawText("12p", chartLeftPix + perHLength * 13, height
					- perHLength, chartTitlePaint);
			canvas.drawText("11p", chartLeftPix + perHLength * 24, height
					- perHLength, chartTitlePaint);

			// [Neo] 纵坐标
			float perVLength = (chartBottomPix - chartTopPix) / 10.0f;
			canvas.drawText("weight", chartLeftPix * chartVTitleXFactor,
					chartTopPix * chartVTitleYFactor, chartTitlePaint);
			for (int i = 1; i < 10; i++) {
				canvas.drawLine(chartLeftPix, chartTopPix + perVLength * i,
						chartLeftPix * (1 + chartYWidthFactor), chartTopPix
								+ perVLength * i, chartLinePaint);
			}

			// [Neo] 多画三条线，哈
			canvas.drawLines(new float[] { chartLeftPix,
					chartTopPix + perVLength, chartRightPix - perHLength,
					chartTopPix + perVLength, chartLeftPix,
					chartTopPix + perVLength * 5, chartRightPix - perHLength,
					chartTopPix + perVLength * 5, chartLeftPix,
					chartTopPix + perVLength * 9, chartRightPix - perHLength,
					chartTopPix + perVLength * 9, }, chartLinePaint);

			// [Neo] 是否要更新结果集
			if (null == list || false == isCached) {
				isCached = true;
				// [Neo] 这个 SQL 真心的长
				list = PrivateUtils
						.selectDB2list("SELECT records.id AS id, records.weight as weight, (strftime('%s', records.time) - strftime('%s', '"
								+ dateString
								+ "')) AS time, tags.name as tag FROM records INNER JOIN tags ON records.tag_id = tags.id WHERE records.time BETWEEN '"
								+ dateString
								+ " 00:00:00' AND '"
								+ dateString
								+ " 23:59:59' "
								+ "AND records.user_id = "
								+ UserActivity.CURRENT_USER_ID
								+ " ORDER BY records.time ASC");
			}

			if (null != list) {
				weights = new float[list.size()];
				int[] times = new int[list.size()];

				avgWeight = 0;
				maxWeight = Float.parseFloat(list.get(0).get("weight"));
				minWeight = maxWeight;

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

				// [Neo] 确保最大值与最小值至少有个 1kg 的容差
				if (maxW == minW) {
					maxW += 1;
				}

				// [Neo] 绘制平均虚线以及各点所需的坐标变量
				float dotx, doty;
				doty = chartTopPix + perVLength
						* (1 + 8 * (1 - (avgWeight - minW) / (maxW - minW)));

				// [Neo] Path 用法举例，哈，reset 很重要
				// path.reset();
				// path.moveTo(chartLeftPix, doty);
				// path.lineTo(chartRightPix - perHLength, doty);
				// canvas.drawPath(path, dashPaint);

				canvas.drawLine(chartLeftPix, doty, chartRightPix - perHLength,
						doty, dashPaint);
				canvas.drawText(String.format("AVG: %.1f", avgWeight),
						chartRightPix - perHLength * 7, doty - perHLength,
						chartTitlePaint);

				// [Neo] 绘制纵坐标数值
				canvas.drawText(String.format("%d.0", maxW), 0, chartTopPix
						+ perVLength * 1.2f, chartTitlePaint);
				canvas.drawText(
						String.format("%.1f", (maxW - minW) / 2.0f + minW), 0,
						chartTopPix + perVLength * 5.2f, chartTitlePaint);
				canvas.drawText(String.format("%d.0", minW), 0, chartTopPix
						+ perVLength * 9.2f, chartTitlePaint);

				// [Neo] 判断是否需要绘制检出点
				if (false != isShowFinger
						&& currentPos.x > chartLeftPix + perHLength * 2
						&& currentPos.x < chartLeftPix + perHLength * 26) {
					int currentTime = (int) ((currentPos.x - chartLeftPix - perHLength * 2)
							/ perHLength * 3600);
					int minLength = Math.abs(currentTime - times[0]);
					index = 0;
					// [Neo] 找到那个最近的有效点索引
					for (int i = 1; i < times.length; i++) {
						if (minLength > Math.abs(currentTime - times[i])) {
							index = i;
							minLength = Math.abs(currentTime - times[i]);
						}
					}
				}

				// [Neo] 如果有检出点，在界面上更新其测试时间和数值
				if (index > -1) {
					index = (list.size() + index) % list.size();
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

				// [Neo] 那么，就把记录都显示到图表上面吧
				for (int i = 0; i < list.size(); i++) {
					// [Neo] 坐标计算的代码稍微复杂了点，原理还是比较容易理解的
					dotx = chartLeftPix + perHLength * (2 + times[i] / 3600.0f);
					doty = chartTopPix
							+ perVLength
							* (1 + 8 * (1 - (weights[i] - minW) / (maxW - minW)));

					if (i != index) {
						// [Neo] 正常绘制这些记录点
						canvas.drawCircle(dotx, doty, fingerOutR / 3, dotPaint);
					}
				}

				// [Neo] 绘制检出点
				if (index > -1) {
					dotx = chartLeftPix + perHLength
							* (2 + times[index] / 3600.0f);
					doty = chartTopPix
							+ perVLength
							* (1 + 8 * (1 - (weights[index] - minW)
									/ (maxW - minW)));

					// [Neo] 绘制标签这里有技巧哦，高过多少要在下面输出
					float y = 0;
					if (doty < height / 3) {
						y = doty + perVLength;
					} else {
						y = doty - perVLength * 0.5f;
					}

					String tagString = list.get(index).get("tag");
					// [Neo] 检出点太靠右可能会影响标签显示，那就往左边来点
					int offset = (times[index] > 14 * 3600) ? (tagString
							.length() * 4 / 5) : (tagString.length() / 2);
					canvas.drawText(tagString, dotx - perHLength * offset, y,
							chartTitlePaint);
					canvas.drawCircle(dotx, doty, fingerOutR / 3, checkOutPaint);
				}

			} else {
				// [Neo] 告诉用户数据库里面没记录，要自己添加的，亲
				if (false == isErrShowed) {
					isErrShowed = true;
					layoutHandler
							.sendEmptyMessage(ChartActivity.WHAT_ERR_NO_DATA);
				}
			}

			break;

		case RANGE_WEEK:
			// [Neo] TODO
			break;

		case RANGE_MONTH:
			// [Neo] TODO
			break;

		default:
			break;
		}

		// [Neo] 手指圆点，个人比较喜欢这个颜色，尺寸还有待商榷
		if (false != isShowFinger) {
			canvas.drawCircle(fingerPos.x, fingerPos.y, fingerOutR,
					fingerOutPaint);
			canvas.drawCircle(fingerPos.x, fingerPos.y,
					Math.max(fingerOutR / 4, 2), fingerInPaint);
		}

		// [Neo] 关闭数据库，很有必要
		PrivateUtils.DB_UTILS.close();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			// [Neo] 发现局部更新并没有减少很多垃圾，瓶颈兴许不在这儿
			// redrawFinger(false);

			// [Neo] 除了绝对坐标外，手指圆圈的位置要稍微加工一下才会好看点
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

			// [Neo] a little trick
			isCancelAutoHide = true;
			redrawFinger(true);
			break;

		case MotionEvent.ACTION_UP:
			isCancelAutoHide = false;
			// [Neo] 延迟手指 1s 后消失，哈
			hanlder.sendEmptyMessageDelayed(WHAT_AUTO_HIDE_FINGER, 1 * 1000);
			break;

		default:
			// [Neo] 理论上不容易触发这个事件
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
		// [Neo] 局部刷新未必会提供整体的效率，暂时注释吧
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
