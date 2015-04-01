/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.maplibui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplibui.R;
import com.nextgis.maplibui.api.IChooseLayerResult;
import com.nextgis.maplibui.api.ILayerSelector;
import com.nextgis.maplibui.api.ILayerUI;

import java.util.ArrayList;
import java.util.List;


/**
 * A dialog to choose layer and start attributes edit form
 */
public class ChooseLayerDialog extends DialogFragment implements ILayerSelector
{
    protected String       mTitle;
    protected List<ILayer> mLayers;
    protected ChooseLayerListAdapter mListAdapter;
    protected int mCode;

    protected final static String KEY_TITLE = "title";
    protected final static String KEY_LAYERS_IDS = "ids";
    protected final static String KEY_CODE = "code";

    public ChooseLayerDialog setTitle(String title)
    {
        mTitle = title;
        return this;
    }

    public ChooseLayerDialog setLayerList(List<ILayer> list)
    {
        mLayers = list;
        return this;
    }


    public ChooseLayerDialog setCode(int code)
    {
        mCode = code;
        return this;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Context context = new ContextThemeWrapper(getActivity(), R.style.NGDialog);

        mListAdapter = new ChooseLayerListAdapter(this);

        if(null == savedInstanceState){

        }
        else{
            mTitle = savedInstanceState.getString(KEY_TITLE);
            List<Integer> ids = savedInstanceState.getIntegerArrayList(KEY_LAYERS_IDS);
            IGISApplication app = (IGISApplication) getActivity().getApplication();
            MapBase map = app.getMap();
            mLayers = new ArrayList<>();
            for(Integer id : ids){
                ILayer layer = map.getLayerById(id);
                mLayers.add(layer);
            }
            mCode = savedInstanceState.getInt(KEY_CODE);
        }

        View view = View.inflate(context, R.layout.layout_layers, null);
        ListView dialogListView = (ListView) view.findViewById(R.id.listView);
        dialogListView.setAdapter(mListAdapter);
        dialogListView.setOnItemClickListener(mListAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(mTitle)
               .setView(view)
               .setInverseBackgroundForced(true)
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
               {
                   public void onClick(
                           DialogInterface dialog,
                           int id)
                   {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString(KEY_TITLE, mTitle);
        ArrayList<Integer> ids = new ArrayList<>();
        for(ILayer layer : mLayers){
            ids.add((int) layer.getId());
        }
        outState.putIntegerArrayList(KEY_LAYERS_IDS, ids);
        outState.putInt(KEY_CODE, mCode);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onLayerSelect(ILayer layer)
    {
        IChooseLayerResult activity = (IChooseLayerResult) getActivity();
        if(null != activity) {
            activity.onFinishChooseLayerDialog(mCode, layer);
        }

        dismiss();
    }


    @Override
    public List<ILayer> getLayers()
    {
        return mLayers;
    }


    @Override
    public Context getContext()
    {
        return getActivity();
    }
}