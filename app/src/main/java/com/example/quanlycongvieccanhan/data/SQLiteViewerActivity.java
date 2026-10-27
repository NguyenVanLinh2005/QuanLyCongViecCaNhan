package com.example.quanlycongvieccanhan.data;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.quanlycongvieccanhan.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SQLiteViewerActivity extends AppCompatActivity {

    private TextView tvDbName, tvDbPath, tvDbInfo, tvTableSummary, tvSchemaDetails, tvQueryResultTitle;
    private EditText etSqlQuery;
    private LinearLayout layoutRawSqlRows;
    private File dbFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite_viewer);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarSqlite);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvDbName = findViewById(R.id.tvDbName);
        tvDbPath = findViewById(R.id.tvDbPath);
        tvDbInfo = findViewById(R.id.tvDbInfo);
        tvTableSummary = findViewById(R.id.tvTableSummary);
        tvSchemaDetails = findViewById(R.id.tvSchemaDetails);
        tvQueryResultTitle = findViewById(R.id.tvQueryResultTitle);

        etSqlQuery = findViewById(R.id.etSqlQuery);
        layoutRawSqlRows = findViewById(R.id.layoutRawSqlRows);

        MaterialButton btnExportDb = findViewById(R.id.btnExportDb);
        MaterialButton btnRunQuery = findViewById(R.id.btnRunQuery);

        dbFile = getDatabasePath("task_database");

        loadDatabaseFileInfo();
        loadTableSchema();
        executeSqlQuery("SELECT * FROM task_table");

        btnRunQuery.setOnClickListener(v -> {
            String query = etSqlQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                executeSqlQuery(query);
            }
        });

        btnExportDb.setOnClickListener(v -> exportSqliteDbFile());
    }

    private void loadDatabaseFileInfo() {
        if (dbFile != null && dbFile.exists()) {
            long sizeKb = dbFile.length() / 1024;
            tvDbName.setText("• Tên CSDL SQLite: task_database");
            tvDbPath.setText("• Đường dẫn CSDL: " + dbFile.getAbsolutePath());
            tvDbInfo.setText("• Dung lượng tệp: " + sizeKb + " KB | Phiên bản SQLite: 3");
        } else {
            tvDbInfo.setText("• Trạng thái CSDL: Đang khởi tạo tệp...");
        }
    }

    private void loadTableSchema() {
        if (dbFile == null || !dbFile.exists()) return;

        StringBuilder schemaSb = new StringBuilder();
        schemaSb.append("BẢNG: task_table\n");
        schemaSb.append("=========================================\n");

        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
            Cursor cursor = db.rawQuery("PRAGMA table_info(task_table)", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int cid = cursor.getInt(cursor.getColumnIndexOrThrow("cid"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    int notNull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull"));
                    int pk = cursor.getInt(cursor.getColumnIndexOrThrow("pk"));

                    schemaSb.append(cid).append(". ").append(name)
                            .append(" (").append(type).append(")")
                            .append(pk == 1 ? " [PRIMARY KEY]" : "")
                            .append(notNull == 1 ? " [NOT NULL]" : "")
                            .append("\n");
                }
                cursor.close();
            }
            tvSchemaDetails.setText(schemaSb.toString());
        } catch (Exception e) {
            tvSchemaDetails.setText("Lỗi đọc cấu trúc bảng SQLite: " + e.getMessage());
        }
    }

    private void executeSqlQuery(String sql) {
        if (dbFile == null || !dbFile.exists()) {
            Toast.makeText(this, "Tệp CSDL SQLite chưa được tạo!", Toast.LENGTH_SHORT).show();
            return;
        }

        layoutRawSqlRows.removeAllViews();
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                int rowCount = cursor.getCount();
                tvTableSummary.setText("• Bảng dữ liệu: task_table (" + rowCount + " bản ghi)");
                tvQueryResultTitle.setText("📊 Kết quả Lệnh SQL (" + rowCount + " hàng dữ liệu):");

                int index = 1;
                while (cursor.moveToNext()) {
                    LinearLayout rowCard = new LinearLayout(this);
                    rowCard.setOrientation(LinearLayout.VERTICAL);
                    rowCard.setBackgroundColor(Color.parseColor("#F4F7FE"));
                    rowCard.setPadding(24, 20, 24, 20);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 16);
                    rowCard.setLayoutParams(lp);

                    TextView tvRowTitle = new TextView(this);
                    tvRowTitle.setText("📌 Bản ghi SQL #" + index++);
                    tvRowTitle.setTextColor(Color.parseColor("#4318FF"));
                    tvRowTitle.setTextSize(14);
                    tvRowTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    rowCard.addView(tvRowTitle);

                    StringBuilder rowContentSb = new StringBuilder();
                    for (int col = 0; col < cursor.getColumnCount(); col++) {
                        String colName = cursor.getColumnName(col);
                        String val = cursor.getString(col);
                        rowContentSb.append("• ").append(colName).append(": ").append(val != null ? val : "NULL").append("\n");
                    }

                    TextView tvRowBody = new TextView(this);
                    tvRowBody.setText(rowContentSb.toString().trim());
                    tvRowBody.setTextColor(Color.parseColor("#2B3674"));
                    tvRowBody.setTextSize(12);
                    tvRowBody.setTypeface(android.graphics.Typeface.MONOSPACE);
                    tvRowBody.setPadding(0, 8, 0, 0);
                    rowCard.addView(tvRowBody);

                    layoutRawSqlRows.addView(rowCard);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi thực thi SQL: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportSqliteDbFile() {
        if (dbFile == null || !dbFile.exists()) {
            Toast.makeText(this, "Không tìm thấy tệp CSDL SQLite để xuất!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File exportFile = new File(getCacheDir(), "task_database.db");
            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = new FileOutputStream(exportFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", exportFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ Tệp Cơ Sở Dữ Liệu SQLite (.db)"));

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi chia sẻ tệp SQLite: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}