package com.app.sambeautyworld.base_classes


import Preferences
import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by android on 2/11/17.
 * *
 */
open class BaseFragment : Fragment() {
    private val PERMISSION_REQUEST = 121
    protected var DATE_FORMAT = "yyyy-MM-dd"
    protected val SEVEN_DAYS = 7 * 60 * 60 * 1000
    protected val overview = 0
    protected val TAG = javaClass.simpleName
    protected var mContent: View? = null// For showing snackbar
    private var mActivity: FragmentActivity? = null

    private var mProgressDialog: Dialog? = null
    private lateinit var mCalendar: Calendar
    private var mStartTime: Calendar? = null
    private var mEndTime: Calendar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContent = view
        mStartTime = Calendar.getInstance()
        mEndTime = Calendar.getInstance()
        mCalendar = Calendar.getInstance()
    }


    fun showSnackBar(message: String) {
        mContent?.let {
            val snackbar = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            val snackbarView = snackbar.view
            val tv = snackbarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
            tv.maxLines = 3
            snackbar.duration = Constants.SNACK_BAR_DURATION
            snackbar.show()
        }
    }


    override fun onPause() {
        super.onPause()
        Utils.hideKeyboard(this.activity!!)
    }

    override fun onStart() {
        super.onStart()
        Utils.hideKeyboard(activity!!)
    }


    /**
     * Add fragment with or without addToBackStack
     * @param fragment       which needs to be attached
     * @param addToBackStack is fragment needed to backstack
     */
    fun addFragment(fragment: Fragment, addToBackStack: Boolean, id: Int) {
        val tag = fragment.javaClass.simpleName
        val fragmentManager = mActivity?.supportFragmentManager
        val fragmentOldObject = fragmentManager?.findFragmentByTag(tag)
        val transaction = fragmentManager?.beginTransaction()
//        transaction?.setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in_reverse, R.anim.anim_out_reverse)
        if (fragmentOldObject != null) {
            fragmentManager.popBackStackImmediate(tag, 0)
        } else {
            if (addToBackStack) {
                transaction?.addToBackStack(tag)
            }
            transaction?.add(id, fragment, tag)
                    ?.commitAllowingStateLoss()
        }
    }

    //for future use
    fun addFragmentForFlipTransition(fragment: Fragment, addToBackStack: Boolean, id: Int) {
        val tag = fragment.javaClass.simpleName
        val fragmentManager = mActivity?.supportFragmentManager
        val fragmentOldObject = fragmentManager?.findFragmentByTag(tag)
        val transaction = fragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.animator.right_in, R.animator.right_out)
        if (fragmentOldObject != null) {
            fragmentManager.popBackStackImmediate(tag, 0)
        } else {
            if (addToBackStack) {
                transaction?.addToBackStack(tag)
            }
            transaction?.replace(id, fragment, tag)
                    ?.commitAllowingStateLoss()
        }
    }


    protected fun setProfileImage(imagePath: String?, imageView_profile: ImageView, progressBar: ProgressBar?) {
//        GlideApp.with(mActivity)
//                .load(imagePath)
//                .placeholder(R.drawable.ic_add_media)
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .centerCrop()
//                .into(imageView_profile)
    }

    public fun goBack() {
        activity?.onBackPressed()
    }

    fun showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = Dialog(mActivity, android.R.style.Theme_Translucent)
            mProgressDialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            mProgressDialog?.setContentView(R.layout.loader_half__layout)
            mProgressDialog?.setCancelable(false)
        }
        mProgressDialog?.show()
    }

    fun hideProgress() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog?.dismiss()
        }
    }

    fun showMessage(message: String) {
        Utils.showSnackbar(mContent, message)
    }


    fun showLoading(show: Boolean?) {
        if (show!!) showProgress() else hideProgress()
    }


    fun checkForPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity!!,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermission()
                return false
            }
        }
        return true
    }


    /**
     * This method will request permission
     */
    private fun requestPermission() {

        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE),
                Constants.PERMISSION_REQUEST_CODE)

    }


    /**
     * This will show permission dialog
     */

    fun permissionDenied() {
        val builder = android.support.v7.app.AlertDialog.Builder(activity!!)

        builder.setMessage(getString(R.string.permission_denied))

        val positiveText = getString(android.R.string.ok)
        builder.setPositiveButton(positiveText
        ) { dialog, which ->
            enablePermission()
        }

        val negativeText = getString(android.R.string.cancel)
        builder.setNegativeButton(negativeText
        ) { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        // display dialog
        dialog.show()
    }


    fun enablePermission() {
        val packageName = activity?.packageName

        try {
            //Open the specific App Info page:
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, PERMISSION_REQUEST)

        } catch (e: ActivityNotFoundException) {
            //e.printStackTrace();
            //Open the generic Apps page:
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            startActivityForResult(intent, PERMISSION_REQUEST)

        }

    }

    fun replaceFragment(fragment: Fragment, animate: Boolean, container: Int) {
        val tag: String = fragment::class.java.simpleName

        val transaction = activity?.supportFragmentManager?.beginTransaction()
//        if (animate) {
//            transaction?.setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in_reverse, R.anim.anim_out_reverse)
//        }
        transaction?.replace(container, fragment, tag)
                ?.commitAllowingStateLoss()
    }

    fun checkForStoragePermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                return false
            }
        }
        return true

    }


    fun handleFailure(error: Throwable) {
        ApiFailureTypes().getFailureMessage(error)
    }

    /**
     **************** show logout dialog ****************
     */

    fun showAlertDialog(pos: Int, heading: String, message: String, listener: AlertDialogListener?) {
        val builder = AlertDialog.Builder(activity!!).apply {
            setTitle(heading)
            setMessage(message)
        }

        val positiveText = getString(android.R.string.ok)
        builder.setPositiveButton(positiveText
        ) { dialog, which ->
            // positive button logic
            dialog.dismiss()
            listener?.actionOk(pos)

        }

        val negativeText = getString(android.R.string.cancel)
        builder.setNegativeButton(negativeText
        ) { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        // display dialog
        dialog.show()
    }


    fun goBackWithDelay() {
        Handler().postDelayed({ goBack() }, Constants.HANDLER_DELAY_TIME)

    }


    /*****
     ****** show notificaiton count ********
     *
     */
    fun showNotificationCount(textView: TextView) {
        val notiCount = Preferences.prefs?.getValue(Constants.NOTI_COUNT, 0) ?: 0
        if (notiCount == 0) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            if (notiCount > 99) {
                textView.text = "99+"
            } else {
                textView.text = notiCount.toString()
            }
        }
    }

    fun setProductList(list: ArrayList<Product>) {
        productList = list
    }

    fun getProductList(): ArrayList<Product> {
        return productList
    }

    fun setServiceList(list: ArrayList<SubService>) {
        subServices = list
    }

    fun getServiceList(): ArrayList<SubService> {
        return subServices
    }

    fun setFavourite(list: ArrayList<BookMark>) {
        booksmarks = list
    }

    fun getFavourite(): ArrayList<BookMark> {
        return booksmarks
    }


    public fun getAddress(latitude: Double, longitude: Double): ArrayList<Address> {
        val result = StringBuilder();
        var addresses: ArrayList<Address> = ArrayList()
        try {
            val geocoder = Geocoder(activity, Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1) as ArrayList<Address>;
            if (addresses.isNotEmpty()) {
                val address = addresses[0];
                result.append(address.locality).append("\n");
                result.append(address.countryName);
            }
        } catch (e: IOException) {

        }

        return addresses;
    }

}