package edu.calpoly.cpe409.fridgereminder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

//Adapted from Wordsnap-OCR (Spiros Papadimitriou <spapadim@cs.cmu.edu>)
public class WeOCRFormEntity extends AbstractHttpEntity {
	private static final String BOUNDARY = "--------------GfHioqf1adDgeQwHF2fp9"; // monkey-typed
																					// random
																					// string
	private static final String CONTENT_TYPE = "multipart/form-data; boundary="
			+ BOUNDARY;

	private static final String BODY_HEADER = "--"
			+ BOUNDARY
			+ "\r\n"
			+ "Content-Disposition: form-data; name=\"userfile\"; filename=\"text.jpg\"\r\n"
			+ "Content-Type: image/jpeg\r\n"
			+ "Content-Transfer-Encoding: binary\r\n" + "\r\n";
	private static final String BODY_TRAILER = "\r\n" + "--" + BOUNDARY
			+ "\r\n"
			+ "Content-Disposition: form-data; name=\"outputformat\"\r\n"
			+ "\r\n" + "txt\r\n" + "--" + BOUNDARY + "\r\n"
			+ "Content-Disposition: form-data; name=\"outputencoding\"\r\n"
			+ "\r\n" + "utf-8\r\n" + "--" + BOUNDARY + "--\r\n";

	private static final int STREAM_BUFFER_SIZE = 2560;
	private ByteArrayOutputStream mImageStream;

	public WeOCRFormEntity(Bitmap img) throws IOException {
		this(img, 90);
	}

	public WeOCRFormEntity(Bitmap img, int quality) throws IOException {
		// Write compressed image to memory; we need the content length
		ByteArrayOutputStream imageStream = new ByteArrayOutputStream(
				STREAM_BUFFER_SIZE);
		img.compress(CompressFormat.JPEG, quality, imageStream);
		imageStream.close();

		mImageStream = imageStream;
		setContentType(CONTENT_TYPE);
		setChunked(false);
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		throw new UnsupportedOperationException(
				"WeOCRFormEntity does not support getContent()");
	}

	@Override
	public long getContentLength() {
		return mImageStream.size() + BODY_HEADER.length()
				+ BODY_TRAILER.length();
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public void writeTo(OutputStream os) throws IOException {
		if (os == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		os.write(BODY_HEADER.getBytes("ascii"));
		mImageStream.writeTo(os);
		os.write(BODY_TRAILER.getBytes("ascii"));
		os.flush();
	}

}
