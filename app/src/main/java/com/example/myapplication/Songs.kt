package com.example.myapplication

import android.os.Parcel
import android.os.Parcelable

data class Songs(val name: String, val resourceId: Int): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readInt()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeInt(resourceId)
    }

    companion object CREATOR : Parcelable.Creator<Songs> {
        override fun createFromParcel(parcel: Parcel): Songs {
            return Songs(parcel)
        }

        override fun newArray(size: Int): Array<Songs?> {
            return arrayOfNulls(size)
        }
    }
}