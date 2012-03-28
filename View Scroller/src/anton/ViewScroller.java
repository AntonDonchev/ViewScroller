package anton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class ViewScroller extends ViewGroup {
	private int currentScreen = 0;
	private Scroller scroller = null;

	protected static final int INVALID_SCREEN = -100;

	protected static final int DIRECTION_NEXT = 1;
	protected static final int DIRECTION_PREV = -1;

	public static final int SCROLL_SPEED_INSTANT = 0;
	public static final int SCROLL_SPEED_DEFAULT = 300;
	private ORIENTATION orientation = ORIENTATION.HORIZONTAL;
	private boolean mFirstLayout = true;
	private Point<Float> touchStartCoords = new Point<Float>();
	private Point<Float> touchCurrentCoords = new Point<Float>();
	private Point<Float> touchPrevCoords = new Point<Float>();
	private Point<Integer> touchScroll = new Point<Integer>();

	public enum ORIENTATION {
		HORIZONTAL, VERTICAL
	}

	public enum DIRECTION {
		NEXT, BACK
	}

	public class Point<T extends Number> {
		private T x;
		private T y;

		public Point() {
		}

		public Point(T x, T y) {
			this.x = x;
			this.y = y;
		}

		public Point(Point<T> p) {
			this.x = p.x;
			this.y = p.y;
		}
	}

	public ViewScroller(Context context) {
		super(context);
		init();
	}

	public ViewScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ViewScroller(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		scroller = new Scroller(getContext());
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childStart = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				switch (orientation) {
				case HORIZONTAL:
					final int childWidth = child.getMeasuredWidth();
					child.layout(childStart, 0, childStart + childWidth, child.getMeasuredHeight());
					childStart += childWidth;
					break;
				case VERTICAL:
					final int childHeight = child.getMeasuredHeight();
					child.layout(0, childStart, child.getMeasuredWidth(), childHeight + childStart);
					childStart += childHeight;
					break;
				}
			}
		}
		mFirstLayout = false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		int mHeight = Integer.MIN_VALUE;
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
			mHeight = Math.max(getChildAt(i).getMeasuredHeight(), mHeight);
		}

		if (mHeight != Integer.MIN_VALUE) {
			setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), resolveSize(mHeight, heightMeasureSpec));
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			setHorizontalScrollBarEnabled(false);
			int width = MeasureSpec.getSize(widthMeasureSpec);
			scrollTo(currentScreen * width, 0);
			setHorizontalScrollBarEnabled(true);
			mFirstLayout = false;
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public void scrollToScreen(int screen, int speed) {
		if (screen == INVALID_SCREEN) {
			return;
		}
		
		screen = Math.max(Math.min(screen, getChildCount() - 1), 0);
		
		if (screen == currentScreen) {
			return;
		}

		switch (orientation) {
		case HORIZONTAL:
			scroller.startScroll(getScrollX(), 0, getScrollDistanceX(screen), 0, speed);
			break;
		case VERTICAL:
			scroller.startScroll(0, getScrollY(), 0, getScrollDistanceY(screen), speed);
			break;
		}
		currentScreen = screen;
		invalidate();

	}

	protected int getScrollDistanceX(int screen) {
		if (orientation == ORIENTATION.VERTICAL) {
			return 0;
		}

		int screensToScroll = screen - currentScreen;
		int scrollDistance = screensToScroll * getWidth() - getScrollX();

		return scrollDistance;
	}

	protected int getScrollDistanceY(int screen) {
		if (orientation == ORIENTATION.HORIZONTAL) {
			return 0;
		}
		int screensToScroll = screen - currentScreen;
		int scrollDistance = screensToScroll * getHeight() - getScrollY();

		return scrollDistance;
	}

	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		}
	}

	protected int getScreenView(int screen) {
		int count = 0;
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i).getVisibility() != View.GONE) {
				if (screen == count) {
					return i;
				}
				count++;
			}
		}
		return INVALID_SCREEN;
	}

	protected int getNextScreen() {
		return getAdjacentScreen(DIRECTION.NEXT);
	}

	protected int getPrevScreen() {
		return getAdjacentScreen(DIRECTION.BACK);
	}

	private int getAdjacentScreen(DIRECTION direction) {
		int step = 1;
		switch (direction) {
		case BACK:
			if (currentScreen < 1) {
				return INVALID_SCREEN;
			}
			step = -1;
			break;
		case NEXT:
			if (currentScreen > getChildCount() - 2) {
				return INVALID_SCREEN;
			}
			step = 1;
			break;
		}
		for (int i = currentScreen + step; i < getChildCount(); i = i + step) {
			View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				return i;
			}
		}
		return INVALID_SCREEN;
	}

	public int getScreensCount() {
		int screenCount = 0;
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i).getVisibility() != View.GONE) {
				screenCount++;
			}
		}
		return screenCount;
	}

	public ORIENTATION getOrientation() {
		return orientation;
	}

	public void setOrientation(ORIENTATION orientation) {
		this.orientation = orientation;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			View child = getChildAt(currentScreen);
			switch (orientation) {
			case HORIZONTAL:

				if ((child != null) && (Math.abs(event.getX() - touchStartCoords.x) > getWidth() / 2)) {
					if (event.getX() > touchStartCoords.x) {
						scrollToScreen(getAdjacentScreen(DIRECTION.BACK), SCROLL_SPEED_DEFAULT);
					} else {
						scrollToScreen(getAdjacentScreen(DIRECTION.NEXT), SCROLL_SPEED_DEFAULT);
					}
				} else {
					scrollToScreen(currentScreen, SCROLL_SPEED_DEFAULT);
					return super.onTouchEvent(event);
				}
				break;
			case VERTICAL:
				if ((child != null) && (Math.abs(event.getY() - touchStartCoords.y) > getHeight() / 2)) {
					if (event.getY() > touchStartCoords.y) {
						scrollToScreen(getAdjacentScreen(DIRECTION.BACK), SCROLL_SPEED_DEFAULT);
					} else {
						scrollToScreen(getAdjacentScreen(DIRECTION.NEXT), SCROLL_SPEED_DEFAULT);
					}
				} else {
					scrollToScreen(currentScreen, SCROLL_SPEED_DEFAULT);
					return super.onTouchEvent(event);
				}
				break;

			}
			break;

		case MotionEvent.ACTION_DOWN:
			switch (orientation) {
			case HORIZONTAL:
				touchStartCoords.x = event.getX();
				touchPrevCoords.x = touchStartCoords.x;
				touchStartCoords.y = 0f;
				touchPrevCoords.y = 0f;
				touchScroll.x = getScrollX();
				touchScroll.y = 0;
				break;

			case VERTICAL:
				touchStartCoords.x = 0f;
				touchPrevCoords.x = 0f;
				touchStartCoords.y = event.getY();
				touchPrevCoords.y = touchStartCoords.y;
				touchScroll.y = getScrollY();
				touchScroll.x = 0;
				break;

			}
			makeAdjacentChildsVisible();
			break;

		case MotionEvent.ACTION_MOVE:
			switch (orientation) {
			case HORIZONTAL:
				touchCurrentCoords.x = event.getX();
				touchCurrentCoords.y = 0f;
				break;

			case VERTICAL:
				touchCurrentCoords.y = event.getY();
				touchCurrentCoords.x = 0f;
				break;
			}
			touchScroll();
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return true;
	}

	private void makeAdjacentChildsVisible() {
		int childIndex = getNextScreen();
		if (childIndex != INVALID_SCREEN) {
			getChildAt(childIndex).setVisibility(View.VISIBLE);
		}
		childIndex = getPrevScreen();
		if (childIndex != INVALID_SCREEN) {
			getChildAt(childIndex).setVisibility(View.VISIBLE);
		}
	}

	private void touchScroll() {
		int scrollStartX = getScrollX();
		int scrollStartY = getScrollY();
		int screenCount = getScreensCount();
		int scrolldX = Math.min(Math.max((int) (touchPrevCoords.x - touchCurrentCoords.x), 0), getWidth() * screenCount);
		int scrolldY = Math.min(Math.max((int) (touchPrevCoords.y - touchCurrentCoords.y), 0), getWidth() * screenCount);

		touchPrevCoords.x = touchCurrentCoords.x;
		touchPrevCoords.y = touchCurrentCoords.y;

		scroller.startScroll(scrollStartX, scrollStartY, scrolldX, scrolldY, 0);
		invalidate();
	}
}
