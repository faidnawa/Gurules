package com.skfaid.gurules.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.skfaid.gurules.R
import com.skfaid.gurules.model.GuruResponse
import com.skfaid.gurules.model.Sortdata
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_guru.view.*
import java.math.RoundingMode


class MainAdapter (
    private val content: List<Sortdata>,
    private val onClickListener: (Sortdata) -> Unit
) :
    RecyclerView.Adapter<MainAdapter.MainViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder =
        MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_guru, parent, false)
        )

    override fun getItemCount(): Int = content.size
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) =
        holder.bind(content[position], onClickListener)

    class MainViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        fun bind(content: Sortdata, onClickListener: (Sortdata) -> Unit) {

            itemView.item_card_guru

            itemView.item_card_guru.setOnClickListener { onClickListener(content) }

            itemView.img_store.load(content.foto) {
                crossfade(true)
                placeholder(R.drawable.loading_animation)
                error(R.drawable.ic_broken_image)
            }


            itemView.tv_title_shop.text = content.nama
            itemView.tv_address_shop.text = content.alamat
            itemView.tv_shop_status.text = content.mata_pelajaran
            itemView.tv_distance.text =
                "${content.distance?.toBigDecimal()?.setScale(1, RoundingMode.UP)?.toDouble()} KM"
        }
    }
}
