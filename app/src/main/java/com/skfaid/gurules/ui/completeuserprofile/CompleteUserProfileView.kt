package com.skfaid.gurules.base

interface CompleteUserProfileView : View {
    fun userPhoneDataFromAuthentication(phone: String)
    fun showProgressDialog()
    fun closeProgressDialog()
    fun progressDialogMessage(message: String)
    fun returnMainActivity()
}