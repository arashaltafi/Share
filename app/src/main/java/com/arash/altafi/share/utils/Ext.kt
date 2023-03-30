package com.arash.altafi.share.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.arash.altafi.share.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.File

fun ImageView.clear() {
    this.setImageDrawable(null)
}

fun ImageView.loadCompat(
    url: Any,
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    if (url is String && url.toString().contains(".svg"))
        loadSVG(url, placeholderRes, errorRes, requestOptions)
    else loadDrawable(url, placeholderRes, errorRes, requestOptions)
}

@SuppressLint("CheckResult")
private fun ImageView.loadSVG(
    url: Any,
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(context).getSVGRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            errorRes?.let { error(it) }
        }.into(this)
}


private fun ImageView.loadDrawable(
    url: Any,
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(context).getDrawableRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            errorRes?.let { error(it) }
        }.into(this)
}

fun Context.getBitmap(
    url: Any,
    result: ((Bitmap) -> Unit),
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(this).getBitmapRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            error(errorRes)
        }
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                result.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

        })

}

fun ImageView.setBlurImage(url: File) {
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
        .into(this)
}

fun ImageView.setBlurImage(url: String) {
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
        .into(this)
}

fun ImageView.setBlurImage(@DrawableRes url: Int) {
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
        .into(this)
}


fun View.toShow() {
    this.visibility = View.VISIBLE
}

fun View.isShow(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.toHide() {
    this.visibility = View.INVISIBLE
}

fun View.isHide(): Boolean {
    return this.visibility == View.INVISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.isGone(): Boolean {
    return this.visibility == View.GONE
}
