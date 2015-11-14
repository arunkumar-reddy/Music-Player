package com.example.thejoker.musplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends ActionBarActivity implements MediaController.MediaPlayerControl{

    ViewPager Tab;
    TabPagerAdapter TabAdapter;
    ActionBar actionBar;
    private ArrayList<Song> artistList;
    private ArrayList<Song> albumList;
    private ArrayList<Song> songList;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        artistList = new ArrayList<Song>();
        albumList = new ArrayList<Song>();
        songList = new ArrayList<Song>();
        getArtistList();
        getAlbumList();
        getSongList();
        setController();
        TabAdapter = new TabPagerAdapter(getSupportFragmentManager());
        Tab = (ViewPager)findViewById(R.id.pager);
        new MyAsyncTask().execute();
        Tab.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar = getSupportActionBar();
                        actionBar.setSelectedNavigationItem(position);
                    }
                });
        Tab.setAdapter(TabAdapter);
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener(){
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
                Tab.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
            }
            @Override
            public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
            }
        };
        actionBar.addTab(actionBar.newTab().setText("Artist").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Album").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Song").setTabListener(tabListener));
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_end:
                musicSrv.onDestroy();
                System.exit(0);
                break;
            case R.id.action_shuffle:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void getArtist(View view)
    {
        Intent intent = new Intent(this,ArtistList.class);
        Integer position = Integer.parseInt(view.getTag().toString());
        String artist = artistList.get(position).getArtist();
        intent.putExtra("message",artist);
        startActivity(intent);
    }
    public void getAlbum(View view)
    {
        Intent intent = new Intent(this,AlbumList.class);
        Integer position = Integer.parseInt(view.getTag().toString());
        String album = albumList.get(position).getAlbum();
        intent.putExtra("message",album);
        startActivity(intent);
    }
    public void playSong(View view)
    {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
    private final ServiceConnection musicConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    private void setController()
    {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.pager));
        controller.setEnabled(true);
    }
    private void playNext()
    {
        musicSrv.playNext();
        if(playbackPaused)
        {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }
    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        if(playIntent==null)
        {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    @Override
    protected void onDestroy()
    {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        paused = true;
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }
    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }
    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null&&musicBound&&musicSrv.isPng())
        {
            return musicSrv.getDur();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null&&musicBound&&musicSrv.isPng())
        {
            return musicSrv.getPosn();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null&&musicBound)
        {
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    class MyAsyncTask extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            Drawable d = getResources().getDrawable(R.drawable.tiles);
            LinearLayout layout = (LinearLayout)findViewById(R.id.activity);
            layout.setBackground(d);
            return null;
        }
    }
    public void getArtistList()
    {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst())
        {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumtitle = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumtitle);
                int i, f = 0;
                for (i = 0; i < artistList.size(); i++) {
                    if (thisArtist.equals(artistList.get(i).getArtist())) {
                        f = 1;
                    }
                }
                if (f == 0) {
                    artistList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
                }
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(artistList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getArtist().compareTo(b.getArtist());
            }
        });
    }
    public void getAlbumList()
    {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst())
        {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumtitle = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumtitle);
                int i, f = 0;
                for (i = 0; i < albumList.size(); i++) {
                    if (thisAlbum.equals(albumList.get(i).getAlbum())) {
                        f = 1;
                    }
                }
                if (f == 0) {
                    albumList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
                }
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(albumList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getAlbum().compareTo(b.getAlbum());
            }
        });
    }
    public void getSongList()
    {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst())
        {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumtitle = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            do
            {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumtitle);
                songList.add(new Song(thisId, thisTitle, thisArtist,thisAlbum));
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }
}
