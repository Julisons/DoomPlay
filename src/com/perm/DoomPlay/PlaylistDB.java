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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bugsense.trace.BugSenseHandler;

import java.util.ArrayList;

import static android.provider.MediaStore.Audio.Media;

public class PlaylistDB extends SQLiteOpenHelper
{

    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "playlistsData";
    private static final String TABLE_LISTPLAYLIST = "listplaylist";
    private static final String TABLE_DEFAULT = "defaultTable";
    private static final String KEY_NAME_PLAYLIST = "playlistName";


    private PlaylistDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static volatile PlaylistDB instance;

    public static PlaylistDB getInstance(Context context)
    {
        PlaylistDB temp = instance;
        if(temp == null)
        {
            synchronized (PlaylistDB.class)
            {
                temp = instance;
                if(temp == null)
                    instance = temp = new PlaylistDB(context);
            }
        }
        return temp;
    }

    private static void createTable(String table,SQLiteDatabase db)
    {
          db.execSQL("CREATE TABLE IF NOT EXISTS " + table + "("
                  + Media._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Media.DATA + " TEXT,"+
                  Media.ARTIST + " TEXT,"+ Media.TITLE + " TEXT," + Media.ALBUM_ID +" INTEGER)");
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createListPlaylistTable = "CREATE TABLE IF NOT EXISTS " + TABLE_LISTPLAYLIST + "(" + Media._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME_PLAYLIST + " TEXT" +")";

        db.execSQL(createListPlaylistTable);
        createTable(TABLE_DEFAULT,db);



        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME_PLAYLIST, TABLE_DEFAULT);
        db.insert(TABLE_LISTPLAYLIST,null,cv);

        String[] tables = getListPlaylistForDatabase(db);
        for(String table : tables )
        {
            if(!table.equals(TABLE_DEFAULT))
            {
                createTable(table,db);
            }
        }
    }
    private boolean isContentTrack(String table,String track,SQLiteDatabase db)
    {
        Cursor cursor = db.query(table,new String[]{Media.ALBUM_ID},Media.DATA + "= ?",new String[]{track},null,null,null);

        if(cursor.moveToFirst())
        {
            cursor.close();
            return true;
        }
        else
        {
            cursor.close();
            return false;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String[] tables = getListPlaylistForDatabase(db);
        for(String table : tables)
        {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTPLAYLIST);
        onCreate(db);
    }


    void addTracks(ArrayList<Audio> audios, String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv;

        for(Audio audio : audios)
        {
            if(!isContentTrack(playlist,audio.getUrl(),db))
            {
                cv = new ContentValues();
                cv.put(Media.DATA, audio.getUrl());
                cv.put(Media.ARTIST, audio.getArtist());
                cv.put(Media.ALBUM_ID, audio.getAid());
                cv.put(Media.TITLE, audio.getTitle());
                db.insert(playlist, null, cv);
            }
        }
        db.close();
    }
    void deleteTrack(String trackPath,String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(playlist, Media.DATA + " = ?", new String[]{trackPath});

        db.close();
    }
    ArrayList<Audio> getTracks(String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.query(playlist,TracksHolder.projection,null, null, null, null, null);

        ArrayList<Audio> result = Audio.parseAudiosCursor(c);

        c.close();
        db.close();

        return result;
    }
    private String[] getListPlaylistForDatabase(SQLiteDatabase db)
    {
        Cursor c = db.query(TABLE_LISTPLAYLIST,new String[]{KEY_NAME_PLAYLIST},null,null,null,null,null);

        String[] result = new String[c.getCount()];

        if (c.moveToFirst())
        {
            do
            {
                result[c.getPosition()] = (c.getString(c.getColumnIndex(KEY_NAME_PLAYLIST)));

            }while(c.moveToNext());
        }
        c.close();
        return  result;
    }
    String[] getListPlaylist()
    {

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_LISTPLAYLIST,new String[]{KEY_NAME_PLAYLIST},null,null,null,null,null);

        String[] result = new String[c.getCount()];

        if (c.moveToFirst())
        {
            do
            {
                result[c.getPosition()] = (c.getString(c.getColumnIndex(KEY_NAME_PLAYLIST)));

            }while(c.moveToNext());
        }
        c.close();
        db.close();

        return  result;
    }
    public void deletePlaylist(String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LISTPLAYLIST,KEY_NAME_PLAYLIST + " = ?", new String[]{playlist});
        db.execSQL("DROP TABLE IF EXISTS " + playlist);
        db.close();
    }
    void changeColumns(String playlist,String first,String second)
    {

        SQLiteDatabase db = getWritableDatabase();

        Cursor cFirst = db.query(playlist,TracksHolder.projectionPlusId,Media.DATA + " = ?",new String[]{first},null,null,null);
        Cursor cSecond = db.query(playlist,TracksHolder.projectionPlusId,Media.DATA + " = ?",new String[]{second},null,null,null);

        if(!cFirst.moveToFirst() || !cSecond.moveToFirst())
        {
            // it has thrown CursorIndexOfBoundException twice
            BugSenseHandler.sendException(new IndexOutOfBoundsException("can't changeColumns exception"));
            return;
        }


        String idFirst = cFirst.getString(cFirst.getColumnIndex(Media._ID));
        String artistFirst  = cFirst.getString(cFirst.getColumnIndex(Media.ARTIST));
        String titleFirst  = cFirst.getString(cFirst.getColumnIndex(Media.TITLE));
        long albumIdFirst  = cFirst.getLong(cFirst.getColumnIndex(Media.ALBUM_ID));
        cFirst.close();

        String idSecond = cSecond.getString(cSecond.getColumnIndex(Media._ID));
        String artistSecond  = cSecond.getString(cSecond.getColumnIndex(Media.ARTIST));
        String titleSecond  = cSecond.getString(cSecond.getColumnIndex(Media.TITLE));
        long albumIdSecond  = cSecond.getLong(cSecond.getColumnIndex(Media.ALBUM_ID));
        cSecond.close();



        ContentValues cv = new ContentValues();
        cv.put(Media.DATA,second);
        cv.put(Media.TITLE,titleSecond);
        cv.put(Media.ARTIST,artistSecond);
        cv.put(Media.ALBUM_ID,albumIdSecond);

        ContentValues cv2 = new ContentValues();
        cv2.put(Media.DATA,first);
        cv2.put(Media.TITLE,titleFirst);
        cv2.put(Media.ARTIST,artistFirst);
        cv2.put(Media.ALBUM_ID,albumIdFirst);

        db.update (playlist, cv,Media._ID + " = ?",  new String[]{idFirst});

        db.update (playlist, cv2,Media._ID + " = ?" , new String[]{idSecond});

        db.close();

    }
    void addPlaylist(String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME_PLAYLIST,playlist);
        db.insert(TABLE_LISTPLAYLIST,null,cv);
        createTable(playlist,db);
        db.close();
    }
}