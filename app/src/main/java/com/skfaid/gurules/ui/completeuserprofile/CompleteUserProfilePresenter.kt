package com.skfaid.gurules.ui.completeuserprofile

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.skfaid.gurules.base.CompleteUserProfileView
import com.skfaid.gurules.base.Presenter

class CompleteUserProfilePresenter : Presenter<CompleteUserProfileView> {
    private var mView: CompleteUserProfileView? = null
    private lateinit var db: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var storageReference: StorageReference? = null
    private var authentication: FirebaseAuth? = null

    override fun onAttach(view: CompleteUserProfileView) {
        mView = view
    }

    override fun onDetach() {
        mView = null
    }

    fun initFirebase() {
        db = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
        storageReference = storage?.reference
        authentication = FirebaseAuth.getInstance()
    }

    private fun userAuthentication() = authentication?.currentUser != null

    fun getUserPhoneFromAuthentication() {
        if (userAuthentication()) {
            val phone = authentication?.currentUser?.phoneNumber.toString()

            mView?.userPhoneDataFromAuthentication(phone)
        }
    }

    fun uploadUserPhoto(filePath: Uri?) {
        val userUID = authentication?.currentUser?.uid.toString()
        val imageReference =
            storageReference?.child("users/$userUID/$userUID")

        if (userAuthentication()) {
            // Photo Profile Upload
            if (filePath != null) {
                mView?.showProgressDialog()

                imageReference?.putFile(filePath)
                    ?.addOnSuccessListener {
                        mView?.closeProgressDialog()
                    }
                    ?.addOnFailureListener {
                        mView?.closeProgressDialog()
                    }
                    ?.addOnProgressListener {
                        val progress = 100.0 * it.bytesTransferred / it.totalByteCount

                        mView?.progressDialogMessage("Uploaded $progress %...")
                    }
            }
        }
    }

    fun sendUserData(name: String, address: String, phone: String) {
        val userUID = authentication?.currentUser?.uid.toString()
        val imageThumbnailsReference = storageReference?.child("users/$userUID/$userUID")

        if (userAuthentication()) {
            mView?.showProgressDialog()

            imageThumbnailsReference?.downloadUrl?.addOnSuccessListener {
                val docData = hashMapOf(
                    "address" to address,
                    "image_profile" to (it?.toString() ?: "-"),
                    "name" to name,
                    "phone" to phone,
                    "uid" to userUID
                )

                db?.child("users")?.child(userUID)
                    ?.setValue(docData)
                    ?.addOnSuccessListener {
                        mView?.returnMainActivity()
                    }
                    ?.addOnFailureListener {  e->
                        Log.e("ERROR!!", "$e")
                    }
            }
        }
        mView?.closeProgressDialog()
    }
}