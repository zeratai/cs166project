package cs166.cafe.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import cs166.cafe.Cafe;
import cs166.cafe.LoginActivity;

import cs166.cafe.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

public class MenuActivity extends AppCompatActivity {
    private TextView mUserName;
    private TextView myAccountView;
    private LinearLayout mMainMenu;
    private LinearLayout itemResult;
    private LinearLayout itemBackground;
    private EditText itemName;
    private Button logOut;
    private Button backButton;
    private ImageView searchItem;
    private ImageView searchType;
    private String URL;
    private String item;
    private String type;
    private String description;
    private String price;
    private boolean searchItemName = true;
    private boolean searchItemType = false;
    private boolean showItem = false;
    private boolean showMainMenu = true;
    private boolean showTypeResult = false;
    private String uInput;
    private int searchInt = 1;

    private TextView iName;
    private TextView iType;
    private TextView iDescription;
    private TextView iPrice;

    class queryDB implements Runnable {
        Cafe esql = null;
        int action;

        queryDB(int i) {
            action = i;
        }

        public void run() {

            switch (action) {

                case 1:
                String query = String.format("SELECT * FROM Menu WHERE Menu.itemName = '%s'", uInput);
                try {
                    Class.forName("org.postgresql.Driver").newInstance();
                    String dbname = "mydb";
                    String dbport = "5432";

                    esql = new Cafe(dbname, dbport);

                    if ((esql.executeQuery(query)) <= 0) {
                        showMainMenu = true;
                        showItem = false;
                    } else {
                        showItem = true;
                        showMainMenu = false;
                        List<List<String>> l = esql.executeQueryAndReturnResult(query);

                        item = l.get(0).get(0).toString().trim();
                        type = l.get(0).get(1).toString().trim();
                        price = l.get(0).get(2).toString().trim();
                        description = l.get(0).get(3).toString().trim();
                        URL = l.get(0).get(4).toString().trim();

                    }

                    esql.cleanup();

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                    break;

                case 2:

                    String typeQuery = String.format("SELECT * FROM Menu WHERE Menu.type = '%s'", uInput);
                    try {
                        Class.forName("org.postgresql.Driver").newInstance();
                        String dbname = "mydb";
                        String dbport = "5432";

                        esql = new Cafe(dbname, dbport);

                        if ((esql.executeQuery(typeQuery)) <= 0) {
                            showMainMenu = true;
                            showItem = false;
                            showTypeResult = false;
                        } else {
                            showItem = false;
                            showMainMenu = false;
                            showTypeResult = true;
                        }

                        esql.cleanup();

                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        myAccountView = (TextView) findViewById(R.id.my_account_content);
        mUserName = (TextView) findViewById(R.id.userName);
        itemResult = (LinearLayout) findViewById(R.id.itemResult);
        itemName = (EditText) findViewById(R.id.item_search);
        mMainMenu = (LinearLayout) findViewById(R.id.main_menu_layout);
        logOut = (Button) findViewById(R.id.dummy_button);
        searchItem = (ImageView) findViewById(R.id.itemSearch);
        searchType = (ImageView) findViewById(R.id.searchType);
        itemBackground = (LinearLayout) findViewById(R.id.itemBackground);

        iName = (TextView) findViewById(R.id.item_name);
        iType = (TextView) findViewById(R.id.item_type);
        iDescription = (TextView) findViewById(R.id.item_description);
        iPrice = (TextView) findViewById(R.id.item_price);

        backButton = (Button) findViewById(R.id.back_button);

        mUserName.setText(LoginActivity.getUserName());


        // Set up the user interaction to manually show or hide the system UI.
        myAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Back Button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu = true;
                showItem = false;
                showTypeResult = false;

                updateView();
            }
        });

        // Set Listener for Item Type Button
        searchType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchItemName = false;
                searchItemType = true;

                updateView();

            }
        });

        searchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchItemName = true;
                searchItemType = false;

                updateView();
            }
        });

        itemName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {

                    if (searchItemName) {

                        Log.d("Search by Item Name", "");

                        uInput = itemName.getText().toString();

                        queryDB qb = new queryDB(searchInt);

                        Thread t = new Thread(qb);
                        t.start();
                        try {
                            t.join();

                            if (showMainMenu) {
                                Context context = getApplicationContext();
                                CharSequence text = "Cannot find Item!";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                                showMainMenu = true;
                                showItem = false;

                                updateView();
                            }

                            else {
                                try {
                                    final InputStream is = getApplicationContext().getAssets().open(URL);
                                    final Drawable iBackground = Drawable.createFromStream(is, null);

                                    if(iBackground != null) {
                                        itemBackground.setBackground(iBackground);
                                        itemBackground.setAlpha((float) 0.8);

                                        iName.setText(item);
                                        iType.setText(type);
                                        iDescription.setText(description);
                                        iPrice.setText(price);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                mMainMenu.setVisibility(showMainMenu ? View.VISIBLE : View.GONE);
                                itemResult.setVisibility(showItem ? View.VISIBLE : View.GONE);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(searchItemType) {
                    uInput = itemName.getText().toString();

                    Log.d("user input", uInput);
                    Log.d("Search by Item Type", "");

                    queryDB qb = new queryDB(searchInt);

                    Thread t = new Thread(qb);
                    t.start();
                    try {
                        t.join();

                        if (showMainMenu) {
                            Context context = getApplicationContext();
                            CharSequence text = "Cannot find Type!";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }

                        mMainMenu.setVisibility(showMainMenu ? View.VISIBLE : View.GONE);
                        itemResult.setVisibility(showItem ? View.VISIBLE : View.GONE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }

        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    private void updateView() {
        mMainMenu.setVisibility(showMainMenu ? View.VISIBLE : View.GONE);
        itemResult.setVisibility(showItem ? View.VISIBLE : View.GONE);
        if(searchItemName) {
            itemName.setHint(R.string.search_by_item);
            searchInt = 1;
        }
        if(searchItemType) {
            itemName.setHint(R.string.search_by_type);
            searchInt = 2;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}