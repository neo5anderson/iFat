package neo.droid.weight;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ChartView extends View {

	public ChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO 自动生成的构造函数存根
	}

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO 自动生成的构造函数存根
	}

	public ChartView(Context context) {
		super(context);
		// TODO 自动生成的构造函数存根
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO 自动生成的方法存根
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("down " + event.getX() + ", " + event.getY());
			return true;

		case MotionEvent.ACTION_UP:
			System.out.println("up " + event.getX() + ", " + event.getY());
			return true;

		case MotionEvent.ACTION_MOVE:
			System.out.println("moving " + event.getX() + ", " + event.getY());
			return true;

		default:
			System.out.println("other " + event.getX() + ", " + event.getY());
			break;
		}
		return super.onTouchEvent(event);
	}

}
