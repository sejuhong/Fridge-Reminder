package edu.calpoly.cpe409.fridgereminder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class HUDView extends View {

	private static final String TAG = HUDView.class.getSimpleName();

	private static final float OUTER_FRACTION = 0.125f; // 1/8th
	private static final float INNER_FRACTION = 0.083f; // 1/12th
	private static final float WIDTH_FRACTION = 0.300f; // 1/4th

	private Paint mPaint;
	private Rect mRect;
	private Rect mFocusRect;
	private int mDarkMaskColor;
	private int mLightMaskColor;
	private int mCrosshairsColor;
	private int mExtentColor;
	private float viewFinderWidthPercentage = 0.15f;

	private int canvasWidth;

	private int canvasHeight;

	public HUDView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		mPaint = new Paint();
		mRect = new Rect();
		mFocusRect = new Rect();
		Resources resources = getResources();
		mDarkMaskColor = resources.getColor(R.color.guide_mask_dark);
		mLightMaskColor = resources.getColor(R.color.guide_mask_light);
		mCrosshairsColor = resources.getColor(R.color.guide_crosshairs);
		mExtentColor = resources.getColor(R.color.guide_extent);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Copy into local variables for efficiency
		final Paint paint = mPaint;
		final Rect rect = mRect;
		final Rect frect = mFocusRect;

		canvasWidth = canvas.getWidth();
		canvasHeight = canvas.getHeight();

		int maskHeight = Math.round((1.0f - OUTER_FRACTION) * canvasHeight
				/ 2.0f);
		int guideGap = Math.round((OUTER_FRACTION - INNER_FRACTION)
				* canvasHeight / 2.0f);
		int maskWidth = Math
				.round((1.0f - WIDTH_FRACTION) * canvasWidth / 2.0f);

		// Draw the exterior (i.e. outside the framing guides) darkened
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(mDarkMaskColor);
		rect.set(0, 0, canvasWidth, maskHeight - 1);
		canvas.drawRect(rect, paint);
		rect.set(0, canvasHeight - maskHeight + 1, canvasWidth, canvasHeight);
		canvas.drawRect(rect, paint);

		// Draw lighter mask
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(mCrosshairsColor);
		paint.setStrokeWidth(2);
		// frect.set(0, maskHeight, width, height - maskHeight);
		updateViewFinder(frect, maskHeight);
		canvas.drawRect(frect, paint);
	}

	private void updateViewFinder(Rect frect, int maskHeight) {
		frect.set(
				(int) Math.round(canvasWidth * viewFinderWidthPercentage),
				maskHeight,
				(int) Math.round(canvasWidth - canvasWidth
						* viewFinderWidthPercentage), canvasHeight - maskHeight);
	}

	public Rect getFocusRect() {
		return mFocusRect;
	}

	public void setViewFinderRect(Rect extentRect) {
		Rect invalidateRect = mFocusRect;
		if (invalidateRect != null && extentRect != null) {
			invalidateRect.union(extentRect); // old rect will be discarded
												// anyway
		} else if (extentRect != null) {
			invalidateRect = new Rect(extentRect);
		}
		mFocusRect = extentRect;
		if (invalidateRect != null) {
			invalidateRect.inset(-2, -2);
			invalidate(invalidateRect);
		}
	}

	public void setViewFinderWidth(int i) {
		viewFinderWidthPercentage += i * 0.05f;

		if (viewFinderWidthPercentage > 0.4f)
			viewFinderWidthPercentage = 0.4f;
		else if (viewFinderWidthPercentage < 0.0f)
			viewFinderWidthPercentage = 0.0f;

		int maskHeight = Math.round((1.0f - OUTER_FRACTION) * canvasHeight
				/ 2.0f);
		
		Rect newViewFinder = new Rect();
		updateViewFinder(newViewFinder, maskHeight);
		setViewFinderRect(newViewFinder);
		Log.i("HUDView", "New width percent: " + viewFinderWidthPercentage);
	}

}
