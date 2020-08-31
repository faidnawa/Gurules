package com.skfaid.gurules.base


interface Presenter<T : View> {
    fun onAttach(view: T)

    fun onDetach()
}
