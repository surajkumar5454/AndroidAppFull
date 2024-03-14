package com.example.edata;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    private TextView textViewResult;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        textViewResult = findViewById(R.id.textViewUid);
        database = SQLiteDatabase.openDatabase(getDatabasePath("pims.db").toString(), null, SQLiteDatabase.OPEN_READONLY);

        // Retrieve UID_No from intent extra
        String uid = getIntent().getStringExtra("UID_No");
        searchRecords(uid);
        // Fetch complete details for the UID_No from the database
        // Display details in the activity
    }

    private void searchRecords(String uid) {
        // Execute a SQL query to search for records with a matching name
        Cursor cursor = database.rawQuery("SELECT * FROM permanent_info WHERE UID_No LIKE ?", new String[]{"%" + uid + "%"});
        StringBuilder queryResult = new StringBuilder();
        if (cursor.moveToFirst()) {
            do {
                String[] columnNames = cursor.getColumnNames();
                // Append values to StringBuilder
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    String columnName = columnNames[i];
                    String columnValue = cursor.getString(i);
                    // queryResult.append(cursor.getString(i));

                        queryResult.append(columnName).append(":").append(columnValue).append("\n");
                        //queryResult.append(" - "); // Add delimiter between values

                }
                queryResult.append("\n\n\n"); // Add newline after each row
            } while (cursor.moveToNext());
            // Close the cursor after use
            cursor.close();
        }
        // Display query result or handle empty result
        String result = queryResult.toString();
        if (!result.isEmpty()) {
            // Display result in a TextView or handle it as needed
            textViewResult.setText(result);
        } else {
            // No results found
            textViewResult.setText("");
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        }

    }
}
