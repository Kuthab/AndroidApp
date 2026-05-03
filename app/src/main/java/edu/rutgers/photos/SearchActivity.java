package edu.rutgers.photos;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.photos.model.DataStore;
import edu.rutgers.photos.model.Tag;

public class SearchActivity extends AppCompatActivity {

    private DataStore store;
    private SearchResultAdapter adapter;

    private Spinner spType1, spType2;
    private AutoCompleteTextView etValue1, etValue2;
    private RadioGroup rgCombine;
    private LinearLayout rowSecond;
    private RecyclerView rvResults;
    private TextView tvEmpty;

    private boolean searched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        store = DataStore.get(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        spType1 = findViewById(R.id.spType1);
        spType2 = findViewById(R.id.spType2);
        etValue1 = findViewById(R.id.etValue1);
        etValue2 = findViewById(R.id.etValue2);
        rgCombine = findViewById(R.id.rgCombine);
        rowSecond = findViewById(R.id.rowSecond);
        rvResults = findViewById(R.id.rvResults);
        tvEmpty = findViewById(R.id.tvEmpty);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{ getString(R.string.person), getString(R.string.location) });
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType1.setAdapter(typeAdapter);
        spType2.setAdapter(typeAdapter);

        rgCombine.setOnCheckedChangeListener((g, id) ->
                rowSecond.setVisibility(id == R.id.rbSingle ? View.GONE : View.VISIBLE));

        attachAutoComplete(spType1, etValue1);
        attachAutoComplete(spType2, etValue2);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(this, this::openHit);
        rvResults.setAdapter(adapter);

        MaterialButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> doSearch());

        MaterialButton btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            etValue1.setText("");
            etValue2.setText("");
            spType1.setSelection(0);
            spType2.setSelection(0);
            rgCombine.check(R.id.rbSingle);
            adapter.submit(new ArrayList<>());
            searched = false;
            tvEmpty.setVisibility(View.GONE);
        });

        etValue1.setOnEditorActionListener((tv, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); return true; }
            return false;
        });
        etValue2.setOnEditorActionListener((tv, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); return true; }
            return false;
        });
    }

    private void attachAutoComplete(Spinner typeSpinner, AutoCompleteTextView et) {
        Runnable refresh = () -> {
            Tag.Type type = typeFor(typeSpinner.getSelectedItemPosition());
            String prefix = et.getText() == null ? "" : et.getText().toString();
            List<String> suggestions = new ArrayList<>(
                    store.autocompleteValues(type, prefix));
            ArrayAdapter<String> a = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            et.setAdapter(a);
        };
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                refresh.run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int a) {
                refresh.run();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        refresh.run();
    }

    private Tag.Type typeFor(int spinnerPos) {
        return spinnerPos == 1 ? Tag.Type.LOCATION : Tag.Type.PERSON;
    }

    private void doSearch() {
        searched = true;
        Tag.Type t1 = typeFor(spType1.getSelectedItemPosition());
        String v1 = etValue1.getText() == null ? "" : etValue1.getText().toString().trim();

        List<DataStore.SearchHit> hits;
        int mode = rgCombine.getCheckedRadioButtonId();
        if (mode == R.id.rbSingle) {
            hits = store.searchSingle(t1, v1);
        } else {
            Tag.Type t2 = typeFor(spType2.getSelectedItemPosition());
            String v2 = etValue2.getText() == null ? "" : etValue2.getText().toString().trim();
            if (mode == R.id.rbAnd) hits = store.searchAnd(t1, v1, t2, v2);
            else hits = store.searchOr(t1, v1, t2, v2);
        }
        adapter.submit(hits);
        tvEmpty.setVisibility(hits.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openHit(DataStore.SearchHit hit) {
        Intent i = new Intent(this, PhotoActivity.class);
        i.putExtra(PhotoActivity.EXTRA_ALBUM_ID, hit.album.getId());
        i.putExtra(PhotoActivity.EXTRA_PHOTO_ID, hit.photo.getId());
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachAutoComplete(spType1, etValue1);
        attachAutoComplete(spType2, etValue2);
        if (searched) doSearch();
    }
}
