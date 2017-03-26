/*******************************************************************************
 * Copyright (c) 2012 Curtis Larson (QuackWare).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package datapole.giftest.ui;


import java.io.File;
import java.io.FilenameFilter;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import datapole.giftest.HttpHandler;
import datapole.giftest.R;
import datapole.giftest.util.Compatibility;
import datapole.giftest.util.Donate;
import datapole.giftest.util.ErrorReporter;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// /mnt/sdcard/dcim/Camera/


//Fixed incorrect message from appearing
//Added Application Settings to choose various options such as Decode type
//Couple crash fixes

public class MainActivity extends Activity implements OnTouchListener {

    private static final int SELECT_VIDEO = 1;
    private static final int RECORD_VIDEO = 2;
    private static final int SELECT_GIF = 3;

    private static final String TAG = "MainActivity";

    private ProgressBar mainProgressBar;
    public String jsonUber;

    public String enteredString = "Wanna climb out of hell";
    String lyrics[] = new String[3];
    String song[] = new String[3];
    String artist[] = new String[3];
    String ytrl[] = new String[3];

    public String videoPath;
    DownloadManager manager;
    private long enqueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//        clipboard.setText("Text to copy");
        String temp = (String) clipboard.getText();
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == ' ') {
                enteredString += "%20";
            } else {
                enteredString += temp.charAt(i);
            }
        }

        new getMatches().execute();

//       ------------------>> ye vala code aayega on dowload ke result mn

//        Intent intent = new Intent(this, IntervalSelectorActivity.class);
//        intent.putExtra("videoPath", path);
//        startActivity(intent);

//          ----------------->>>>>>>>>>>>>>>>.

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long download = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = manager.query(query);
                    if (c.moveToFirst()) {
                        int coloumnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(coloumnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            videoPath = uriString;
                            //  --------------->>>>>>>>>>>>>>>>       THIS IS THE DOWNLOADED AUDIO URI       <<<<<<<------
                        }
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    public void onResume() {
        super.onResume();
        loadFileList();
    }

    private void initiateErrorReporter() {
        ErrorReporter report = new ErrorReporter();
        report.Init(this);
        report.CheckErrorAndSendMail(this);
    }

    private void checkForEnabledStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //We can read + write
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //We can only read
            Toast.makeText(this, ("PERMISSION FOR READ ONLY"), Toast.LENGTH_LONG).show();
        } else {
            //Can neither read nor write.
            Toast.makeText(this, ("NO READ OR WRITE PERMISSION"), Toast.LENGTH_LONG).show();
        }
    }

    private void setupButtonClickListeners() {
        //Button exitButton = (Button)findViewById(R.id.exit);
        //exitButton.setOnClickListener(this);

        ((Button) findViewById(R.id.selectVideo)).setOnTouchListener(this);
        ((Button) findViewById(R.id.recordVideo)).setOnTouchListener(this);
        ((Button) findViewById(R.id.viewGallery)).setOnTouchListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //Honestly at this point we can treat both of these as the same thing.
                case SELECT_VIDEO:
                case RECORD_VIDEO:
                    Uri videoUri = data.getData();
                    String path = getPath(videoUri);
                    //Log.i(TAG,path);
                    Log.d(TAG, "videoURL: " + videoUri);
                    Log.d(TAG, "path: " + path);
                    if (path != null) {
                        if (path.equals("")) {
                            Toast.makeText(this, "Error retriving path", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(this, "Error retriving path111111", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //Ok we have the path, thats all we really need so lets go ahead and pass it to CreatorActivity...
                    Intent intent = new Intent(this, IntervalSelectorActivity.class);
                    intent.putExtra("videoPath", path);
                    startActivity(intent);
                    break;
                case SELECT_GIF:
                    Uri gifUri = data.getData();
                    String gifPath = getPath(gifUri);
                    Log.i(TAG, "GifPath: " + gifPath);
                    Intent previewIntent = new Intent(this, PreviewActivity.class);
                    previewIntent.putExtra("gifPath", gifPath);
                    startActivity(previewIntent);
            }
        }
    }

    private String getPath(Uri uri) {
        //file:///mnt/sdcard/DCIM/Camera/VID_20111217_233451.mp4

        if (uri.toString().contains("content")) {
            try {
                String[] projection = {MediaColumns.DATA};
                Cursor cursor = managedQuery(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndex(MediaColumns.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } catch (Exception ex) {
                return null;
            }
        } else {
            return uri.toString();
        }
    }

    private void handleClickEvent(View v) {
        switch (v.getId()) {
            case R.id.viewGallery:
                loadFileList();
                if (mFileList.length > 0) {
                    Builder builder = new Builder(this);

                    builder.setTitle("Choose your file");
                    builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mChosenFile = mFileList[which];
                            Intent previewIntent = new Intent(MainActivity.this,
                                    PreviewActivity.class);
                            previewIntent.putExtra("gifPath", getApplicationContext()
                                    .getExternalFilesDir(null) + "/" + mChosenFile);
                            startActivity(previewIntent);
                        }
                    });
                    builder.create().show();

                } else {
                    Toast.makeText(this, "No GIFs in your gallery!", Toast.LENGTH_SHORT).show();
                }
                ///Intent viewIntent = new Intent();
                //viewIntent.setType("image/gif");
                //viewIntent.setAction(Intent.ACTION_PICK);
                //startActivityForResult(Intent.createChooser(viewIntent,"Select GIF"),SELECT_GIF);
                break;
            case R.id.selectVideo:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO);
                //image/*
                break;
            case R.id.recordVideo:
                Intent recordIntent = new Intent();
                recordIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                recordIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                //recordIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(recordIntent, RECORD_VIDEO);
                break;
        }
    }


    //#FFA500
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (v.getId()) {
                    case R.id.selectVideo:
                        ((Button) findViewById(R.id.selectVideo)).setBackgroundColor(0xFFFFA500);
                        break;
                    case R.id.recordVideo:
                        ((Button) findViewById(R.id.recordVideo)).setBackgroundColor(0xFFFFA500);
                        break;
                    case R.id.viewGallery:
                        ((Button) findViewById(R.id.viewGallery)).setBackgroundColor(0xFFFFA500);
                        break;
                }
                return true;
            case MotionEvent.ACTION_UP:
                switch (v.getId()) {
                    case R.id.selectVideo:
                        ((Button) findViewById(R.id.selectVideo)).setBackgroundColor(Color.BLACK);
                        handleClickEvent(v);
                        break;
                    case R.id.recordVideo:
                        ((Button) findViewById(R.id.recordVideo)).setBackgroundColor(Color.BLACK);
                        handleClickEvent(v);
                        break;
                    case R.id.viewGallery:
                        ((Button) findViewById(R.id.viewGallery)).setBackgroundColor(Color.BLACK);
                        handleClickEvent(v);
                        break;
                }

            default:
                return true;
        }
    }

    //In an Activity
    private String[] mFileList;
    private File mPath;
    private String mChosenFile;
    private static final String FTYPE = ".gif";
    private static final int DIALOG_LOAD_FILE = 1000;

    private void loadFileList() {

        try {
            mPath = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/");
            Log.i(TAG, "loadFileList() path: " + mPath.getAbsolutePath() + "/");
        } catch (Exception ex) {
            mPath = null;
            return;
        }
        try {
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card " + e.toString());
        }

        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(FTYPE) || sel.isDirectory();
                }
            };
            mFileList = mPath.list(filter);
        } else {
            mFileList = new String[0];
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settingsItem:
                Intent i = new Intent(MainActivity.this, ApplicationPreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder builder = new Builder(this);

        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                if (mFileList == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        Intent previewIntent = new Intent(MainActivity.this,
                                PreviewActivity.class);
                        previewIntent.putExtra("gifPath", getApplicationContext()
                                .getExternalFilesDir(null) + "/" + mChosenFile);
                        startActivity(previewIntent);
                    }
                });
                break;
        }
        dialog = builder.create();
        return dialog;
    }

    private void getYoutubeDownloadUrl(String youtubeLink) {
        new YouTubeExtractor(this) {

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                mainProgressBar.setVisibility(View.GONE);

                if (ytFiles == null) {
                    // Something went wrong we got no urls. Always check this.
                    finish();
                    return;
                }
                // Iterate over itags
                for (int i = 0, itag; i < ytFiles.size(); i++) {
                    itag = ytFiles.keyAt(i);
                    // ytFile represents one file with its url and meta data
                    YtFile ytFile = ytFiles.get(itag);

                    // Just add videos in a decent format => height -1 = audio
                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        addButtonToMainLayout(vMeta.getTitle(), ytFile);
                    }
                }
            }
        }.extract(youtubeLink, true, false);
    }

    private void addButtonToMainLayout(final String videoTitle, final YtFile ytfile) {
        // Display some buttons and let the user choose the format
        String btnText = (ytfile.getFormat().getHeight() == -1) ? "Audio " +
                ytfile.getFormat().getAudioBitrate() + " kbit/s" :
                ytfile.getFormat().getHeight() + "p";
        btnText += (ytfile.getFormat().isDashContainer()) ? " dash" : "";
        Button btn = new Button(this);
        btn.setText(btnText);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String filename;
                if (videoTitle.length() > 55) {
                    filename = videoTitle.substring(0, 55) + "." + ytfile.getFormat().getExt();
                } else {
                    filename = videoTitle + "." + ytfile.getFormat().getExt();
                }
                filename = filename.replaceAll("\\\\|>|<|\"|\\||\\*|\\?|%|:|#|/", "");
                downloadFromUrl(ytfile.getUrl(), videoTitle, filename);
                finish();
            }
        });
//        mainLayout.addView(btn);
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = manager.enqueue(request);
    }


    private class getMatches extends AsyncTask<Void, Void, Void> {
        HttpHandler sh = new HttpHandler();
        String reqUrl;
        String reqOlaUrl;
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Matching emotions...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            reqUrl = "http://192.168.2.207:5000/getMatch?sent=" + enteredString;     //enteredString;  // check  that last one could be of the format ->> sent=I%20wanna%20fuck%20you
//            reqOlaUrl = "https://devapi.olacabs.com/v1/products?pickup_lat=12.9491416&pickup_lng=77.64298&category=mini";
            jsonUber = sh.makeServiceCall(reqUrl);
//            jsonOla = sh.makeServiceCall(reqOlaUrl);

            Log.d(TAG, "jsonConcept" + jsonUber);

            try {
                JSONArray array = new JSONArray(jsonUber);
//                jsonObject = new JSONObject(jsonUber);

                for (int i = 0; i < 3; i++) {
                    JSONObject price = array.getJSONObject(i);
                    String x = price.getString("ytURL");
                    ytrl[i] = x;
                    String y = price.getString("lyric_match");
                    lyrics[i] = y;
                    String z = price.getString("song");
                    song[i] = z;
                    String z1 = price.getString("artist");
                    artist[i] = z1;

                    Log.d(TAG, "price:: " + price);
                    Log.d(TAG, "xxxx::: " + x);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();

            // run the below command three times with differnet callbacks

// ----------->>>>>>>>>>>>>>>>>>>>>>>>           getYoutubeDownloadUrl(youtubeLink);     // response mn aayega youtube link   <<<<<<<<<<--------------
            getYoutubeDownloadUrl(ytrl[0]);
            super.onPostExecute(aVoid);
        }
    }
}
