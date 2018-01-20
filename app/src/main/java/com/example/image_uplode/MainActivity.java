package com.example.image_uplode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class MainActivity extends ActionBarActivity {


	File finalFile;
	public static File mypath;

	static String responsestr, image_path;
	ImageView image_iv;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		image_iv=(ImageView)findViewById(R.id.image) ;
		imageUpload();

    }

	private void imageUpload()
	{
		final CharSequence[] items = {"Take Photo", "Choose from Sdcard", "Cancel"};
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Update Photo");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Take Photo")) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent, 0);

				} else if (items[item].equals("Choose from Sdcard"))
				{
					Intent in = new Intent(Intent.ACTION_PICK);
					in.setType("image/*");
					startActivityForResult(Intent.createChooser(in, "Complete action using"), 1);

				} else if (items[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}
    
    

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				Uri tempUri = getImageUri(getApplicationContext(), photo);  // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
				finalFile = new File(getRealPathFromURI(tempUri));    // CALL THIS METHOD TO GET THE ACTUAL PATH
				image_path = finalFile.getAbsolutePath();
				mypath = new File(image_path);
				Log.e("Image Path....", finalFile + "");

				try {
					new UploadData().execute();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "some error occurred during uploading image", Toast.LENGTH_SHORT).show();

				}

			}
		} else if (requestCode == 1 && resultCode == RESULT_OK) {

			Uri selectedImageUri = data.getData();
			image_path = getPath(selectedImageUri);
			mypath = new File(image_path);
			finalFile = new File(image_path);
			Log.e("Image Path....", finalFile + "");
			try
			{
				new UploadData().execute();

			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "some error occurred during uploading image", Toast.LENGTH_SHORT).show();

			}
		}
	}


	public Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 0, bytes);
		String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		return Uri.parse(path);
	}
	public String getRealPathFromURI(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}
	public String getPath(Uri uri) {
		String[] projection = {"_data"};
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow("_data");
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	private class UploadData extends AsyncTask<String, String, String> {
		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pd = new ProgressDialog(MainActivity.this);
			pd.setTitle("");
			pd.setMessage("Please wait a moment");
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Thread th = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					upload();
				}
			});
			th.start();
			try {
				th.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@SuppressWarnings("deprecation")
		private String upload()
		{
			try
			{
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://test.trukker.ae/trukkerUAEApitest/Api/driver/UploadOrderCompleteImage");

				MultipartEntity entity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);

				entity.addPart("driver_OrderCompleteImage", new FileBody(finalFile));
				entity.addPart("load_inquiry_no", new StringBody(""));
				httppost.setEntity(entity);

				// server call
				HttpResponse hResponse = httpClient.execute(httppost);
				HttpEntity hEntity = hResponse.getEntity();
				responsestr = EntityUtils.toString(hEntity);
				Log.e("Update_Propic_res->", responsestr);

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return responsestr;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try
			{
				responsestr = responsestr.trim();
				if (responsestr.length() > 0) {
					responsestr = responsestr.substring(1, responsestr.length() - 1);
					responsestr = responsestr.replace("\\", "");
				}
				else
				{
					responsestr = "";
				}
				Picasso.with(getApplicationContext()).load(responsestr).into(image_iv);

				//Toast.makeText(getApplicationContext(),responsestr+"",Toast.LENGTH_SHORT).show();
				Toast.makeText(getApplicationContext(),responsestr+"",Toast.LENGTH_SHORT).show();

			}
			catch (Exception e)
			{
			}
			if (pd.isShowing())
			{
				pd.dismiss();
			}
		}
	}

}
