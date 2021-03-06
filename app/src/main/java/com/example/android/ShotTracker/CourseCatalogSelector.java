package com.example.android.ShotTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.SubCourse;

import java.util.List;

/**
 * Created by ewjensen on 3/25/15.
 */
public class CourseCatalogSelector extends Activity {

    private long courseID;
    private String courseName;

    private TextView[] subCourseNames = new TextView[5];

    private Vibrator vibe;

    private CourseDAO courseDAO = null;
    private SubCourseDAO subCourseDAO = null;

    private List<SubCourse> subCourses = null;

    private boolean fullRound = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coursecatalogsubcourseselector);

        Intent myIntent = getIntent();
        courseID = myIntent.getLongExtra("Course ID", -1);

        //Initialize Vibrate
        vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        //Sets the course name in a banner along the top
        setCourseName();

        //Loads subcourses of the given course
        setSubCourses();

        //Switches between number of holes the players are going to play
        numberOfHolesSwitchRadioButton();

        //On next
        selectPlayerButtonInitializer();
    }

    public void setCourseName(){
        courseDAO = new CourseDAO(this);

        courseName = courseDAO.readCourseNameFromID(courseID);

        TextView courseNameText = (TextView)findViewById(R.id.coursecatalogsubcourseselectorcoursename);
        courseNameText.setText(courseName);
    }

    public void setSubCourses(){

        subCourseNames[0] = (TextView)findViewById(R.id.coursecatalogsubcourseselectorcourse1text);
        subCourseNames[1] = (TextView)findViewById(R.id.coursecatalogsubcourseselectorcourse2text);
        subCourseNames[2] = (TextView)findViewById(R.id.coursecatalogsubcourseselectorcourse3text);
        subCourseNames[3] = (TextView)findViewById(R.id.coursecatalogsubcourseselectorcourse4text);
        subCourseNames[4] = (TextView)findViewById(R.id.coursecatalogsubcourseselectorcourse5text);

        //Radiobutton[front/back][subcoursenumber]
        RadioButton[][] radioButton = new RadioButton[2][5];
        radioButton[0][0] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorfront9radio1);
        radioButton[0][1] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorfront9radio2);
        radioButton[0][2] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorfront9radio3);
        radioButton[0][3] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorfront9radio4);
        radioButton[0][4] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorfront9radio5);
        radioButton[1][0] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorback9radio1);
        radioButton[1][1] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorback9radio2);
        radioButton[1][2] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorback9radio3);
        radioButton[1][3] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorback9radio4);
        radioButton[1][4] = (RadioButton)findViewById(R.id.coursecatalogsubcourseselectorback9radio5);

        subCourseDAO = new SubCourseDAO(this);

        Course course = courseDAO.readCourseFromID(courseID);
        subCourses = subCourseDAO.readListofSubCourses(course);

        switch (subCourses.size()) {
            case 1: {
                subCourseNames[0].setText(subCourses.get(0).getName());
                subCourseNames[0].setVisibility(View.VISIBLE);
                radioButton[0][0].setVisibility(View.VISIBLE);
                radioButton[1][0].setVisibility(View.VISIBLE);
                break;
            }
            case 2: {
                subCourseNames[0].setText(subCourses.get(0).getName());
                subCourseNames[0].setVisibility(View.VISIBLE);
                radioButton[0][0].setVisibility(View.VISIBLE);
                radioButton[1][0].setVisibility(View.VISIBLE);

                subCourseNames[1].setText(subCourses.get(1).getName());
                subCourseNames[1].setVisibility(View.VISIBLE);
                radioButton[0][1].setVisibility(View.VISIBLE);
                radioButton[1][1].setVisibility(View.VISIBLE);
                break;
            }
            case 3: {
                subCourseNames[0].setText(subCourses.get(0).getName());
                subCourseNames[0].setVisibility(View.VISIBLE);
                radioButton[0][0].setVisibility(View.VISIBLE);
                radioButton[1][0].setVisibility(View.VISIBLE);

                subCourseNames[1].setText(subCourses.get(1).getName());
                subCourseNames[1].setVisibility(View.VISIBLE);
                radioButton[0][1].setVisibility(View.VISIBLE);
                radioButton[1][1].setVisibility(View.VISIBLE);

                subCourseNames[2].setText(subCourses.get(2).getName());
                subCourseNames[2].setVisibility(View.VISIBLE);
                radioButton[0][2].setVisibility(View.VISIBLE);
                radioButton[1][2].setVisibility(View.VISIBLE);
                break;
            }
            case 4: {
                subCourseNames[0].setText(subCourses.get(0).getName());
                subCourseNames[0].setVisibility(View.VISIBLE);
                radioButton[0][0].setVisibility(View.VISIBLE);
                radioButton[1][0].setVisibility(View.VISIBLE);

                subCourseNames[1].setText(subCourses.get(1).getName());
                subCourseNames[1].setVisibility(View.VISIBLE);
                radioButton[0][1].setVisibility(View.VISIBLE);
                radioButton[1][1].setVisibility(View.VISIBLE);

                subCourseNames[2].setText(subCourses.get(2).getName());
                subCourseNames[2].setVisibility(View.VISIBLE);
                radioButton[0][2].setVisibility(View.VISIBLE);
                radioButton[1][2].setVisibility(View.VISIBLE);

                subCourseNames[3].setText(subCourses.get(3).getName());
                subCourseNames[3].setVisibility(View.VISIBLE);
                radioButton[0][3].setVisibility(View.VISIBLE);
                radioButton[1][3].setVisibility(View.VISIBLE);
                break;
            }
            case 5: {
                subCourseNames[0].setText(subCourses.get(0).getName());
                subCourseNames[0].setVisibility(View.VISIBLE);
                radioButton[0][0].setVisibility(View.VISIBLE);
                radioButton[1][0].setVisibility(View.VISIBLE);

                subCourseNames[1].setText(subCourses.get(1).getName());
                subCourseNames[1].setVisibility(View.VISIBLE);
                radioButton[0][1].setVisibility(View.VISIBLE);
                radioButton[1][1].setVisibility(View.VISIBLE);

                subCourseNames[2].setText(subCourses.get(2).getName());
                subCourseNames[2].setVisibility(View.VISIBLE);
                radioButton[0][2].setVisibility(View.VISIBLE);
                radioButton[1][2].setVisibility(View.VISIBLE);

                subCourseNames[3].setText(subCourses.get(3).getName());
                subCourseNames[3].setVisibility(View.VISIBLE);
                radioButton[0][3].setVisibility(View.VISIBLE);
                radioButton[1][3].setVisibility(View.VISIBLE);

                subCourseNames[4].setText(subCourses.get(4).getName());
                subCourseNames[4].setVisibility(View.VISIBLE);
                radioButton[0][4].setVisibility(View.VISIBLE);
                radioButton[1][4].setVisibility(View.VISIBLE);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void numberOfHolesSwitchRadioButton(){
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.coursecatalogsubcourseselectorradiobuttons);

        //called when radio button is selected
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup rGroup, int checkedId)
            {
                vibe.vibrate(15);

                int numHoles = rGroup.getCheckedRadioButtonId();

                RadioGroup back9RadioGroup = (RadioGroup)findViewById(R.id.coursecatalogsubcourseselectorback9);
                TextView back9Label = (TextView)findViewById(R.id.coursecatalogsubcourseselectorfrontbackheaderback);

                switch (numHoles) {
                    case R.id.coursecatalogsubcourseselectorradioButton1: {
                        fullRound = false;
                        back9RadioGroup.setVisibility(View.INVISIBLE);
                        back9Label.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case R.id.coursecatalogsubcourseselectorradioButton2: {
                        fullRound = true;
                        back9RadioGroup.setVisibility(View.VISIBLE);
                        back9Label.setVisibility(View.VISIBLE);
                        break;
                    }
                    default: {
                        fullRound = true;
                        back9RadioGroup.setVisibility(View.VISIBLE);
                        back9Label.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        });
    }

    private void selectPlayerButtonInitializer(){
        Button selectPlayersButton = (Button)findViewById(R.id.coursecatalogstartroundactivitybutton);

        selectPlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    vibe.vibrate(15);

                    RadioGroup[] radioGroups = new RadioGroup[2];
                    radioGroups[0] = (RadioGroup)findViewById(R.id.coursecatalogsubcourseselectorfront9);
                    radioGroups[1] = (RadioGroup)findViewById(R.id.coursecatalogsubcourseselectorback9);

                    int front9RadioButtonID = radioGroups[0].getCheckedRadioButtonId();
                    int back9RadioButtonID = radioGroups[1].getCheckedRadioButtonId();

                    long front9SubCourseID;
                    long back9SubCourseID;

                    switch (front9RadioButtonID) {
                        case R.id.coursecatalogsubcourseselectorfront9radio1: {
                            front9SubCourseID = subCourses.get(0).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorfront9radio2: {
                            front9SubCourseID = subCourses.get(1).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorfront9radio3: {
                            front9SubCourseID = subCourses.get(2).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorfront9radio4: {
                            front9SubCourseID = subCourses.get(3).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorfront9radio5: {
                            front9SubCourseID = subCourses.get(4).getID();
                            break;
                        }
                        default: {
                            front9SubCourseID = -1;
                            break;
                        }
                    }

                    switch (back9RadioButtonID) {
                        case R.id.coursecatalogsubcourseselectorback9radio1: {
                            back9SubCourseID = subCourses.get(0).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorback9radio2: {
                            back9SubCourseID = subCourses.get(1).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorback9radio3: {
                            back9SubCourseID = subCourses.get(2).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorback9radio4: {
                            back9SubCourseID = subCourses.get(3).getID();
                            break;
                        }
                        case R.id.coursecatalogsubcourseselectorback9radio5: {
                            back9SubCourseID = subCourses.get(4).getID();
                            break;
                        }
                        default: {
                            back9SubCourseID = -1;
                            break;
                        }
                    }

                    //The file name is sent to the next activity which is entering the players names
                    Intent myIntent = new Intent(v.getContext(), CourseInfo.class);
                    if (fullRound == false) {
                        myIntent.putExtra("Front 9 SubCourseID", front9SubCourseID);
                        myIntent.putExtra("Back 9 SubCourseID", (long)-10);
                    } else {
                        myIntent.putExtra("Front 9 SubCourseID", front9SubCourseID);
                        myIntent.putExtra("Back 9 SubCourseID", back9SubCourseID);
                    }
                    startActivity(myIntent);

                    //The activity is then closed
                    finish();

                } catch (Exception e) {
                }
            }
        });
    }
}
