package edu.calpoly.cpe409.fridgereminder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ScannerActivity extends Activity implements SurfaceHolder.Callback {
	private static final int DIALOG_OCR_RESULT = 20;
	private static final int ADD_ITEMS_BATCH = 21;
	private static final int DIALOG_OCR_HELP = 30;
	private static final int DIALOG_UNSUPPORTED = 40;
	private static final String TAG = "ScannerActivity";

	// Change the contrast of a bitmap
	private static Bitmap adjustContrast(Bitmap d, float contrast) {
		Bitmap bitmap = d.copy(Bitmap.Config.ARGB_8888, true);
		float r, g, b;

		if (contrast >= 0)
			contrast += 1;
		else
			contrast -= 1;

		int oldPixel;
		for (int x = 0; x < bitmap.getWidth(); x++)
			for (int y = 0; y < bitmap.getHeight(); y++) {
				oldPixel = bitmap.getPixel(x, y);

				b = Color.blue(oldPixel) / 255f;
				b -= 0.5f;
				b *= contrast;
				b += 0.5f;
				b *= 255;
				if (b > 255)
					b = 255;
				else if (b < 0)
					b = 0;

				g = Color.green(oldPixel) / 255f;
				g -= 0.5f;
				g *= contrast;
				g += 0.5f;
				g *= 255;
				if (g > 255)
					g = 255;
				else if (g < 0)
					g = 0;

				r = Color.red(oldPixel) / 255f;
				r -= 0.5f;
				r *= contrast;
				r += 0.5f;
				r *= 255;
				if (r > 255)
					r = 255;
				else if (r < 0)
					r = 0;

				int newPixel = Color.rgb(Math.round(r), Math.round(g),
						Math.round(b));
				bitmap.setPixel(x, y, newPixel);
			}

		return bitmap;
	}

	// Workaround taken from:
	// http://code.google.com/p/android/issues/detail?id=823
	static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & (yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	private static Bitmap grayscale(Bitmap colorBitmap) {
		Bitmap grayscaleBitmap = Bitmap.createBitmap(colorBitmap.getWidth(),
				colorBitmap.getHeight(), Bitmap.Config.RGB_565);

		Canvas c = new Canvas(grayscaleBitmap);
		Paint p = new Paint();
		ColorMatrix cm = new ColorMatrix();

		cm.setSaturation(0);
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
		p.setColorFilter(filter);
		c.drawBitmap(colorBitmap, 0, 0, p);

		return grayscaleBitmap;
	}

	private ArrayList<String> itemsToAdd;
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private boolean doingAutoFocus;
	private TextView statusText;
	private WeOCRClient client;
	private boolean doingOCR;
	private HUDView hudView;

	private String ocrResultText;

	private Button submitButton;

	private void doProcess() {
		if (!doingAutoFocus) {
			doingAutoFocus = true;
			statusText.setText(getResources().getString(
					R.string.scanner_focus_status_text));

			mCamera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					Log.e(this.getClass().getCanonicalName(), "AutoFocus: "
							+ success);
					requestPreviewFrame();
					doingAutoFocus = false;
				}
			});
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case (ADD_ITEMS_BATCH):
			if (resultCode == Activity.RESULT_OK) {

				setResult(RESULT_OK, data);
				finish();

			}
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		doingOCR = false;

		// Hide the window title.
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Inflate layout
		setContentView(R.layout.scanner_view);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.SurfaceView);
		mHolder = surfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		surfaceView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				doProcess();
				return true;
			}
		});

		doingAutoFocus = false;
		statusText = (TextView) findViewById(R.id.HUDStatusText);
		statusText.setText(getResources().getString(
				R.string.scanner_default_text));
		hudView = (HUDView) findViewById(R.id.HUD);
		submitButton = (Button) findViewById(R.id.ScanSubmitButton);
		client = new WeOCRClient(
		// getResources().getString(R.string.scanner_ocr_url_2));
				getResources().getString(R.string.scanner_ocr_url_1));

		submitButton.setVisibility(View.GONE);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						AddItemActivity.class);
				intent.setAction(AddItemActivity.ACTION_ADD_NEW_ITEM_BATCH);
				intent.putExtra(AddItemActivity.DATA_BATCH_ITEM, itemsToAdd);
				itemsToAdd = null;
				submitButton.setVisibility(View.GONE);
				startActivityForResult(intent, ADD_ITEMS_BATCH);

			}
		});

		showDialog(DIALOG_OCR_HELP);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog toReturn = null;

		switch (id) {
		case DIALOG_OCR_RESULT: {
			// toReturn = new OCRDialog(this);
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(getResources().getString(R.string.scanner_result_text));
			ab.setPositiveButton("Accept",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (itemsToAdd == null)
								itemsToAdd = new ArrayList<String>();

							itemsToAdd.add(ocrResultText);
							submitButton.setVisibility(View.VISIBLE);
						}
					});
			ab.setNegativeButton("Reject",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Do nothing
						}
					});

			ab.setMessage("");
			toReturn = ab.create();
			break;
		}
		case DIALOG_OCR_HELP: {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("Scanner Instructions");
			ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing
				}
			});
			ab.setMessage(R.string.dialog_scan_instructions);
			toReturn = ab.create();
			break;
		}
		case DIALOG_UNSUPPORTED: {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("Oops!");
			ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setResult(RESULT_CANCELED);
					finish();
				}
			});
			ab.setCancelable(false);
			ab.setMessage(R.string.dialog_unsupported_device);
			toReturn = ab.create();

			break;
		}
		}

		return toReturn;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean toReturn = super.onKeyDown(keyCode, event);
		int action = event.getAction();

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			toReturn = toReturn || true;
			if (action == KeyEvent.ACTION_DOWN)
				hudView.setViewFinderWidth(-1);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			toReturn = toReturn || true;
			if (action == KeyEvent.ACTION_DOWN)
				hudView.setViewFinderWidth(1);
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			toReturn = toReturn || true;
			if (action == KeyEvent.ACTION_DOWN)
				doProcess();
			break;
		}

		return toReturn;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_OCR_RESULT:
			AlertDialog ad = (AlertDialog) dialog;
			ad.setMessage(ocrResultText);
			break;
		}
	}

	private void requestPreviewFrame() {
		if (!doingOCR) {
			doingOCR = true;
			statusText.setText(getResources().getString(
					R.string.scanner_doing_ocr_text));
			mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					Camera.Parameters params = mCamera.getParameters();
					Size size = params.getPreviewSize();

					int[] decoded = new int[size.height * size.width];
					decodeYUV420SP(decoded, data, size.width, size.height);

					// Try to improve OCR accuracy by grayscaling, cropping, and
					// increasing contrast
					Bitmap colorBitmap = Bitmap.createBitmap(decoded,
							size.width, size.height, Bitmap.Config.RGB_565);
					Rect rect = hudView.getFocusRect();

					double scaleX = (double) size.width
							/ mHolder.getSurfaceFrame().width();
					double scaleY = (double) size.height
							/ mHolder.getSurfaceFrame().height();

					int scaledLeft = (int) Math.round(rect.left * scaleX);
					int scaledRight = (int) Math.round(rect.right * scaleX);
					int scaledTop = (int) Math.round(rect.top * scaleY);
					int scaledBottom = (int) Math.round(rect.bottom * scaleY);

					Bitmap croppedBitmap = Bitmap.createBitmap(colorBitmap,
							scaledLeft, scaledTop, scaledRight - scaledLeft,
							scaledBottom - scaledTop);

					Bitmap grayBitmap = grayscale(croppedBitmap);
					Bitmap adjustedBitmap = adjustContrast(grayBitmap, 0.30f);

					try {
						FileOutputStream out = new FileOutputStream(
								"/sdcard/fridge_reminder_debug.jpg");
						adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 90,
								out);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						ocrResultText = client.doOCR(adjustedBitmap);
						statusText.setText(getResources().getString(
								R.string.scanner_default_text));
						showDialog(DIALOG_OCR_RESULT);

					} catch (IOException e) {
						e.printStackTrace();

						if (e instanceof UnknownHostException)
							statusText.setText(getResources().getString(
									R.string.scanner_ocr_error_host));
						else
							statusText.setText(getResources().getString(
									R.string.scanner_ocr_error_text));
					}
					doingOCR = false;
				}
			});
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.

		Log.d(TAG, "format=" + format + ", width=" + width + ", height="
				+ height);
		try {
			Camera.Parameters params = mCamera.getParameters();

			List<Size> sizes = params.getSupportedPreviewSizes();
			Size optimalSize = getOptimalPreviewSize(sizes, width, height);

			params.setPreviewSize(optimalSize.width, optimalSize.height);

			mCamera.setParameters(params);
			mCamera.startPreview();
		} catch (RuntimeException exc) {
			exc.printStackTrace();
			showDialog(DIALOG_UNSUPPORTED);
		}
		// requestAutoFocus();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();

		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}
}
