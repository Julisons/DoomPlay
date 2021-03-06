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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;


public class SmallWidget extends AppWidgetProvider
{
    public final static String actionUpdateWidget ="doom.update.widget";
    public final static String EXTRA_AUDIO = "audio";
    public final static String EXTRA_SIZE = "size";


    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context,intent);

        if(intent.getAction().equals(actionUpdateWidget))
            updateWidget(context,(Audio)intent.getParcelableExtra(EXTRA_AUDIO),intent.getIntExtra(EXTRA_SIZE,0));

    }
    private static void updateWidget(Context context,Audio audio,int size)
    {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);

        views.setTextViewText(R.id.widgetTitle, audio.getTitle());
        views.setTextViewText(R.id.widgetArtist, audio.getArtist());

        Bitmap cover = AlbumArtGetter.getBitmapFromStore(audio.getAid(),context);
        if (cover != null)
        {
            //TODO: java.lang.IllegalArgumentException: RemoteViews for widget update
            // exceeds maximum bitmap memory usage (used: 3240000, max: 2304000)
            // The total memory cannot exceed that required to fill the device's screen once
            try
            {
                views.setImageViewBitmap(R.id.widgetAlbum, cover);
            }
            catch(IllegalArgumentException e)
            {
                views.setImageViewBitmap(R.id.widgetAlbum,BitmapFactory.decodeResource(context.getResources(), R.drawable.fallback_cover));
            }
            finally {
                cover.recycle();
            }
        }
        else
        {
            Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fallback_cover);
            views.setImageViewBitmap(R.id.widgetAlbum,tempBitmap);
        }



        int playButton = PlayingService.isPlaying ? R.drawable.widget_pause : R.drawable.widget_play;

        views.setImageViewResource(R.id.widgetPlay, playButton);

        ComponentName componentService = new ComponentName(context,PlayingService.class);

        Intent intentPlay = new Intent(PlayingService.actionPlay);
        intentPlay.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.widgetPlay, PendingIntent.getService(context, 0, intentPlay,0));



        Intent intentNext = new Intent(PlayingService.actionNext);
        intentNext.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.widgetNext, PendingIntent.getService(context, 0, intentNext, 0));

        Intent intentPrevious = new Intent(PlayingService.actionPrevious);
        intentPrevious.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.widgetPrevious, PendingIntent.getService(context, 0, intentPrevious, 0));


        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        ComponentName componentWidget = new ComponentName(context,SmallWidget.class);

        int ids[] = manager.getAppWidgetIds(componentWidget);

        for (int widgetID : ids)
            manager.updateAppWidget(widgetID, views);

    }
}
