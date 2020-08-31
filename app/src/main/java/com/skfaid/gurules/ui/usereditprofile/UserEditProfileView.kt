package com.skfaid.gurules.ui.usereditprofile

import com.skfaid.gurules.base.View

interface UserEditProfileView : View {
    fun showProgressDialog()
    fun closeProgressDialog()
    fun progressDialogMessage(message: String)
    fun returnUserProfileActivity()
}