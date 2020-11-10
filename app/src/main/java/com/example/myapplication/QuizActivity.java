package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extraScore";
    private static final long COUTDOWN_IN_MILLIS = 30000;

    //we need keys to identify the saved values
    private static final String KEY_SCORE = "keyScore";
    private static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    private static final String KEY_MILLIS_LEFT= "keyMillisLeft";
    private static final String KEY_ANSWERED= "keyMillisLeft";
    private static final String KEY_QUESTION_LIST= "keyQuestionList";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCountDown;
    private RadioGroup rbGroup;
    private RadioButton rb1, rb2, rb3;
    private Button buttonConfirm;


    private ColorStateList textColorDefault;
    private ColorStateList textColorDefaultCd;

    private ArrayList<Question> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;

    private int score;
    private boolean answered;

    private long backPressedTime;
    private long timeLeftMillis;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.question);
        textViewScore  = findViewById(R.id.view_score);
        textViewQuestionCount  = findViewById(R.id.question_count);
        textViewCountDown = findViewById(R.id.countdown);
        rbGroup = findViewById(R.id.group_btn);
        rb1 = findViewById(R.id.btn1);
        rb2 = findViewById(R.id.btn2);
        rb3 = findViewById(R.id.btn3);
        buttonConfirm = findViewById(R.id.btn_confirm);

        textColorDefault = rb1.getTextColors();
        textColorDefaultCd = textViewCountDown.getTextColors();

        if( savedInstanceState == null)
        {
            QuizDbHelper dbHelper = new QuizDbHelper(this);
            questionList = dbHelper.getAllQuestions();
            questionCountTotal = questionList.size();
            Collections.shuffle(questionList);
            Log.d("myTag", "its null");

            showNextQuestion();
        } else {
            Log.d("myTag",""+ savedInstanceState.getInt(KEY_QUESTION_COUNT));
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            if(questionList == null) finish();
            questionCountTotal = questionList.size();
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = questionList.get(questionCounter-1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered = savedInstanceState.getBoolean(KEY_ANSWERED);

            if (!answered){
                startCountDown();
            } else {
                updateCountDownText();
                shwoSolution();
            }
        }


    }

    private void checkAnswer()
    {
        answered = true;
        countDownTimer.cancel();

        RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
        int answerNr = rbGroup.indexOfChild(rbSelected)+1;

        if(answerNr == currentQuestion.getAnswerNr())
        {
            score++;
            textViewScore.setText("Score: "+score);
        }

        shwoSolution();
    }

    private void shwoSolution()
    {
        rb1.setTextColor(Color.RED);
        rb2.setTextColor(Color.RED);
        rb3.setTextColor(Color.RED);

        switch(currentQuestion.getAnswerNr())
        {
            case 1:
                rb1.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 1 is Correct");
                break;

            case 2:
                rb2.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 2 is Correct");
                break;

            case 3:
                rb3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 3 is Correct");
                break;
        }

        if(questionCounter < questionCountTotal)
        {
            buttonConfirm.setText("Next");
            buttonConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNextQuestion();
                }
            });

        } else {
            buttonConfirm.setText("Finish");
        }

    }

    private void showNextQuestion()
    {
        rb1.setTextColor(textColorDefault);
        rb2.setTextColor(textColorDefault);
        rb3.setTextColor(textColorDefault);
        rbGroup.clearCheck();

        if(questionCounter < questionCountTotal)
        {
            currentQuestion = questionList.get(questionCounter);
            textViewQuestion.setText(currentQuestion.getQuestion());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());

            questionCounter++;
            textViewQuestionCount.setText("Question: " + questionCounter + "/" +questionCountTotal);
            answered = false;
            buttonConfirm.setText("Confirm");

            timeLeftMillis = COUTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishQuiz();
        }

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!answered)
                {
                    if(rb1.isChecked() || rb2.isChecked() || rb3.isChecked())
                    {
                        checkAnswer();
                    } else {
                        Toast.makeText(QuizActivity.this, "Please Select an answer.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    finishQuiz();
                }
            }
        });

    }

    private void startCountDown()
    {
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                updateCountDownText();
                checkAnswer();
            }
        }.start();
    }

    public void updateCountDownText(){
         int minutes = (int)(timeLeftMillis/1000)/60;
         int seconds = (int)(timeLeftMillis/1000)%60;

         String timeFormated = String.format(Locale.getDefault(),"%02d:%02d",minutes, seconds);

         textViewCountDown.setText(timeFormated);

         if(timeLeftMillis < 10000)
         {
             textViewCountDown.setTextColor(Color.RED);
         }else {
             textViewCountDown.setTextColor(textColorDefaultCd);
         }
    }

    private void finishQuiz()
    {
        //we want to finish and send the result back

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SCORE, score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            finishQuiz();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(countDownTimer != null)
        {
            countDownTimer.cancel();
        }
    }


//  to save the state after page rotate
   /* @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(KEY_SCORE,score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT,timeLeftMillis);
        outState.putBoolean(KEY_ANSWERED, answered);

        //array list of a question object
        Log.d("msg",KEY_QUESTION_LIST);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);

    }*/

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT, timeLeftMillis);
        outState.putBoolean(KEY_ANSWERED, answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);
    }


}