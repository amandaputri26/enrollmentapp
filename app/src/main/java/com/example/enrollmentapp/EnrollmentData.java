package com.example.enrollmentapp;

import android.os.Parcel;
import android.os.Parcelable;

public class EnrollmentData implements Parcelable {
    private String subjectName;
    private int subjectCredits;

    public EnrollmentData(String subjectName, int subjectCredits) {
        this.subjectName = subjectName;
        this.subjectCredits = subjectCredits;
    }

    protected EnrollmentData(Parcel in) {
        subjectName = in.readString();
        subjectCredits = in.readInt();
    }

    public static final Creator<EnrollmentData> CREATOR = new Creator<EnrollmentData>() {
        @Override
        public EnrollmentData createFromParcel(Parcel in) {
            return new EnrollmentData(in);
        }

        @Override
        public EnrollmentData[] newArray(int size) {
            return new EnrollmentData[size];
        }
    };

    public String getSubjectName() {
        return subjectName;
    }

    public int getCredits() {
        return subjectCredits;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subjectName);
        dest.writeInt(subjectCredits);
    }
}

