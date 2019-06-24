package student.com;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import static android.view.View.GONE;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_GET_REQUEST = 1024;
    private static final int CODE_POST_REQUEST = 1025;


    //defining views
    EditText editTextHeroId, editTextName, editTextRealname, editTextRegNumber;
    Spinner spinnerTeam;
    ProgressBar progressBar;
    ListView listView;
    Button buttonAddUpdate;

    //we will use this list to display students in listview
    List<Student> studentList;

    //as the same button is used for create and update
    //we need to track whether it is an update or create operation
    //for this we have this boolean
    boolean isUpdating = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextHeroId = findViewById(R.id.editTextHeroId);
        editTextName =  findViewById(R.id.editTextName);
        editTextRealname = findViewById(R.id.editTextRealname);
        editTextRegNumber =findViewById(R.id.editTextRegNumber);
        spinnerTeam = findViewById(R.id.spinnerTeamAffiliation);

        buttonAddUpdate =findViewById(R.id.buttonAddUpdate);

        progressBar =  findViewById(R.id.progressBar);
        listView =findViewById(R.id.listViewHeroes);

        studentList = new ArrayList<>();


        buttonAddUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if it is updating
                if (isUpdating) {
                    //calling the method update hero
                    //method is commented becuase it is not yet created
                    updateStudent();
                } else {
                    System.out.println("################-Creating a new student record now-########################");
                    createStudent();
                }
            }
        });

        //calling the method read heroes to read existing heros from the database
        //method is commented because it is not yet created
        readStudents();
    }

    //inner class to perform network request extending an AsyncTask
    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {

        //the url where we need to send the request
        String url;

        //the parameters
        HashMap<String, String> params;

        //the request code to define whether it is a GET or POST
        int requestCode;

        //constructor to initialize values
        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        //when the task started displaying a progressbar
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        //this method will give the response from the request
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                    /*
                    This method will be called to reflesh the existing list on the screen and updates it with any
                    new existing entry
                     */
                    refreshStudentList(object.getJSONArray("students"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //the network operation will be performed in background
        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);

            if (requestCode == CODE_GET_REQUEST)
                return requestHandler.sendGetRequest(url);
            return null;
        }
    }

    class StudentAdapter extends ArrayAdapter<Student> {

        //our hero list
        List<Student> studentList;


        //constructor to get the list
        public StudentAdapter(List<Student> studentList) {
            super(MainActivity.this, R.layout.layout_hero_list, studentList);
            this.studentList = studentList;
        }


        //method returning list item
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View listViewItem = inflater.inflate(R.layout.layout_hero_list, null, true);

            //getting the textview for displaying name
            TextView textViewName = listViewItem.findViewById(R.id.textViewName);

            //the update and delete textview
            TextView textViewUpdate = listViewItem.findViewById(R.id.textViewUpdate);
            TextView textViewDelete = listViewItem.findViewById(R.id.textViewDelete);

            final Student student = studentList.get(position);

            textViewName.setText(student.getRegNumber());

            //attaching click listener to update
            textViewUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //so when it is updating we will
                    //make the isUpdating as true
                    isUpdating = true;

                    //we will set the selected student to the UI elements
                    editTextHeroId.setText(String.valueOf(student.getRegNumber()));
                    editTextName.setText(student.getFirstName());
                    editTextRealname.setText(student.getLastName());
                    editTextRegNumber.setText(student.getRegNumber());
                    spinnerTeam.setSelection(((ArrayAdapter<String>) spinnerTeam.getAdapter()).getPosition(student.getGender()));

                    //we will also make the button text to Update
                    buttonAddUpdate.setText("Update");
                }
            });

            //when the user selected delete
            textViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // we will display a confirmation dialog before deleting
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Delete " + student.getFirstName())
                            .setMessage("Are you sure you want to delete it?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //if the choice is yes we will delete the student
                                    //method is commented because it is not yet created
                                    deleteStudent(student.getRegNumber());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            });

            return listViewItem;
        }
    }

    private void createStudent() {
        String firstName = editTextName.getText().toString().trim();
        String lastName = editTextRealname.getText().toString().trim();
        String regNumber=editTextRegNumber.getText().toString().trim();
        String gender = spinnerTeam.getSelectedItem().toString();

        //validating the inputs
        if (TextUtils.isEmpty(firstName)) {
            editTextName.setError("Please enter first name");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            editTextRealname.setError("Please enter last name");
            editTextRealname.requestFocus();
            return;
        }

        //if validation passes
        HashMap<String, String> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("regNumber", regNumber);
        params.put("gender", gender);

        //Calling the create hero API
        PerformNetworkRequest request = new PerformNetworkRequest(Helper.URL_CREATE, params, CODE_POST_REQUEST);
        request.execute();
    }

    private void readStudents() {
        System.out.println(Helper.URL_READ+" URL to read data from the server");
        PerformNetworkRequest request = new PerformNetworkRequest(Helper.URL_READ, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void refreshStudentList(JSONArray students) throws JSONException {
        //clearing previous heroes
        studentList.clear();

        //traversing through all the items in the json array
        //the json we got from the response
        for (int i = 0; i < students.length(); i++) {
            //getting each hero object
            JSONObject obj = students.getJSONObject(i);
            //adding the hero to the list
            studentList.add(new Student(
                    obj.getString("firstName"),
                    obj.getString("lastName"),
                    obj.getString("regNumber"),
                    obj.getString("gender")
            ));
        }
        //creating the adapter and setting it to the custom list view that is supposed
        // to hold the list of students along side with the buttong to update or delete
        StudentAdapter adapter = new StudentAdapter(studentList);
        listView.setAdapter(adapter);
    }

    private void deleteStudent(String regNumber) {
        PerformNetworkRequest request = new PerformNetworkRequest(Helper.URL_DELETE + regNumber, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void updateStudent() {
        String id = editTextHeroId.getText().toString();
        String firstName = editTextName.getText().toString().trim();
        String lastName = editTextRealname.getText().toString().trim();

        String regNumber  = editTextRegNumber.getText().toString().trim();

        String gender = spinnerTeam.getSelectedItem().toString();

        if (TextUtils.isEmpty(firstName)) {
            editTextName.setError("Please enter first name");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            editTextRealname.setError("Please enter last name");
            editTextRealname.requestFocus();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("studentId", id);
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("regNumber", regNumber);
        params.put("gender", gender);


        PerformNetworkRequest request = new PerformNetworkRequest(Helper.URL_UPDATE, params, CODE_POST_REQUEST);
        request.execute();

        buttonAddUpdate.setText("Add");

        editTextName.setText("");
        editTextRealname.setText("");
        editTextRegNumber.setText("");
        spinnerTeam.setSelection(0);

        isUpdating = false;
    }
}
