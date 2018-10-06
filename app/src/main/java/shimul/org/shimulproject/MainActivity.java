package shimul.org.shimulproject;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<CustomData> customDataList;

    private Spinner districtSpinner;
    private Spinner schoolSpinner;
    private EditText commentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        districtSpinner = findViewById(R.id.districtSpinner);
        schoolSpinner = findViewById(R.id.schoolSpinner);
        commentEditText = findViewById(R.id.commentEditText);

        //get data from online
        final String dataFromLink = "https://api.androidhive.info/contacts/";
        new GetDataFromOnline(dataFromLink).execute();
    }

    public void submitButtonClicked(View v){

        String district =  districtSpinner.getSelectedItem().toString();
        String school =  schoolSpinner.getSelectedItem().toString();
        String comment =  commentEditText.getText().toString();

        Uploader uploader = new Uploader(this);
        uploader.execute( district, school, comment);
    }


    private class GetDataFromOnline extends AsyncTask<Void, Void, Void> {

        private String urlLink;

        public GetDataFromOnline(String urlLink) {
            this.urlLink = urlLink;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this,"Getting Data From Server",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpRequestHandler sh = new HttpRequestHandler();

            String url = urlLink;
            String jsonStr = sh.makeServiceCall(url);

            if(jsonStr == null){
                showToastMessage("Can not Load Data From Server!");
                return null;
            }

            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray contacts = jsonObj.getJSONArray("contacts");

                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    String district = c.getString("district");
                    String school = c.getString("school");
                    customDataList.add(new CustomData(district,school));
                }
            } catch (final JSONException e) {
                showToastMessage("Parsing Error!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ArrayList<String> onlyDistricts = new ArrayList<>();
            for(int i=0;i<customDataList.size();i++){
                String currentDistrict = customDataList.get(i).getDistrict();
                boolean alreadyHave = false;
                for(int j=0;j<onlyDistricts.size();j++){
                    if(onlyDistricts.get(i).equals(currentDistrict)){
                        alreadyHave = true;
                        break;
                    }
                }
                if(alreadyHave == false) {
                    onlyDistricts.add(currentDistrict);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, onlyDistricts);
            districtSpinner.setAdapter(adapter);
            if(onlyDistricts.size() > 0){
                //show item 0 as selected from the district spinner
                districtSpinner.setSelection(0);

                //now set school according to the district
                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, getSchoolList(onlyDistricts.get(0)));
                schoolSpinner.setAdapter(adapter2);
            }
        }
    }

    public void showToastMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public ArrayList<String> getSchoolList(String district){
        ArrayList<String> ret = new ArrayList<>();
        for(int i=0;i<customDataList.size();i++){
            if(customDataList.get(i).getDistrict().equals(district)){
                ret.add(customDataList.get(i).getSchool());
            }
        }
        return ret;
    }


}
