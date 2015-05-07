package xavier.jorda.dailyselfie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity
{
    private AlarmManager mAlarmManager;
    private PendingIntent mSelfiePendingIntent;
    private Intent mSelfieNotificationIntent;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final File STORAGE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    private static final int THUMB_DIM = 100;
    private static final long TWO_MINUTES = 120 * 1000L;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private List<Selfie> selfiesList = new ArrayList<Selfie>();
    private final String TAG = "MainActivity";

    private static CustomListAdapter mCustomListAdapter;

    private ListView mSelfieListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        setContentView(R.layout.activity_main);

        // Create new list adapter
        getSelfies(selfiesList);
        mCustomListAdapter = new CustomListAdapter(this, R.layout.selfie_list_item, R.id.dailyselfieText, selfiesList);
        mCustomListAdapter.setNotifyOnChange(true);

        mSelfieListView = (ListView)findViewById(R.id.listView);

        mSelfieListView.setAdapter(mCustomListAdapter);

        mSelfieListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView <?> parent, View view, int position,long arg3)
            {
                view.setSelected(true);
                Log.d("selfieList item clicked"," item : "+position+" clicked");

                Intent fullSizeSelfie = new Intent(Intent.ACTION_VIEW);
                fullSizeSelfie.setDataAndType(Uri.parse("file://" + selfiesList.get(position).getSelfiePath()), "image/*");
                startActivity(fullSizeSelfie);


            }
        });

        mSelfieListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                Selfie selfie = selfiesList.get(i);
                File selfieFile = new File(selfie.getSelfiePath());

                if (selfieFile.exists()) {
                    String toastMsg = selfieFile.delete() ? "Successfully deleted " + selfie.getSelfieName() : "Delete failed";
                    Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
                    selfiesList.remove(selfie);
                    mCustomListAdapter.notifyDataSetChanged();
                    return true;
                }

                return false;
            }
        });

        createPendingIntents();

        createSelfieReminders();
    }

    private void createSelfieReminders() {
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Broadcast the notification intent at specified intervals
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP
                , System.currentTimeMillis() + TWO_MINUTES
                , TWO_MINUTES
                , mSelfiePendingIntent);

    }

    private void createPendingIntents() {
        // Create the notification pending intent
        mSelfieNotificationIntent = new Intent(MainActivity.this, SelfieNotificationReceiver.class);
        mSelfiePendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, mSelfieNotificationIntent, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
         if (id == R.id.action_camera)
        {
            Log.d("onOptionItemSelected","actionCamera pressed");
            takePicture();

        }

        return super.onOptionsItemSelected(item);
    }

    private void takePicture()
    {
        Log.i(TAG, "Opening the camera to take picture");

        // Dispatch take picture intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check to see if there is an application available to handle capturing images.
        // This is required else if no camera handling application is found, the Selfie app will crash
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the file where the photo should be saved to
            File selfieFile = null;

            try
            {
                selfieFile = createImageFile();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error creating selfie file", e);
            }

            if (selfieFile != null)
            {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(selfieFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            //update selfieList with the new selfie files;
            getSelfies(selfiesList);

            // tell the adapter to refresh the listView with the new selfies.
            ((BaseAdapter) mSelfieListView.getAdapter()).notifyDataSetChanged();
        }
    }

    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String imageFileName = "Selfie_" + timeStamp + "_";

        Log.i(TAG, "imageFileName => "+imageFileName);
        Log.i(TAG, "storageDir => "+STORAGE_DIR);

        STORAGE_DIR.mkdirs();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                STORAGE_DIR      /* directory */
        );
        Log.i(TAG, "fileDir=:" + image.getAbsolutePath());

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private void getSelfies(List<Selfie> selfieList)
    {
        Log.d(TAG,"getSelfies");

        if (selfieList != null && STORAGE_DIR.exists())
        {
            // For now we are starting from fresh and rebuilding the list.
            selfieList.clear();
            Log.i(TAG, "Storage directory exists!!");

            SelfieFileFilter fileFiler = new SelfieFileFilter();

            if(fileFiler!=null)
            {
                Log.i(TAG,"FileFilter is not NULL");
            }

            File[] fileList = STORAGE_DIR.listFiles(new SelfieFileFilter());

            int length = fileList.length;

            Log.i(TAG, "Files in directory are ==>"+length);

            for (File file : STORAGE_DIR.listFiles(new SelfieFileFilter()))
            {
                selfieList.add(
                        new Selfie(
                                file.getName()
                                , file.getAbsolutePath()
                                , getSelfieThumbnail(file.getAbsolutePath())
                        )
                );
            }
        }
        else
        {
            Log.i(TAG, "getSelfies - Storage directory DOESN'T exists OR null !!");
        }
    }

    private Bitmap getSelfieThumbnail(String photoPath)
    {
        Log.i(TAG, "Getting selfie thumbnails");
        // Get the dimensions of the View
        int targetW = THUMB_DIM;
        int targetH = THUMB_DIM;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    private class SelfieFileFilter implements FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            return file.getName().contains("Selfie_");
        }
    }
}
