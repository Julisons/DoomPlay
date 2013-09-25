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
import android.content.Intent;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

abstract class AbstractVkItems extends AbstractReceiver
{
    static boolean isLoading;
    LinearLayout linearLoading;
    ListView listView;

    protected abstract void onClickRefresh();
    protected abstract ArrayList<Audio> getAudios(int position);

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemRefresh:
                onClickRefresh();
                return true;
            case R.id.itemInterrupt:
                cancelLoading();
                return true;
            case R.id.itemExit:
                sendBroadcast(new Intent(actionKill));
                return true;
            case R.id.itemSettings:
                startActivity(new Intent(this,SettingActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private TaskLoader taskLoader;

    @Override
    public void onBackPressed()
    {
        cancelLoading();
        super.onBackPressed();
    }
            AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {

                    if(!isLoading)
                    {
                        if(Utils.isOnline(getBaseContext()))
                        {
                            taskLoader = new TaskLoader();
                            taskLoader.execute(position);
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "check internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
            else
                Toast.makeText(getBaseContext(), "please wait", Toast.LENGTH_SHORT).show();
        }

    };

    void cancelLoading()
    {
        if(isLoading && taskLoader != null)
        {
            isLoading = false;
            linearLoading.setVisibility(View.GONE);
            taskLoader.cancel(true);
        }
    }
    protected void startListVkActivity(ArrayList<Audio> audios)
    {
        Intent intent = new Intent(this,ListVkActivity.class);
        intent.setAction(ListVkActivity.actionJust);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack,audios);
        startActivity(intent);
    }
    protected void setLoading()
    {
         if(isLoading)
         {
             isLoading = false;
             linearLoading.setVisibility(View.GONE);
         }
         else
         {
             linearLoading.setVisibility(View.VISIBLE);
             isLoading = true;
         }
    }



    class TaskLoader extends AsyncTask<Integer,Void,ArrayList<Audio>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            setLoading();
        }

        @Override
        protected ArrayList<Audio> doInBackground(Integer... params)
        {
            return getAudios(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Audio> audios)
        {
            super.onPostExecute(audios);
            setLoading();

            if(audios == null)
                Toast.makeText(getBaseContext(),"can't get Audio",Toast.LENGTH_SHORT).show();
            else
                startListVkActivity(audios);
        }
    }
}
