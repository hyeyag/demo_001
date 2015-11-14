/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.support.wearable.notifications;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WearableListView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WearableListView.ClickListener {
    private static final int SAMPLE_NOTIFICATION_ID = 0;
    public static final String KEY_REPLY = "reply";
    private static final int SPEECH_REQUEST_CODE = 0;
    private static List<String> mTODOs = new ArrayList<String>();
    private static Adapter adapter;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            Log.d("input voice", spokenText);
            mTODOs.add(spokenText);
            adapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WearableListView listView = (WearableListView) findViewById(R.id.list);
        //listView.setAdapter(new Adapter(this));
        adapter = new Adapter(this);
        listView.setAdapter(adapter);
        listView.setClickListener(this);

        displaySpeechRecognizer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            Bundle inputResults = RemoteInput.getResultsFromIntent(getIntent());
            if (inputResults != null) {
                CharSequence replyText = inputResults.getCharSequence(KEY_REPLY);
                if (replyText != null) {
                    Toast.makeText(this, TextUtils.concat(getString(R.string.reply_was), replyText),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /** Post a new or updated notification using the selected notification options. */
    private void updateNotification(int presetIndex) {
        NotificationPreset preset = NotificationPresets.PRESETS[presetIndex];
        Notification notif = preset.buildNotification(this);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(SAMPLE_NOTIFICATION_ID, notif);
        finish();
    }

    @Override
    public void onClick(WearableListView.ViewHolder v) {
        //updateNotification((Integer) v.itemView.getTag());
        displaySpeechRecognizer();
    }


    @Override
    public void onTopEmptyRegionClick() {
        mTODOs.clear();
        adapter.notifyDataSetChanged();
        displaySpeechRecognizer();
    }


    private static final class Adapter extends WearableListView.Adapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private int count = 1;

        private Adapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.notif_preset_list_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            TextView view = (TextView) holder.itemView.findViewById(R.id.name);
            //view.setText(mContext.getString(NotificationPresets.PRESETS[position].nameResId));
            //view.setText("test");
            view.setText(mTODOs.get(position));
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            //return NotificationPresets.PRESETS.length;
            //return count;
            if(mTODOs == null)
                return 0;
            return mTODOs.size();
        }
    }
}
