package com.perm.DoomPlay;


/*
 *    Copyright 2013 Vladislav Krot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    You can contact me <DoomPlaye@gmail.com>
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*
    DownloadingService - Controller
    Download - Model
    DownloadNotifBuilder - View
*/

public class DownloadingService extends Service implements Download.DoomObserver
{
    public static final String keyDownload = "downloadTrack";
    private NotificationManager manager;

    private Map<Long,DownloadHolder> downloads ;

    //private static boolean isUpdate = false;


    @Override
    public void doomUpdate(long aid)
    {
        DownloadHolder holder = downloads.get(aid);

        // TODO: i don't know why ,but if i fast click on notif (for 3 seconds) it's throw the nullPointerException
        if(holder == null || holder.download == null)
            return;

        Notification notification = null;

        switch (holder.download.getStatus())
        {
            case DOWNLOADING:
                notification = holder.downloadBuilder.createStarting();
                break;
            case CANCELLED:
                notification = holder.downloadBuilder.createCanceled();
                dispose(aid);
                break;
            case COMPLETED:
                notification = holder.downloadBuilder.createCompleted();
                dispose(aid);
                break;
            case PAUSED:
                notification = holder.downloadBuilder.createPaused();
                break;
        }

        manager.notify(holder.downloadBuilder.notificationId,notification);

    }

    @Override
    public void doomError(long aid, String message)
    {
        // TODO: i don't know why ,but if i fast click on notif (for 3 seconds) it's throw the nullPointerException
        DownloadHolder holder = downloads.get(aid);

        if(holder == null || holder.download == null)
            return;

        Notification notification = holder.downloadBuilder.createError(message);
        dispose(aid);

        manager.notify(holder.downloadBuilder.notificationId,notification);
    }


    private static class DownloadHolder
    {
        Download download;
        DownloadNotifBuilder downloadBuilder;
    }
    private void dispose(long aid)
    {
        downloads.remove(aid);
        if(downloads.size() == 0)
        {
            stopSelf();
            //isUpdate = false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for(DownloadHolder holder : downloads.values())
        {
            holder.download.error("Service has been destroyed(OutOfMemory)");
        }
    }

    private void addDownload(Audio audio,String filePath)
    {
        URL url;
        try
        {
            url = new URL(audio.getUrl());
        }
        catch (MalformedURLException e)
        {
            if(downloads.size() == 0)
                stopSelf();

            e.printStackTrace();
            return;
        }
        DownloadHolder holder = new DownloadHolder();

        holder.download = new Download(url,filePath,audio.getAid(),this);
        holder.downloadBuilder = new DownloadNotifBuilder(audio,filePath,this);
        downloads.put(audio.getAid(),holder);

        holder.download.resume();
    }


    /*
    private final Thread updatingThread = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            while (isUpdate)
            {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(DownloadHolder holder : downloads.values())
                {
                    if(holder.download.getStatus() == Download.States.DOWNLOADING)
                    {
                        Notification notification = holder.downloadBuilder.createStarting(holder.download.getProgress());
                        manager.notify(holder.downloadBuilder.notificationId,notification);
                    }
                }
            }
        }
    });
    private void startUpdatingThread()
    {
         if(!isUpdate)
         {
             isUpdate = true;
             updatingThread.start();
         }
    }
    */



    private final static String defaultDownloadDir = Utils.getRealPath(
            Environment.getExternalStorageDirectory()) + "/download/";

    public static String getDownloadDir()
    {
        String path = PreferenceManager.getDefaultSharedPreferences(
                MyApplication.getInstance()).getString("foldertracks",defaultDownloadDir);

        File defaultFile = new File(path);

        if(!defaultFile.exists() && !defaultFile.mkdirs())
            Log.e("tag","can't create directory");

        return path;

    }

    private static String generateFilePath(Audio track)
    {
        String title = track.getTitle() ;
        if(title.length() > 25)
            title = title.substring(0,25);

        String trackName = (track.getArtist() + "-" + title + ".mp3").replaceAll("[%#@^&$]","");

        return getDownloadDir() + trackName;
    }



    @Override
    public void onCreate()
    {
        super.onCreate();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        downloads = new HashMap<Long,DownloadHolder>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        String action = intent.getAction();

        if(action.equals(PlayingService.actionPlay))
        {
            Audio track = intent.getParcelableExtra(keyDownload);
            if(!downloads.containsKey(track.getAid()))
                addDownload(track, generateFilePath(track));
            else
            {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.track_downloading), Toast.LENGTH_SHORT).show();
                if(downloads.size() < 1)
                    stopSelf();
            }
        }
        else
        {

            long aid = intent.getLongExtra("aid", 666);
            DownloadHolder holder = downloads.get(aid);

            if(holder == null || holder.download == null)
            {
                BugSenseHandler.sendException(new NullPointerException("NullPointer in DownloadingService"));
                return 0;
            }


            if(action.equals(PlayingService.actionClose))
            {
                holder.download.cancel();
            }
            else if(action.equals(PlayingService.actionIconPlay))
            {
                holder.download.resume();
            }
            else if(action.equals(PlayingService.actionIconPause))
            {
                holder.download.pause();
            }
            else
            {
                throw new IllegalArgumentException("wrong action in DownloadingService");
            }
        }


        return START_NOT_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
