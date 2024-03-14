package com.example.edata;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AllDynamicSearchActivity extends AppCompatActivity {

    private LinearLayout layoutDynamicFilters;
    private TextView textViewResult;
    private Button btnAddFilter, btnQuery;
    private ListView listViewResults;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_dynamic_search_activity);

        layoutDynamicFilters = findViewById(R.id.layoutDynamicFilters);
        btnAddFilter = findViewById(R.id.btnAddFilter);
        btnQuery = findViewById(R.id.btnQuery);
        listViewResults = findViewById(R.id.listViewResults);
      //  textViewResult = findViewById(R.id.textViewResult);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();
        // Initialize database
      //  database = SQLiteDatabase.openDatabase(getDatabasePath("pims_all.db").toString(), null, SQLiteDatabase.OPEN_READONLY);
        //database = openOrCreateDatabase("YourDatabaseName", MODE_PRIVATE, null);

        // Add initial filter views
        addFilterViews();

        // Add filter button click listener
        btnAddFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFilterViews();
            }
        });

        // Query button click listener
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeQuery();
            }
        });
    }

    private void addFilterViews() {
        LinearLayout filterLayout = new LinearLayout(this);
        filterLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        filterLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Add spinner for column names
        Spinner spinner = new Spinner(this);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        List<String> columnNames = getColumnNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, columnNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        filterLayout.addView(spinner);

        // Add AutoCompleteTextView for value
        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this);
        autoCompleteTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        autoCompleteTextView.setHint("Enter Value");
        filterLayout.addView(autoCompleteTextView);

        // Add "Remove Filter" button
        Button btnRemoveFilter = new Button(this);
        btnRemoveFilter.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btnRemoveFilter.setText("X");
        btnRemoveFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDynamicFilters.removeView(filterLayout);
            }
        });
        filterLayout.addView(btnRemoveFilter);

        layoutDynamicFilters.addView(filterLayout);

        // Set adapter for AutoCompleteTextView when the spinner selection changes
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Fetch column values from the database for the selected column
                String selectedColumn = (String) parent.getItemAtPosition(position);
                List<String> columnValues = getColumnValues(selectedColumn);
                ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(AllDynamicSearchActivity.this, android.R.layout.simple_dropdown_item_1line, columnValues);
                autoCompleteTextView.setAdapter(autoCompleteAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private List<String> getColumnValues(String columnName) {
        List<String> values = new ArrayList<>();
        Cursor cursor = database.query(true, "permanent_info", new String[]{columnName}, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String value = cursor.getString(cursor.getColumnIndex(columnName));
                values.add(value);
            }
            cursor.close();
        }
        values.add(" ");
        return values;
    }

    private List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<>();
        Cursor cursor = database.rawQuery("PRAGMA table_info(permanent_info)", null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String columnName = cursor.getString(cursor.getColumnIndex("name"));
                columnNames.add(columnName);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        columnNames.add("UID_No");
//        columnNames.add("Name");
        return columnNames;
    }


    private void executeQuery() {
        // Build and execute the query
        String query = "SELECT * FROM permanent_info WHERE";
        List<String> conditions = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < layoutDynamicFilters.getChildCount(); i++) {
            LinearLayout filterLayout = (LinearLayout) layoutDynamicFilters.getChildAt(i);
            Spinner spinner = (Spinner) filterLayout.getChildAt(0);
            EditText editText = (EditText) filterLayout.getChildAt(1);
            String columnName = spinner.getSelectedItem().toString();
            String value = editText.getText().toString();
            if (!value.isEmpty()) {
                conditions.add(columnName + " LIKE ?");
                values.add("%" + value + "%");
            }
        }
        if (conditions.isEmpty()) {
            query = "SELECT * FROM permanent_info";
        } else {
            query += " " + String.join(" AND ", conditions);
        }

        executeQuery(query, values.toArray(new String[0]));

    }

    private void executeQuery(String query, String[] values) {

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, values);
            LinearLayout layout = findViewById(R.id.detailsLayout); // Assuming you have a LinearLayout with id "detailsLayout" in your activity_details.xml
            layout.removeAllViews();
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder queryResult = new StringBuilder();
                    @SuppressLint("Range") String uid = cursor.getString(cursor.getColumnIndex("UID_No"));
            //        @SuppressLint("Range") String rank = cursor.getString(cursor.getColumnIndex("Rank"));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("Name"));

                    queryResult.append("UID_No:").append(uid).append("\n");
               //     queryResult.append("Rank:").append(rank).append("\n");
                    queryResult.append("Name:").append(name).append("\n");
                    queryResult.append("\n");

                    Intent intent = new Intent(this, DetailsActivity.class);
                    intent.putExtra("UID_No", uid);
                // Create a new TextView to display the record
                    TextView recordTextView = new TextView(this);
                    recordTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    recordTextView.setText(queryResult);
                    // Add the TextView to the layout
                    layout.addView(recordTextView);

                    // Add a separator between records (optional)
                    View separatorView = new View(this);
                    separatorView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1)); // Height of separator
                    separatorView.setBackgroundColor(0); // Color of separator
                    layout.addView(separatorView);
                    recordTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(intent);
                            executeQuery("SELECT * FROM permanent_info WHERE UID_No=?", new String[]{uid});
                        }
                    });

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
