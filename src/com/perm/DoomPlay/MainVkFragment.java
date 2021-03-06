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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainVkFragment extends Fragment
{
    private MainScreenActivity activity;
    LinearLayout linearLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        activity =(MainScreenActivity)getActivity();
        View view = inflater.inflate(R.layout.main_vk_fragment,container,false);
        linearLoading = (LinearLayout) view.findViewById(R.id.linearLoading);
        view.findViewById(R.id.linearVkAll).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkTop).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkGroup).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkSearch).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkAlbum).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkFriends).setOnClickListener(onClickVkListener);
        return view;
    }

    void setLoading()
    {
        linearLoading.setVisibility(View.VISIBLE);
    }
    void unsetLoading()
    {
        linearLoading.setVisibility(View.GONE);
    }

    boolean trippleCheckToast()
    {
        if(MainScreenActivity.isLoading)
        {
            AbstractList.waitMessage(activity);
            return false;
        }
        else if(!MainScreenActivity.isRegister)
        {
            Toast.makeText(activity,getResources().getString(R.string.please_sign_in),Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            return true;
        }
    }


    private final View.OnClickListener onClickVkListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(trippleCheckToast())
            {
                switch (v.getId())
                {
                    case R.id.linearVkAll:
                        startActivity(new Intent(activity,ListVkActivity.class).setAction(ListVkActivity.actionMyMusic));
                        break;
                    case R.id.linearVkAlbum:
                        startActivity(new Intent(activity,VkAlbumsActivity.class));
                        break;
                    case R.id.linearVkGroup:
                        startActivity(new Intent(activity,VkGrActivity.class));
                        break;
                    case R.id.linearVkSearch:
                        startActivity(new Intent(activity,SearchVkActivity.class));
                        break;
                    case R.id.linearVkTop:
                        startActivity(new Intent(activity,VkPopularActivity.class));
                        break;
                    case R.id.linearVkFriends:
                        startActivity(new Intent(activity,VkFrActivity.class));
                        break;
                }
            }
        }
    };
}
