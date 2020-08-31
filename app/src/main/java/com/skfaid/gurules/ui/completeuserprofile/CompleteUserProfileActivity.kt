@file:Suppress("DEPRECATION")

package com.skfaid.gurules.ui.completeuserprofile

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
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.babedev.dexter.dsl.runtimePermission
import com.skfaid.gurules.MainActivity
import com.skfaid.gurules.R
import com.skfaid.gurules.base.CompleteUserProfileView
import com.skfaid.gurules.util.Constants
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import kotlinx.android.synthetic.main.userprofile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class CompleteUserProfileActivity : AppCompatActivity(), CompleteUserProfileView {

    private lateinit var presenter: CompleteUserProfilePresenter
    private lateinit var progressDialog: ProgressDialog
    private var filePath: Uri? = null
    private var imageFilePath: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userprofile)
        initPresenter()
        onAttachView()
        btnSave.setOnClickListener {
            if (filePath != null) {
                alert(
                    "pastikan data yang anda masukkan sudah benar",
                    "periksa data anda"
                ) {
                    yesButton {
                        saveUserProfileData()
                    }
                    noButton { }
                }.apply {
                    isCancelable = false
                    show()
                }
            } else {
                alert(
                    "Gambara tidak dapat kosong",
                    "peringatan"
                ) {
                    yesButton {}
                }.apply {
                    isCancelable = false
                    show()
                }
            }
            true
        }
    }

    private fun initPresenter() {
        presenter = CompleteUserProfilePresenter()
    }

    override fun onAttachView() {
        presenter.onAttach(this)
        presenter.initFirebase()
        presenter.getUserPhoneFromAuthentication()
        // request permission
        validatePermission()

        btn_choose_image_complete_user.setOnClickListener {
            alert("ambil gambar atau pilih dari galery") {
                positiveButton("pilih galery") {
                    // call File Manager or Gallery Internal / External Storage
                    GlobalScope.launch(Dispatchers.IO) {
                        showFileChooser()
                    }
                }
                negativeButton("ambil gambar") {
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

    override fun userPhoneDataFromAuthentication(phone: String) {
        // Set Text to Form
        tv_phone_number.text = phone
    }

    override fun showProgressDialog() {
        progressDialog = progressDialog(title ="upload data..")
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

    override fun returnMainActivity() {
        finish() // finish this activity
        startActivity<MainActivity>()
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
            startActivityForResult(intent,666)
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
        val imageViewWidth = img_complete_user_profile.width
        val imageViewHeight = img_complete_user_profile.height

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
                        .into(img_complete_user_profile)

                    // Upload to Storage
                    presenter.uploadUserPhoto(filePath)
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
                        .into(img_complete_user_profile)

                    // Upload to Storage
                    presenter.uploadUserPhoto(filePath)
                }
            }
            else -> toast("Unrecognized request code")
        }
    }

    private fun saveUserProfileData() {
        val nameField = findViewById<EditText>(R.id.edt_name_complete_user)
        val addressField = findViewById<EditText>(R.id.edt_address_complete_user)
        val isValid = nameField.nonEmpty() && addressField.nonEmpty()
        val name = edt_name_complete_user.text.toString()
        val address = edt_address_complete_user.text.toString()
        val userPhone = tv_phone_number.text.toString()

        if (isValid) {
            // Upload User Data
            presenter.sendUserData(name, address, userPhone)
        } else {
            // if not valid
            nameField.error = "nama tidak boleh kosong"
            addressField.error = "alamat tidak boleh kosong"
        }
    }
}
