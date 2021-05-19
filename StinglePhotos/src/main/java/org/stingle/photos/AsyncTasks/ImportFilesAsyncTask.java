package org.stingle.photos.AsyncTasks;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import org.stingle.photos.Files.FileManager;
import org.stingle.photos.Files.ImportFile;
import org.stingle.photos.R;
import org.stingle.photos.Util.Helpers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImportFilesAsyncTask extends AsyncTask<Void, Integer, Boolean> {

	private WeakReference<AppCompatActivity> activity;
	private ArrayList<Uri> uris;
	private ArrayList<Uri> importedUris = new ArrayList<>();
	private int set;
	private String albumId;
	private FileManager.OnFinish onFinish;
	private ProgressDialog progress;
	private Long largestDate = 0L;


	public ImportFilesAsyncTask(AppCompatActivity activity, ArrayList<Uri> uris, int set, String albumId, FileManager.OnFinish onFinish){
		this.activity = new WeakReference<>(activity);
		this.uris = uris;
		this.set = set;
		this.albumId = albumId;
		this.onFinish = onFinish;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		AppCompatActivity myActivity = activity.get();
		if(myActivity == null){
			return;
		}
		progress = Helpers.showProgressDialogWithBar(myActivity, myActivity.getString(R.string.importing_files), null, uris.size(), null);
		Helpers.acquireWakeLock(myActivity);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		AppCompatActivity myActivity = activity.get();
		if(myActivity == null){
			return null;
		}
		int index = 0;

		boolean result = true;
		for (Uri uri : uris) {
			if(isCancelled()){
				break;
			}
			Long date = ImportFile.importFile(myActivity, uri, set, albumId, this);
			if(date == null){
				result = false;
			}
			else{
				importedUris.add(uri);
			}
			if(date != null && date > largestDate){
				largestDate = date;
			}

			publishProgress(index+1);
			index++;
		}

		return result;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		progress.setProgress(values[0]);

	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if(progress != null && progress.isShowing()) {
			progress.dismiss();
		}

		if(onFinish != null){
			onFinish.onFinish(largestDate);
		}
		AppCompatActivity myActivity = activity.get();
		if(myActivity != null){
			Helpers.releaseWakeLock(myActivity);
		}

		if(result) {
			deleteOriginals();
		}
	}

	private void deleteOriginals(){
		AppCompatActivity myActivity = activity.get();
		if(myActivity == null || importedUris.size() == 0){
			return;
		}
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(myActivity);
		String pref = settings.getString("delete_after_import", "ask");
		if(pref.equals("never")){
			return;
		}
		else if(pref.equals("ask")){
			Helpers.showConfirmDialog(
					myActivity,
					myActivity.getString(R.string.is_delete_original),
					myActivity.getString(R.string.is_delete_original_desc),
					R.drawable.ic_action_delete,
					(dialog, which) -> (new DeleteUrisAsyncTask(myActivity, importedUris, null)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR),
					null
			);
		}
		else if(pref.equals("always")){
			(new DeleteUrisAsyncTask(myActivity, importedUris, null)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

}
