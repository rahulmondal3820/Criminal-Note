package com.example.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt

fun getScaleBitmap(path: String, destWidth: Int, destHeight: Int):Bitmap{

  val option = BitmapFactory.Options()
  option.inJustDecodeBounds=true
  BitmapFactory.decodeFile(path,option)

  val srcWidth  = option.outWidth.toFloat()
  val srcHeight = option.outHeight.toFloat()

  val sampleSize = if (srcHeight<=destHeight && srcWidth<= destWidth){
    1
  }else{
    val scaleHeight = srcHeight /destHeight
    val scaleWidth = srcWidth /destWidth

    minOf(scaleWidth,scaleHeight).roundToInt()
  }
  return BitmapFactory.decodeFile(path,BitmapFactory.Options().apply {
    inSampleSize = sampleSize
  })
}