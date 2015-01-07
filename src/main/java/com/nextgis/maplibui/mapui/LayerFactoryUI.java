
/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
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

package com.nextgis.maplibui.mapui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplibui.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.nextgis.maplib.util.Constants.*;
import static com.nextgis.maplib.util.GeoConstants.*;


public class LayerFactoryUI
        extends LayerFactory
{


    public LayerFactoryUI(File mapPath)
    {
        super(mapPath);
    }


    public void createNewRemoteTMSLayer(
            final Context context,
            final LayerGroup groupLayer)
    {
        final Context appContext = groupLayer.getContext();
        final LinearLayout linearLayout = new LinearLayout(context);
        final EditText input = new EditText(context);
        input.setText(context.getResources().getText(R.string.osm));

        final EditText url = new EditText(context);
        url.setText(context.getResources().getText(R.string.osm_url));

        final TextView stLayerName = new TextView(context);
        stLayerName.setText(context.getString(R.string.layer_name) + ":");

        final TextView stLayerUrl = new TextView(context);
        stLayerUrl.setText(context.getString(R.string.layer_url) + ":");

        final TextView stLayerType = new TextView(context);
        stLayerType.setText(context.getString(R.string.layer_type) + ":");

        final ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        final Spinner spinner = new Spinner(context);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        adapter.add(context.getString(R.string.tmstype_osm));
        adapter.add(context.getString(R.string.tmstype_normal));
        spinner.setSelection(0);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(stLayerName);
        linearLayout.addView(input);
        linearLayout.addView(stLayerUrl);
        linearLayout.addView(url);
        linearLayout.addView(stLayerType);
        linearLayout.addView(spinner);

        new AlertDialog.Builder(context).setTitle(R.string.create_tms_layer)
//                .setMessage(message)
                .setView(linearLayout)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener()
                {
                    public void onClick(
                            DialogInterface dialog,
                            int whichButton)
                    {
                        int tmsType = 0;
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                tmsType = TMSTYPE_OSM;
                                break;
                            case 1:
                                tmsType = TMSTYPE_NORMAL;
                                break;
                        }
                        String layerName = input.getText().toString();
                        String layerURL = url.getText().toString();

                        //check if {x}, {y} or {z} present
                        if (!layerURL.contains("{x}") || !layerURL.contains("{y}") ||
                            !layerURL.contains("{z}")) {
                            Toast.makeText(context, R.string.error_invalid_url, Toast.LENGTH_SHORT)
                                 .show();
                            return;
                        }

                        //create new layer and store it and add it to the map
                        RemoteTMSLayerUI layer = new RemoteTMSLayerUI(appContext, cretateLayerStorage());
                        layer.setName(layerName);
                        layer.setURL(layerURL);
                        layer.setTMSType(tmsType);
                        layer.setVisible(true);

                        groupLayer.addLayer(layer);
                        groupLayer.save();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(
                            DialogInterface dialog,
                            int whichButton)
                    {
                        // Do nothing.
                    }
                })
                .show();
    }



    public ILayer createLayer(
            Context context,
            File path)
    {
        File config_file = new File(path, CONFIG);
        ILayer layer = null;

        try {
            String sData = FileUtil.readFromFile(config_file);
            JSONObject rootObject = new JSONObject(sData);
            int nType = rootObject.getInt(JSON_TYPE_KEY);

            switch (nType) {
                case LAYERTYPE_LOCAL_TMS:
                    //layer = new LocalTMSLayer(this, path, rootObject);
                    break;
                case LAYERTYPE_LOCAL_GEOJSON:
                    //layer = new LocalGeoJsonLayer(this, path, rootObject);
                    break;
                case LAYERTYPE_LOCAL_RASTER:
                    break;
                case LAYERTYPE_REMOTE_TMS:
                    layer = new RemoteTMSLayerUI(context, path);
                    break;
                case LAYERTYPE_NDW_VECTOR:
                    //layer = new NgwVectorLayer(this, path, rootObject);
                    break;
                case LAYERTYPE_NDW_RASTER:
                    //layer = new NgwRasterLayer(this, path, rootObject);
                    break;
                case LAYERTYPE_LOCAL_NGFP:
                    //layer = new LocalNgfpLayer(this, path, rootObject);
                    break;
                case LAYERTYPE_NGW:
                    break;
            }
        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        return layer;
    }

}