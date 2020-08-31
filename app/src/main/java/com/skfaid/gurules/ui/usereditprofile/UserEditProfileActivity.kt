@file:Suppress("DEPRECATION")

package com.skfaid.gurules.ui.usereditprofile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.babedev.dexter.dsl.runtimePermission
import com.skfaid.gurules.R
import com.skfaid.gurules.ui.completeuserprofile.UserEditProfilePresenter
import com.skfaid.gurules.util.Constants
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import kotlinx.android.synthetic.main.activity_user_edit_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class UserEditProfileActivity : AppCompatActivity(), UserEditProfileView {

    private lateinit var presenter: UserEditProfilePresenter
    private lateinit var progressDialog: ProgressDialog
    private var userUID: String? = ""
    private var name: String? = ""
    private var imageProfile: String? = ""
    private var address: String? = ""
    private var phone: String? = ""
    private var userSellerStatus: Boolean = false
    private var filePath: Uri? = null
    private var imageFilePath: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit_profile)

        initPresenter()
        onAttachView()
    }

    private fun initPresenter() {
        presenter = UserEditProfilePresenter()
    }

    override fun onAttachView() {
        presenter.onAttach(this)
        presenter.initFirebase()

//        setSupportActionBar(toolbar_user_edit_profile)
//        supportActionBar?.apply {
//            title = "edit profile"
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//        }

        // request permission
        validatePermission()

        // set last data user profile
        val intent = intent
        userUID = intent.getStringExtra(Constants.UID)
        name = intent.getStringExtra(Constants.NAME)
        imageProfile = intent.getStringExtra(Constants.IMG_PROFILE)
        address = intent.getStringExtra(Constants.ADDRESS)
        phone = intent.getStringExtra(Constants.PHONE)
        userSellerStatus = intent.getBooleanExtra(Constants.SELLER_STATUS, false)

        // Set last image profile
        Glide.with(this)
            .load(imageProfile)
            .apply(
                RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(img_user_edit_profile)

        // Set name, address, phone field
        edt_name_edit_profile.setText(name)
        edt_address_edit_profile.setText(address)
        tv_phone_number_edit_profile.text = phone

        // choose image
        btn_choose_image_edit_profile.setOnClickListener {
            alert("ambil atau pilih") {
                positiveButton("pilih") {
                    // call File Manager or Gallery Internal / External Storage
                    GlobalScope.launch(Dispatchers.IO) {
                        showFileChooser()
                    }
                }
                negativeButton("ambil dari kamera") {
                    // call camera intent
                    GlobalScope.launch(Dispatchers.IO) {
                        takePhoto()
                    }
                }
            }.show()
        }
    }

    override fun onDetachView() {
        presenter.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        onDetachView()
    }

    override fun showProgressDialog() {
        progressDialog = progressDialog(title = "upload judul")
        progressDialog.apply {
            setCancelable(false)
            show()
        }
    }

    override fun closeProgressDialog() {
        progressDialog.dismiss()
    }

    override fun progressDialogMessage(message: String) {
        progressDialog.setMessage(message)
    }

    override fun returnUserProfileActivity() {
        onBackPressed()
    }

    private fun validatePermission() {
        runtimePermission {
            permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) {
                checked { }
            }
        }
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, Constants.PICK_PHOTO_CODE)
        }
    }

    private fun takePhoto() {
        try {
            // Temporary for preview image/bitmap not save to local storage (internal / external)
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (callCameraIntent.resolveActivity(packageManager) != null) {
                val authorities = "$packageName.fileprovider"

                filePath = FileProvider.getUriForFile(
                    this,
                    authorities, imageFile
                )
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
                startActivityForResult(callCameraIntent, Constants.CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            toast("Could not create file")
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName: String = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        if (storageDir?.exists() == false) storageDir.mkdirs()
        imageFilePath = imageFile.absolutePath

        return imageFile
    }

    private fun setScaledBitmap(): Bitmap {
        val imageViewWidth = img_user_edit_profile.width
        val imageViewHeight = img_user_edit_profile.height

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, bmOptions)
        val bitmapWidth = bmOptions.outWidth
        val bitmapHeight = bmOptions.outHeight

        val scaleFactor = min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(imageFilePath, bmOptions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Glide.with(this)
                        .load(setScaledBitmap())
                        .apply(
                            RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.ic_broken_image)
                        )
                        .into(img_user_edit_profile)
                }
            }
            Constants.PICK_PHOTO_CODE -> {
                if (data != null) {
                    filePath = data.data

                    val selectedImage =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, filePath)

                    Glide.with(this)
                        .load(selectedImage)
                        .apply(
                            RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.ic_broken_image)
                        )
                        .into(img_user_edit_profile)
                }
            }
            else -> toast("Unrecognized request code")
        }
    }

    private fun saveUpdateUserProfileData() {
        val nameField = findViewById<EditText>(R.id.edt_name_edit_profile)
        val addressField = findViewById<EditText>(R.id.edt_address_edit_profile)
        val isValid = nameField.nonEmpty() && addressField.nonEmpty()
        val name = edt_name_edit_profile.text.toString()
        val address = edt_address_edit_profile.text.toString()
        val userPhone = tv_phone_number_edit_profile.text.toString()

        if (isValid) {
            // Upload User Data
            presenter.sendUserUpdateData(name, address, userSellerStatus, userPhone, filePath)

            Log.d(Constants.DEBUG_TAG, "$name, $address, $userPhone")
        } else {
            // if not valid
            nameField.error = "nama error"
            addressField.error = "alamat error"
            Log.d(Constants.DEBUG_TAG, "$name, $address, $userPhone")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // app bar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_sort, menu)
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.menu_save -> {
//                alert(
//                    "cek datamu",
//                   "cek kembali"
//                ) {
//                    yesButton {
//                        saveUpdateUserProfileData()
//                    }
//                    noButton { }
//                }.apply {
//                    isCancelable = false
//                    show()
//                }
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
