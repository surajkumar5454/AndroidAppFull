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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AparActivity extends AppCompatActivity {

    private EditText uidNosearch;
    private Button btnQuery;
    private ListView listViewResults;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apar_activity);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        uidNosearch = findViewById(R.id.uidNo);
        btnQuery = findViewById(R.id.btnQuery);
        listViewResults = findViewById(R.id.listViewResults);

        // Query button click listener
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uidNo = uidNosearch.getText().toString().trim();
                executeQuery(uidNo);
            //    executeQuery();
            }
        });
    }

    private void executeQuery(String values) {

        Cursor cursor = database.rawQuery("SELECT * FROM apar WHERE UID_No LIKE ?", new String[]{"%" + values + "%"});
        try {
            LinearLayout layout = findViewById(R.id.detailsLayout); // Assuming you have a LinearLayout with id "detailsLayout" in your activity_details.xml
            layout.removeAllViews();
            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder queryResult = new StringBuilder();
                    String uid = cursor.getString(0);
                    String name = cursor.getString(1);
                    String dateFrom = cursor.getString(2);
                    String dateTo = cursor.getString(3);
                    String grading = cursor.getString(4);
                    String numGrading = cursor.getString(5);
                    String adverse = cursor.getString(6);
                    String remark = cursor.getString(7);
                    String integrity = cursor.getString(8);
                    if (count==0) {
                        queryResult.append(uid).append("\n");
                        queryResult.append(name).append("\n\n\n");
                        count = 1;
                    }

                    queryResult.append("From: ").append(dateFrom).append("\n");
                    queryResult.append("To: ").append(dateTo).append("\n");
                    queryResult.append("Grading: ").append(grading).append("\n");
                    queryResult.append("Numerical Grading: ").append(numGrading).append("\n");
                    queryResult.append("Adverse: ").append(adverse).append("\n");
                    queryResult.append("Remark: ").append(remark).append("\n");
                    queryResult.append("Integrity: ").append(integrity).append("\n");
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
                       //     startActivity(intent);
                       //     executeQuery("SELECT * FROM posting_info WHERE UID_No=?", new String[]{uid});
                        }
                    });

                } while (cursor.moveToNext());
            }
            else
            {
                // If no records were found, display a message to the user
                Toast.makeText(this, "No records found with the UID: " + values, Toast.LENGTH_SHORT).show();
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
