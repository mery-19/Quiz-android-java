package com.example.myapplication;

import android.provider.BaseColumns;

//container of constants
//will contain classes of tables exist in DB
public final class QuizContract {

    private QuizContract(){}

    public static class QuestionsTable implements BaseColumns{
        public static final String TABLE_NAME ="questions_table";
        public static final String COLUMN_QUESTION ="question";
        public static final String COLUMN_OPTION1 ="option1";
        public static final String COLUMN_OPTION2 ="option2";
        public static final String COLUMN_OPTION3 ="option3";
        public static final String COLUMN_ANSWER_NR ="answer_nr";
    }
}
